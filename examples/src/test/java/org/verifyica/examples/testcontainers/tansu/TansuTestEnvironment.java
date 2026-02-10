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

package org.verifyica.examples.testcontainers.tansu;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Class to implement a TansuTestEnvironment
 */
public class TansuTestEnvironment implements Argument<TansuTestEnvironment> {

    private final String dockerImageName;
    private GenericContainer<?> genericContainer;
    private int hostKafkaPort;

    /**
     * Constructor
     *
     * @param dockerImageName the name
     */
    public TansuTestEnvironment(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    @Override
    public String getName() {
        return dockerImageName;
    }

    @Override
    public TansuTestEnvironment getPayload() {
        return this;
    }

    /**
     * Method to initialize the TansuTestEnvironment using a specific network
     *
     * @param network the network
     * @throws IOException if an error occurs
     */
    public void initialize(Network network) throws IOException {
        for (int i = 0; i < 10; i++) {
            int kafkaPort = getFreePort();

            GenericContainer<?> container = null;

            try {
                container = new GenericContainer<>(DockerImageName.parse(dockerImageName))
                        .withNetwork(network)
                        // alias is useful for other containers on the same Network,
                        // but host-JVM clients must use localhost:<boundPort>
                        .withNetworkAliases("tansu-" + randomId(), "kafka")
                        .withExposedPorts(9092)
                        // bind fixed host port -> container port 9092
                        .withCreateContainerCmdModifier(
                                createContainerCmd -> bindHostPorts(createContainerCmd, kafkaPort))
                        // ephemeral storage
                        .withEnv("STORAGE_ENGINE", "memory://tansu/")
                        // listen inside container
                        .withEnv("LISTENER_URL", "tcp://0.0.0.0:9092")
                        // CRITICAL: advertise host-reachable endpoint for the Java client running on the host
                        .withEnv("ADVERTISED_LISTENER_URL", "tcp://localhost:" + kafkaPort)
                        .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                        // simplest reliable readiness check
                        .waitingFor(Wait.forListeningPort())
                        .withStartupTimeout(Duration.ofSeconds(60));

                container.start();

                this.genericContainer = container;
                this.hostKafkaPort = kafkaPort;

                return;
            } catch (Exception e) {
                safeStop(container);

                if (i < 9) {
                    // try again with another port
                    continue;
                }

                throw e;
            }
        }
    }

    /**
     * Method to determine if the TansuTestEnvironment is running
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return genericContainer != null && genericContainer.isRunning();
    }

    /**
     * Method to get the Kafka bootstrap servers for Tansu
     *
     * @return bootstrap servers
     */
    public String bootstrapServers() {
        // Use the same host/port that we advertised to avoid metadata/unreachable hangs
        return "localhost:" + hostKafkaPort;
    }

    /**
     * Method to destroy the TansuTestEnvironment
     */
    public void destroy() {
        if (genericContainer != null) {
            genericContainer.stop();
            genericContainer = null;
        }
    }

    /**
     * Method to create a topic in the TansuTestEnvironment
     *
     * @param topic the topic name
     * @throws ExecutionException if an error occurs
     * @throws InterruptedException if an error occurs
     */
    public void createTopic(String topic) throws ExecutionException, InterruptedException {
        Properties p = new Properties();
        p.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers());

        try (AdminClient admin = AdminClient.create(p)) {
            List<NewTopic> topics = new ArrayList<>();
            topics.add(new NewTopic(topic, 1, (short) 1));
            admin.createTopics(topics).all().get();
        } catch (ExecutionException e) {
            // ignore “already exists”
            if (e.getCause() == null || !e.getCause().getClass().getName().contains("TopicExists")) {
                throw e;
            }
        }
    }

    /**
     * Method to create a Stream of TansuTestEnvironments
     *
     * @return a Stream of TansuTestEnvironments
     */
    public static Stream<TansuTestEnvironment> createTestEnvironments() throws IOException {
        List<TansuTestEnvironment> envs = new ArrayList<>();

        for (String version : Resource.load(TansuTestEnvironment.class, "/docker-images.txt")) {
            envs.add(new TansuTestEnvironment(version));
        }

        return envs.stream();
    }

    /**
     * Method to bind fixed host port -> container port 9092
     *
     * @param createContainerCmd the create container command
     * @param HOST_KAFKA_PORT the kafka port
     */
    private static void bindHostPorts(CreateContainerCmd createContainerCmd, int HOST_KAFKA_PORT) {
        Ports bindings = new Ports();
        bindings.bind(ExposedPort.tcp(9092), Ports.Binding.bindPort(HOST_KAFKA_PORT));

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
