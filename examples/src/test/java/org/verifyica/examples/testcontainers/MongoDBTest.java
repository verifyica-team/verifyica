/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.verifyica.examples.testcontainers;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.examples.support.RandomSupport.randomString;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.bson.Document;
import org.testcontainers.containers.Network;
import org.verifyica.api.Trap;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

public class MongoDBTest {

    private static final Logger LOGGER = Logger.createLogger(MongoDBTest.class);

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> nameThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<MongoDBTestEnvironment> arguments() {
        return Stream.of(
                new MongoDBTestEnvironment("mongo:4.4"),
                new MongoDBTestEnvironment("mongo:5.0"),
                new MongoDBTestEnvironment("mongo:6.0"),
                new MongoDBTestEnvironment("mongo:7.0"));
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(MongoDBTestEnvironment mongoDBTestEnvironment) {
        LOGGER.info("[%s] initialize test environment ...", mongoDBTestEnvironment.getName());

        Network network = Network.newNetwork();
        network.getId();

        networkThreadLocal.set(network);
        mongoDBTestEnvironment.initialize(network);

        assertThat(mongoDBTestEnvironment.isRunning()).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testInsert(MongoDBTestEnvironment mongoDBTestEnvironment) {
        LOGGER.info("[%s] testing testInsert() ...", mongoDBTestEnvironment.getName());

        String name = randomString(16);
        nameThreadLocal.set(name);
        LOGGER.info("[%s] name [%s]", mongoDBTestEnvironment.getName(), name);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDBTestEnvironment.connectionString()))
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test-db");
            MongoCollection<Document> collection = database.getCollection("users");
            Document document = new Document("name", name).append("age", 30);
            collection.insertOne(document);
        }

        LOGGER.info("[%s] name [%s] inserted", mongoDBTestEnvironment.getName(), name);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testQuery(MongoDBTestEnvironment mongoDBTestEnvironment) {
        LOGGER.info("[%s] testing testQuery() ...", mongoDBTestEnvironment.getName());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDBTestEnvironment.connectionString()))
                .build();

        String name = nameThreadLocal.get();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test-db");
            MongoCollection<Document> collection = database.getCollection("users");
            Document query = new Document("name", name);
            Document result = collection.find(query).first();
            assertThat(result).isNotNull();
            assertThat(result.get("name")).isEqualTo(name);
            assertThat(result.get("age")).isEqualTo(30);
        }
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(MongoDBTestEnvironment mongoDBTestEnvironment) throws Throwable {
        LOGGER.info("[%s] destroy test environment ...", mongoDBTestEnvironment.getName());

        List<Trap> traps = new ArrayList<>();

        traps.add(new Trap(mongoDBTestEnvironment::destroy));
        traps.add(new Trap(() -> ofNullable(networkThreadLocal.get()).ifPresent(Network::close)));
        traps.add(new Trap(nameThreadLocal::remove));
        traps.add(new Trap(networkThreadLocal::remove));

        Trap.assertEmpty(traps);
    }
}
