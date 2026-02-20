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

package org.verifyica.examples.testcontainers.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Class to implement a KafkaTestEnvironment
 */
public class KafkaTestEnvironment implements Argument<KafkaTestEnvironment> {

    private final String dockerImageName;
    private KafkaContainer kafkaContainer;

    /**
     * Constructor
     *
     * @param dockerImageName the name
     */
    public KafkaTestEnvironment(String dockerImageName) {
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
    public KafkaTestEnvironment getPayload() {
        return this;
    }

    /**
     * Method to initialize the KafkaTestEnvironment using a specific network
     *
     * @param network the network
     * @throws Exception if initialization of the KafkaTestEnvironment fails
     */
    public void initialize(Network network) throws Exception {
        final DockerImageName image = DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("apache/kafka");

        kafkaContainer = new KafkaContainer(image)
                .withNetwork(network)
                .withStartupAttempts(3)
                .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

        /*
         * Workaround for Kafka 3.9.0 issue with listeners
         *
         * https://github.com/testcontainers/testcontainers-java/issues/9506
         * https://issues.apache.org/jira/browse/KAFKA-18281
         */
        if (dockerImageName.contains("3.9.0")) {
            kafkaContainer.withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");
        }

        try {
            kafkaContainer.start();

            // Hard readiness gate: Kafka responds to AdminClient metadata calls.
            awaitKafkaReady(kafkaContainer.getBootstrapServers(), Duration.ofMinutes(2));
        } catch (Exception e) {
            // On flake, logs are the difference between guessing and knowing.
            try {
                System.err.println("Kafka container logs (startup failure):\n" + kafkaContainer.getLogs());
            } catch (Exception ignored) {
                // ignore log retrieval issues
            }
            try {
                kafkaContainer.stop();
            } catch (Exception ignored) {
                // ignore cleanup issues
            }
            throw e;
        }
    }

    /**
     * Awaits for Kafka readiness by polling AdminClient metadata calls until success or timeout
     *
     * @param bootstrapServers the bootstrap servers
     * @param timeout          the timeout duration
     * @throws Exception if Kafka does not become ready within the timeout
     */
    private static void awaitKafkaReady(String bootstrapServers, Duration timeout) throws Exception {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("request.timeout.ms", "5000");
        properties.put("default.api.timeout.ms", "5000");

        ListTopicsOptions listTopicsOptions =
                new ListTopicsOptions().timeoutMs(5000).listInternal(true);

        Exception exception = null;

        try (AdminClient adminClient = AdminClient.create(properties)) {
            while (System.nanoTime() < deadlineNanos) {
                try {
                    // describeCluster() proves the broker can answer metadata.
                    adminClient.describeCluster().nodes().get(5, TimeUnit.SECONDS);

                    // listTopics() tends to flush out “not fully ready” states in some setups.
                    adminClient.listTopics(listTopicsOptions).names().get(5, TimeUnit.SECONDS);

                    return; // READY
                } catch (Exception e) {
                    exception = e;

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
        }

        // If we timed out, throw a meaningful error including the last observed failure.
        if (exception != null) {
            throw new IllegalStateException(
                    "Kafka did not become ready within " + timeout + " (bootstrap.servers=" + bootstrapServers + ")",
                    exception);
        }

        throw new IllegalStateException(
                "Kafka did not become ready within " + timeout + " (bootstrap.servers=" + bootstrapServers + ")");
    }

    /**
     * Method to determine if the KafkaTestEnvironment is running
     *
     * @return true if the KafkaTestEnvironment is running, otherwise false
     */
    public boolean isRunning() {
        return kafkaContainer.isRunning();
    }

    /**
     * Method to get the Kafka bootstrap servers
     *
     * @return the Kafka bootstrap servers
     */
    public String getBootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }

    /**
     * Method to destroy the KafkaTestEnvironment
     */
    public void destroy() {
        // info("destroying test environment [%s] ...", dockerImageName);

        if (kafkaContainer != null) {
            kafkaContainer.stop();
            kafkaContainer = null;
        }

        // info("test environment [%s] destroyed", dockerImageName);
    }

    /**
     * Method to create a Stream of KafkaTestEnvironments
     *
     * @return a Stream of KafkaTestEnvironments
     */
    public static Stream<KafkaTestEnvironment> createTestEnvironments() throws IOException {
        List<KafkaTestEnvironment> kafkaTestEnvironments = new ArrayList<>();

        for (String version : Resource.load(KafkaTestEnvironment.class, "/docker-images.txt")) {
            kafkaTestEnvironments.add(new KafkaTestEnvironment(version));
        }

        return kafkaTestEnvironments.stream();
    }
}
