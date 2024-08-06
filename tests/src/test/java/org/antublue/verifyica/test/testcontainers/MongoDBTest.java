/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.test.testcontainers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Random;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.bson.Document;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/** Example using testcontainers-java and MongoDB */
public class MongoDBTest {

    public static final String NETWORK = "network";

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Stream<MongoDBTestEnvironment> arguments() {
        return Stream.of(
                new MongoDBTestEnvironment("mongo:4.4"),
                new MongoDBTestEnvironment("mongo:5.0"),
                new MongoDBTestEnvironment("mongo:6.0"),
                new MongoDBTestEnvironment("mongo:7.0"));
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(ArgumentContext argumentContext) {
        info("initialize test environment ...");

        Network network = Network.newNetwork();
        network.getId();

        argumentContext.getStore().put(NETWORK, network);
        argumentContext
                .getTestArgument(MongoDBTestEnvironment.class)
                .getPayload()
                .initialize(network);
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void testInsert(ArgumentContext argumentContext) {
        info("testing testInsert() ...");

        String name = randomString(16);
        argumentContext.getStore().put("name", name);
        info("name [%s]", name);

        MongoDBTestEnvironment mongoDBTestEnvironment =
                argumentContext.getTestArgument(MongoDBTestEnvironment.class).getPayload();

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(
                                        mongoDBTestEnvironment
                                                .getMongoDBContainer()
                                                .getConnectionString()))
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
    @Verifyica.Order(order = 2)
    public void testQuery(ArgumentContext argumentContext) {
        info("testing testQuery() ...");

        String name = argumentContext.getStore().getOptional("name", String.class).get();
        info("name [%s]", name);

        MongoDBTestEnvironment mongoDBTestEnvironment =
                argumentContext.getTestArgument(MongoDBTestEnvironment.class).getPayload();

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(
                                        mongoDBTestEnvironment
                                                .getMongoDBContainer()
                                                .getConnectionString()))
                        .build();

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
    public void destroyTestEnvironment(ArgumentContext argumentContext) {
        info("destroy test environment ...");

        argumentContext.getTestArgument(MongoDBTestEnvironment.class).getPayload().destroy();

        argumentContext
                .getStore()
                .removeOptional("network", Network.class)
                .ifPresent(Network::close);
    }

    /** Class to implement a TestContext */
    public static class MongoDBTestEnvironment implements Argument<MongoDBTestEnvironment> {

        private final String dockerImageName;
        private MongoDBContainer mongoDBContainer;

        /**
         * Constructor
         *
         * @param dockerImageName the name
         */
        public MongoDBTestEnvironment(String dockerImageName) {
            this.dockerImageName = dockerImageName;
        }

        /**
         * Method to get the name
         *
         * @return the name
         */
        @Override
        public String getName() {
            return dockerImageName;
        }

        /**
         * Method to get the payload (ourself)
         *
         * @return the payload
         */
        @Override
        public MongoDBTestEnvironment getPayload() {
            return this;
        }

        /**
         * Method to initialize the MongoDBTestEnvironment using a specific network
         *
         * @param network the network
         */
        public void initialize(Network network) {
            info("initializing test environment [%s] ...", dockerImageName);

            mongoDBContainer = new MongoDBContainer(DockerImageName.parse(dockerImageName));
            mongoDBContainer.withNetwork(network);
            mongoDBContainer.start();

            info("test environment [%s] initialized", dockerImageName);
        }

        public MongoDBContainer getMongoDBContainer() {
            return mongoDBContainer;
        }

        /** Method to destroy the MongoDBTestEnvironment */
        public void destroy() {
            info("destroying test environment [%s] ...", dockerImageName);

            if (mongoDBContainer != null) {
                mongoDBContainer.stop();
                mongoDBContainer = null;
            }

            info("test environment [%s] destroyed", dockerImageName);
        }
    }

    /**
     * Method to create a random string
     *
     * @param length length
     * @return a random String
     */
    private static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
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
     * Metod to print an info print
     *
     * @param format format
     * @param objects objects
     */
    private static void info(String format, Object... objects) {
        info(format(format, objects));
    }
}
