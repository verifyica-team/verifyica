/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.examples.testcontainers;

import static java.lang.String.format;

import org.testcontainers.containers.Network;
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
     * Method to get the payload (ourself)
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
        info("initialize test environment [%s] ...", dockerImageName);

        kafkaContainer =
                new KafkaContainer(DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("apache/kafka"));
        kafkaContainer.withNetwork(network);
        kafkaContainer.start();

        info("test environment [%s] initialized", dockerImageName);
    }

    /**
     * Method to get the KafkaContainer
     *
     * @return the KafkaContainer
     */
    public KafkaContainer getKafkaContainer() {
        return kafkaContainer;
    }

    /**
     * Method to destroy the KafkaTestEnvironment
     */
    public void destroy() {
        info("destroying test environment [%s] ...", dockerImageName);

        if (kafkaContainer != null) {
            kafkaContainer.stop();
            kafkaContainer = null;
        }

        info("test environment [%s] destroyed", dockerImageName);
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
