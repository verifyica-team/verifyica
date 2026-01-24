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
 * Class to implement a NginxTestEnvironment
 */
public class NginxTestEnvironment implements Argument<NginxTestEnvironment> {

    private final String dockerImageName;
    private NginxContainer<?> nginxContainer;

    /**
     * Constructor
     *
     * @param dockerImageName the name
     */
    public NginxTestEnvironment(String dockerImageName) {
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
    public NginxTestEnvironment getPayload() {
        return this;
    }

    /**
     * Method to initialize the NginxTestEnvironment using a specific network
     *
     * @param network the network
     */
    public void initialize(Network network) {
        // info("initializing test environment [%s] ...", dockerImageName);

        nginxContainer = new NginxContainer<>(DockerImageName.parse(dockerImageName))
                .withNetwork(network)
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
     * Method to determine if the NginxTestEnvironment is running
     *
     * @return true if the NginxTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return nginxContainer.isRunning();
    }

    /**
     * Method to get the NginxContainer
     *
     * @return the NginxContainer
     */
    public NginxContainer<?> getNginxContainer() {
        return nginxContainer;
    }

    /**
     * Method to destroy the NginxTestEnvironment
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
     * Method to create a Stream of NginxTestEnvironments
     *
     * @return a Stream of NginxTestEnvironments
     */
    public static Stream<NginxTestEnvironment> createTestEnvironments() throws IOException {
        List<NginxTestEnvironment> nginxTestEnvironments = new ArrayList<>();

        for (String version : Resource.load("NginxTestEnvironments.txt")) {
            nginxTestEnvironments.add(new NginxTestEnvironment(version));
        }

        return nginxTestEnvironments.stream();
    }
}
