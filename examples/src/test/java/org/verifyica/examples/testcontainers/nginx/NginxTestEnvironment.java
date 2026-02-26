/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.examples.testcontainers.nginx;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Test environment for Nginx using Testcontainers.
 */
public class NginxTestEnvironment implements Argument<NginxTestEnvironment> {

    private final String dockerImageName;
    private NginxContainer<?> nginxContainer;

    /**
     * Constructs a NginxTestEnvironment with the specified Docker image name.
     *
     * @param dockerImageName the Docker image name
     */
    public NginxTestEnvironment(String dockerImageName) {
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
     * @return this NginxTestEnvironment
     */
    @Override
    public NginxTestEnvironment getPayload() {
        return this;
    }

    /**
     * Initializes the NginxTestEnvironment using the specified network.
     *
     * @param network the Docker network
     */
    public void initialize(Network network) {
        // info("initializing test environment [%s] ...", dockerImageName);

        nginxContainer = new NginxContainer<>(DockerImageName.parse(dockerImageName))
                .withNetwork(network)
                .withStartupAttempts(3)
                .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                .withStartupTimeout(Duration.ofSeconds(30));

        try {
            nginxContainer.start();
        } catch (Exception e) {
            nginxContainer.stop();
            throw e;
        }

        // info("test environment [%s] initialized", dockerImageName);
    }

    /**
     * Returns whether the NginxTestEnvironment is running.
     *
     * @return true if the NginxTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return nginxContainer.isRunning();
    }

    /**
     * Returns the Nginx container.
     *
     * @return the NginxContainer
     */
    public NginxContainer<?> getNginxContainer() {
        return nginxContainer;
    }

    /**
     * Destroys the NginxTestEnvironment, stopping the container.
     */
    public void destroy() {
        // info("destroying test environment [%s] ...", dockerImageName);

        if (nginxContainer != null) {
            nginxContainer.stop();
            nginxContainer = null;
        }

        // info("test environment [%s] destroyed", dockerImageName);
    }

    /**
     * Creates a Stream of NginxTestEnvironment instances from the docker-images.txt resource.
     *
     * @return a Stream of NginxTestEnvironments
     * @throws IOException if the resource file cannot be read
     */
    public static Stream<NginxTestEnvironment> createTestEnvironments() throws IOException {
        List<NginxTestEnvironment> nginxTestEnvironments = new ArrayList<>();

        for (String version : Resource.load(NginxTestEnvironment.class, "/docker-images.txt")) {
            nginxTestEnvironments.add(new NginxTestEnvironment(version));
        }

        return nginxTestEnvironments.stream();
    }
}
