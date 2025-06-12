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

package org.verifyica.examples.testcontainers.kafka;

import java.time.Duration;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;

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
     */
    public void initialize(Network network) {
        // info("initialize test environment [%s] ...", dockerImageName);

        kafkaContainer =
                new KafkaContainer(DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("apache/kafka"));
        kafkaContainer.withNetwork(network);
        kafkaContainer.waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1));

        /*
         * Workaround for Kafka 3.9.0 issue with listeners
         *
         * https://github.com/testcontainers/testcontainers-java/issues/9506
         * https://issues.apache.org/jira/browse/KAFKA-18281
         */
        if (dockerImageName.contains("3.9.0")) {
            kafkaContainer.withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");
        }

        /*
         * Workaround for Kafka 4.0.0:native startup timeout issue
         */
        if (dockerImageName.contains("4.0.0:native")) {
            kafkaContainer.withStartupTimeout(Duration.ofSeconds(120));
        }

        kafkaContainer.start();

        // info("test environment [%s] initialized", dockerImageName);
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
    public String bootstrapServers() {
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
    public static Stream<KafkaTestEnvironment> createTestEnvironments() {
        return Stream.of(
                new KafkaTestEnvironment("apache/kafka:3.7.2"),
                new KafkaTestEnvironment("apache/kafka:3.8.1"),
                new KafkaTestEnvironment("apache/kafka-native:3.8.1"),
                new KafkaTestEnvironment("apache/kafka:3.9.1"),
                new KafkaTestEnvironment("apache/kafka-native:3.9.1"),
                new KafkaTestEnvironment("apache/kafka:4.0.0"),
                new KafkaTestEnvironment("apache/kafka-native:4.0.0"));
    }
}
