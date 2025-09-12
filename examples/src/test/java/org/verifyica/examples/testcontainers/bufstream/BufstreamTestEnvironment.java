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

package org.verifyica.examples.testcontainers.bufstream;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.stream.Stream;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.TextBlock;

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
     */
    public void initialize(Network network) {
        // info("initialize test environment [%s] ...", dockerImageName);

        for (int i = 0; i < 10; i++) {
            int HOST_KAFKA_PORT = getFreePort();
            int HOST_ADMIN_PORT = getFreePort();

            String configuration = new TextBlock()
                    .line("kafka:")
                    .line("  address: 0.0.0.0:9092")
                    .line("  public_address: localhost:" + HOST_KAFKA_PORT)
                    .line("  num_partitions: 1")
                    .line("admin_address: 0.0.0.0:9089")
                    .toString();

            try {
                genericContainer = new GenericContainer<>(DockerImageName.parse(dockerImageName))
                        .withNetwork(network)
                        .withNetworkAliases("bufstream")
                        .withExposedPorts(9092, 9089)
                        .withCopyToContainer(Transferable.of(configuration), "/etc/bufstream.yaml")
                        .withCommand("serve", "--inmemory", "-c", "/etc/bufstream.yaml")
                        // bind fixed host ports -> container ports
                        .withCreateContainerCmdModifier(createContainerCmd -> {
                            Ports bindings = new Ports();
                            bindings.bind(ExposedPort.tcp(9092), Ports.Binding.bindPort(HOST_KAFKA_PORT));
                            bindings.bind(ExposedPort.tcp(9089), Ports.Binding.bindPort(HOST_ADMIN_PORT));
                            Objects.requireNonNull(createContainerCmd.getHostConfig())
                                    .withPortBindings(bindings);
                        })
                        .waitingFor(Wait.forHttp("/-/status").forPort(9089).forStatusCode(200));

                genericContainer.start();

                break;
            } catch (Exception e) {
                genericContainer.stop();

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
    public String bootstrapServers() {
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
    public static Stream<BufstreamTestEnvironment> createTestEnvironments() {
        return Stream.of(new BufstreamTestEnvironment("bufbuild/bufstream:0.3.44"));
    }

    /**
     * Method to get a free port on localhost
     *
     * @return a free port on localhost
     */
    private static int getFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            serverSocket.setReuseAddress(true);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
