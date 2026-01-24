/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.examples.testcontainers.mongodb;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Class to implement a MongoDBTestEnvironment
 */
public class MongoDBTestEnvironment implements Argument<MongoDBTestEnvironment> {

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
     * Method to get the payload
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
        // info("initializing test environment [%s] ...", dockerImageName);

        mongoDBContainer = new MongoDBContainer(DockerImageName.parse(dockerImageName))
                .withNetwork(network)
                .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                .withStartupTimeout(Duration.ofSeconds(30));

        try {
            mongoDBContainer.start();
        } catch (Exception e) {
            mongoDBContainer.stop();
            throw e;
        }

        // info("test environment [%s] initialized", dockerImageName);
    }

    /**
     * Method to determine if the MongoDBTestEnvironment is running
     *
     * @return true if the MongoDBTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return mongoDBContainer.isRunning();
    }

    /**
     * Method to get the MongoDB connection string
     *
     * @return the MongoDB connection string
     */
    public String connectionString() {
        return mongoDBContainer.getConnectionString();
    }

    /**
     * Method to destroy the MongoDBTestEnvironment
     */
    public void destroy() {
        // info("destroying test environment [%s] ...", dockerImageName);

        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
            mongoDBContainer = null;
        }

        // info("test environment [%s] destroyed", dockerImageName);
    }

    /**
     * Method to create a Stream of MongoDBTestEnvironments
     *
     * @return a Stream of MongoDBTestEnvironments
     */
    public static Stream<MongoDBTestEnvironment> createTestEnvironments() throws IOException {
        List<MongoDBTestEnvironment> mongoDBTestEnvironments = new ArrayList<>();

        for (String version : Resource.load(MongoDBTestEnvironment.class, "/docker-images.txt")) {
            mongoDBTestEnvironments.add(new MongoDBTestEnvironment(version));
        }

        return mongoDBTestEnvironments.stream();
    }
}
