/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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
 * Test environment for MongoDB using Testcontainers.
 */
public class MongoDBTestEnvironment implements Argument<MongoDBTestEnvironment> {

    private final String dockerImageName;
    private MongoDBContainer mongoDBContainer;

    /**
     * Constructs a MongoDBTestEnvironment with the specified Docker image name.
     *
     * @param dockerImageName the Docker image name
     */
    public MongoDBTestEnvironment(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    /**
     * Returns the Docker image name.
     *
     * @return the Docker image name
     */
    @Override
    public String getName() {
        return dockerImageName;
    }

    /**
     * Returns this test environment as the payload.
     *
     * @return this MongoDBTestEnvironment
     */
    @Override
    public MongoDBTestEnvironment getPayload() {
        return this;
    }

    /**
     * Initializes the MongoDBTestEnvironment using the specified network.
     *
     * @param network the Docker network
     */
    public void initialize(Network network) {
        // info("initializing test environment [%s] ...", dockerImageName);

        mongoDBContainer = new MongoDBContainer(DockerImageName.parse(dockerImageName))
                .withNetwork(network)
                .withStartupAttempts(3)
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
     * Returns whether the MongoDBTestEnvironment is running.
     *
     * @return true if the MongoDBTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return mongoDBContainer.isRunning();
    }

    /**
     * Returns the MongoDB connection string.
     *
     * @return the MongoDB connection string
     */
    public String getConnectionString() {
        return mongoDBContainer.getConnectionString();
    }

    /**
     * Destroys the MongoDBTestEnvironment, stopping the container.
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
     * Creates a Stream of MongoDBTestEnvironment instances from the docker-images.txt resource.
     *
     * @return a Stream of MongoDBTestEnvironments
     * @throws IOException if the resource file cannot be read
     */
    public static Stream<MongoDBTestEnvironment> createTestEnvironments() throws IOException {
        List<MongoDBTestEnvironment> mongoDBTestEnvironments = new ArrayList<>();

        for (String version : Resource.load(MongoDBTestEnvironment.class, "/docker-images.txt")) {
            mongoDBTestEnvironments.add(new MongoDBTestEnvironment(version));
        }

        return mongoDBTestEnvironments.stream();
    }
}
