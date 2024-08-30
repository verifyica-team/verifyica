/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.test.testcontainers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testcontainers.containers.Network;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTest {

    private static final String TOPIC = "test";
    private static final String GROUP_ID = "test-group-id";
    private static final String EARLIEST = "earliest";

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<KafkaTestEnvironment> arguments() {
        return Stream.of(
                new KafkaTestEnvironment("apache/kafka:3.7.0"),
                new KafkaTestEnvironment("apache/kafka:3.7.1"),
                new KafkaTestEnvironment("apache/kafka:3.8.0"),
                new KafkaTestEnvironment("apache/kafka-native:3.8.0"));
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(ArgumentContext argumentContext) {
        info("initialize test environment ...");

        Network network = Network.newNetwork();
        network.getId();

        argumentContext.getStore().put("network", network);

        argumentContext
                .getTestArgument(KafkaTestEnvironment.class)
                .getPayload()
                .initialize(network);
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void testProduce(ArgumentContext argumentContext)
            throws ExecutionException, InterruptedException {
        info("testing testProduce() ...");

        KafkaTestEnvironment kafkaTestEnvironment =
                argumentContext.getTestArgument(KafkaTestEnvironment.class).getPayload();

        String bootstrapServers = kafkaTestEnvironment.getKafkaContainer().getBootstrapServers();

        String message = randomString(16);
        argumentContext.getStore().put("message", message);
        info("producing message [%s] to [%s] ...", message, bootstrapServers);

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, message);
            producer.send(producerRecord).get();
        }

        info("message [%s] produced", message);
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void testConsume1(ArgumentContext argumentContext) {
        info("testConsume1() ...");

        KafkaTestEnvironment kafkaTestEnvironment =
                argumentContext.getTestArgument(KafkaTestEnvironment.class).getPayload();

        String bootstrapServers = kafkaTestEnvironment.getKafkaContainer().getBootstrapServers();

        String message = argumentContext.getStore().get("message", String.class);

        info("consuming message from [%s] ...", bootstrapServers);

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        KafkaConsumer<String, String> consumer = null;

        try {
            List<String> topicList = Collections.singletonList(TOPIC);

            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topicList);

            ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                info("consumed message [%s] from [%s]", consumerRecord.value(), bootstrapServers);
                assertThat(consumerRecord.value()).isEqualTo(message);
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    @Verifyica.Test
    @Verifyica.Order(order = 3)
    public void testConsume2(ArgumentContext argumentContext) {
        info("testConsume2() ...");

        KafkaTestEnvironment kafkaTestEnvironment =
                argumentContext.getTestArgument(KafkaTestEnvironment.class).getPayload();

        String bootstrapServers = kafkaTestEnvironment.getKafkaContainer().getBootstrapServers();

        String message = argumentContext.getStore().get("message", String.class);

        info("consuming message from [%s] ...", bootstrapServers);

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        KafkaConsumer<String, String> consumer = null;

        try {
            List<String> topicList = Collections.singletonList(TOPIC);

            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topicList);

            ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                info("consumed message [%s] from [%s]", consumerRecord.value(), bootstrapServers);
                assertThat(consumerRecord.value()).isEqualTo(message);
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(ArgumentContext argumentContext) {
        info("destroy test environment ...");

        argumentContext.getTestArgument(KafkaTestEnvironment.class).getPayload().destroy();

        argumentContext
                .getStore()
                .removeOptional("network", Network.class)
                .ifPresent(Network::close);
    }

    /** Class to implement a TestContext */
    public static class KafkaTestEnvironment implements Argument<KafkaTestEnvironment> {

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
                    new KafkaContainer(
                            DockerImageName.parse(dockerImageName)
                                    .asCompatibleSubstituteFor("apache/kafka"));
            kafkaContainer.withNetwork(network);
            kafkaContainer.start();

            info("test environment [%s] initialized", dockerImageName);
        }

        public KafkaContainer getKafkaContainer() {
            return kafkaContainer;
        }

        /** Method to destroy the KafkaTestEnvironment */
        public void destroy() {
            info("destroying test environment [%s] ...", dockerImageName);

            if (kafkaContainer != null) {
                kafkaContainer.stop();
                kafkaContainer = null;
            }

            info("test environment [%s] destroyed", dockerImageName);
        }
    }

    /**
     * Method to create a random string
     *
     * @param length length
     * @return a random String
     */
    private static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
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
