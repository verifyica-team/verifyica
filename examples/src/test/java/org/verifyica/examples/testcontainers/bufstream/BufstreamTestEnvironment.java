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

package org.verifyica.examples.testcontainers.bufstream;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.support.TextBlock;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Class to implement a BufstreamTestEnvironment
 */
public class BufstreamTestEnvironment implements Argument<BufstreamTestEnvironment> {

    private final String dockerImageName;
    private GenericContainer<?> genericContainer;

    /**
     * Constructor
     *
     * @param dockerImageName the name
     */
    public BufstreamTestEnvironment(String dockerImageName) {
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
    public BufstreamTestEnvironment getPayload() {
        return this;
    }

    /**
     * Method to initialize the BufstreamTestEnvironment using a specific network
     *
     * @param network the network
     * @throws IOException if an error occurs
     */
    public void initialize(Network network) throws IOException {
        // info("initialize test environment [%s] ...", dockerImageName);

        for (int i = 0; i < 10; i++) {
            int kafkaPort = getFreePort();
            int adminPort = getFreePort();

            String configuration = buildConfiguration(kafkaPort);

            GenericContainer<?> container = null;

            try {
                container = new GenericContainer<>(DockerImageName.parse(dockerImageName))
                        .withNetwork(network)
                        .withNetworkAliases("bufstream-" + randomId())
                        .withExposedPorts(9092, 9089)
                        .withCopyToContainer(Transferable.of(configuration), "/etc/bufstream.yaml")
                        .withCommand("serve", "--inmemory", "-c", "/etc/bufstream.yaml")
                        .withCreateContainerCmdModifier(
                                createContainerCmd -> bindHostPorts(createContainerCmd, kafkaPort, adminPort))
                        .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                        .withStartupAttempts(3)
                        .waitingFor(Wait.forHttp("/-/status").forPort(9089).forStatusCode(200))
                        .withStartupTimeout(Duration.ofSeconds(30));

                container.start();

                genericContainer = container;

                return;
            } catch (Exception e) {
                safeStop(container);

                if (i < 9) {
                    // try again
                    continue;
                }

                throw e;
            }
        }

        // info("test environment [%s] initialized", dockerImageName);
    }

    /**
     * Method to determine if the BufstreamTestEnvironment is running
     *
     * @return true if the BufstreamTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return genericContainer.isRunning();
    }

    /**
     * Method to get the Bufstream bootstrap servers
     *
     * @return the Bufstream bootstrap servers
     */
    public String getBootstrapServers() {
        return genericContainer.getHost() + ":" + genericContainer.getMappedPort(9092);
    }

    /**
     * Method to destroy the BufstreamTestEnvironment
     */
    public void destroy() {
        // info("destroying test environment [%s] ...", dockerImageName);

        if (genericContainer != null) {
            genericContainer.stop();
            genericContainer = null;
        }

        // info("test environment [%s] destroyed", dockerImageName);
    }

    /**
     * Method to create a Stream of BufstreamTestEnvironment
     *
     * @return a Stream of BufstreamTestEnvironment
     */
    public static Stream<BufstreamTestEnvironment> createTestEnvironments() throws IOException {
        List<BufstreamTestEnvironment> bufstreamTestEnvironments = new ArrayList<>();

        for (String version : Resource.load(BufstreamTestEnvironment.class, "/docker-images.txt")) {
            bufstreamTestEnvironments.add(new BufstreamTestEnvironment(version));
        }

        return bufstreamTestEnvironments.stream();
    }

    /**
     * Method to build the Bufstream configuration
     *
     * @param HOST_KAFKA_PORT the kafka port
     * @return the configuration
     */
    private String buildConfiguration(int HOST_KAFKA_PORT) {
        if (dockerImageName.contains("0.3.")) {
            return new TextBlock()
                    .line("kafka:")
                    .line("  address: 0.0.0.0:9092")
                    .line("  public_address: localhost:" + HOST_KAFKA_PORT)
                    .line("  num_partitions: 1")
                    .line("admin_address: 0.0.0.0:9089")
                    .toString();
        }

        return new TextBlock()
                .line("version: v1beta1")
                .line("cluster: test")
                .line("admin:")
                .line("  listen_address: 0.0.0.0:9089")
                .line("kafka:")
                .line("  listeners:")
                .line("    - name: plain")
                .line("      listen_address: 0.0.0.0:9092")
                .line("      advertise_address: localhost:" + HOST_KAFKA_PORT)
                .line("metadata:")
                .line("  etcd: {}")
                .toString();
    }

    /**
     * Method to bind fixed host ports -> container ports
     *
     * @param createContainerCmd the create container command
     * @param HOST_KAFKA_PORT the kafka port
     * @param HOST_ADMIN_PORT the admin port
     */
    private static void bindHostPorts(CreateContainerCmd createContainerCmd, int HOST_KAFKA_PORT, int HOST_ADMIN_PORT) {
        Ports bindings = new Ports();
        bindings.bind(ExposedPort.tcp(9092), Ports.Binding.bindPort(HOST_KAFKA_PORT));
        bindings.bind(ExposedPort.tcp(9089), Ports.Binding.bindPort(HOST_ADMIN_PORT));

        Objects.requireNonNull(createContainerCmd.getHostConfig()).withPortBindings(bindings);
    }

    /**
     * Method to safely stop a container
     *
     * @param container the container to stop
     */
    private static void safeStop(GenericContainer<?> container) {
        if (container == null) {
            return;
        }

        try {
            container.stop();
        } catch (Exception ignored) {
            // ignore
        }
    }

    /**
     * Method to get a free port on localhost
     *
     * @return a free port on localhost
     * @throws IOException if an I/O error occurs when opening the socket
     */
    private static int getFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            serverSocket.setReuseAddress(true);
            return serverSocket.getLocalPort();
        }
    }

    /**
     * Method to generate a random 8-character string
     *
     * @return a random 8-character string
     */
    private static String randomId() {
        int leftLimit = 97; // 'a'
        int rightLimit = 122; // 'z'
        int targetStringLength = 8;

        return ThreadLocalRandom.current()
                .ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
