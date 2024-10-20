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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.bson.Document;
import org.testcontainers.containers.Network;
import org.verifyica.api.ExtendedMap;
import org.verifyica.api.Trap;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.RandomSupport;

public class MongoDBTest4 {

    private static final String NETWORK = "network";
    private static final String NAME = "name";

    private final ThreadLocal<ExtendedMap<String, Object>> extendedMapThreadLocal =
            ThreadLocal.withInitial(ExtendedMap::new);

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
        info("initialize test environment ...");

        Network network = Network.newNetwork();
        network.getId();

        extendedMapThreadLocal.get().put(NETWORK, network);
        mongoDBTestEnvironment.initialize(network);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testInsert(MongoDBTestEnvironment mongoDBTestEnvironment) {
        info("testing testInsert() ...");

        String name = RandomSupport.randomString(16);
        extendedMapThreadLocal.get().put(NAME, name);

        info("name [%s]", name);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(
                        mongoDBTestEnvironment.getMongoDBContainer().getConnectionString()))
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test-db");
            MongoCollection<Document> collection = database.getCollection("users");
            Document document = new Document("name", name).append("age", 30);
            collection.insertOne(document);
        }

        info("name [%s] inserted", name);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testQuery(MongoDBTestEnvironment mongoDBTestEnvironment) {
        info("testing testQuery() ...");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(
                        mongoDBTestEnvironment.getMongoDBContainer().getConnectionString()))
                .build();

        String name = extendedMapThreadLocal.get().getAs(NAME, String.class);

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
        info("destroy test environment ...");

        List<Trap> traps = new ArrayList<>();

        traps.add(
                new Trap(() -> Optional.ofNullable(mongoDBTestEnvironment).ifPresent(MongoDBTestEnvironment::destroy)));
        traps.add(
                new Trap(() -> Optional.ofNullable(extendedMapThreadLocal.get().getAs(NETWORK, Network.class))
                        .ifPresent(Network::close)));
        traps.add(new Trap(() -> extendedMapThreadLocal.get().clear()));
        traps.add(new Trap(extendedMapThreadLocal::remove));

        Trap.assertEmpty(traps);
    }

    /**
     * Method to print an info print
     *
     * @param object object
     */
    private static void info(Object object) {
        System.out.println(object);
    }

    /**
     * Method to print an info print
     *
     * @param format format
     * @param objects objects
     */
    private static void info(String format, Object... objects) {
        info(format(format, objects));
    }
}
