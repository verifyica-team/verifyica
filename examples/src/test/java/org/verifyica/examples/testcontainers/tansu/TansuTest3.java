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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
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
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.util.CleanupExecutor;
import org.verifyica.api.util.RandomUtil;
import org.verifyica.examples.support.Logger;

@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
public class TansuTest3 {

    private static final Logger LOGGER = Logger.createLogger(TansuTest3.class);

    private static final String NETWORK = "network";
    private static final String MESSAGE = "message";

    private static final String TOPIC = "test";
    private static final String GROUP_ID = "test-group-id";
    private static final String EARLIEST = "earliest";

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<TansuTestEnvironment> arguments() throws IOException {
        return TansuTestEnvironment.createTestEnvironments();
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(ArgumentContext argumentContext)
            throws ExecutionException, InterruptedException, IOException {
        LOGGER.info(
                "[%s] initialize test environment ...",
                argumentContext.testArgument().name());

        Network network = Network.newNetwork();
        network.getId();

        argumentContext.map().put(NETWORK, network);
        argumentContext.testArgument().payload(TansuTestEnvironment.class).initialize(network);

        assertThat(argumentContext
                        .testArgument()
                        .payload(TansuTestEnvironment.class)
                        .isRunning())
                .isTrue();

        LOGGER.info(
                "bootstrap servers: %s",
                argumentContext
                        .testArgument()
                        .payload(TansuTestEnvironment.class)
                        .bootstrapServers());

        argumentContext.testArgument().payload(TansuTestEnvironment.class).createTopic(TOPIC);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testProduce(ArgumentContext argumentContext) throws ExecutionException, InterruptedException {
        LOGGER.info(
                "[%s] testing testProduce() ...", argumentContext.testArgument().name());

        String message = RandomUtil.alphaString(16);
        argumentContext.map().put(MESSAGE, message);
        LOGGER.info(
                "[%s] producing message [%s] ...",
                argumentContext.testArgument().name(), message);

        try (KafkaProducer<String, String> producer = createKafkaProducer(argumentContext)) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, message);
            producer.send(producerRecord).get();
        }

        LOGGER.info("[%s] message [%s] produced", argumentContext.testArgument().name(), message);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testConsume1(ArgumentContext argumentContext) {
        LOGGER.info(
                "[%s] testing testConsume1() ...",
                argumentContext.testArgument().name());
        LOGGER.info("[%s] consuming message ...", argumentContext.testArgument().name());

        String expectedMessage = argumentContext.map().getAs(MESSAGE);
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(argumentContext)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info(
                        "[%s] consumed message [%s]",
                        argumentContext.testArgument().name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testConsume2(ArgumentContext argumentContext) {
        LOGGER.info(
                "[%s] testing testConsume2() ...",
                argumentContext.testArgument().name());
        LOGGER.info("[%s] consuming message ...", argumentContext.testArgument().name());

        String expectedMessage = argumentContext.map().getAs(MESSAGE);
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(argumentContext)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info(
                        "[%s] consumed message [%s]",
                        argumentContext.testArgument().name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isFalse();
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(ArgumentContext argumentContext) throws Throwable {
        LOGGER.info(
                "[%s] destroy test environment ...",
                argumentContext.testArgument().name());

        new CleanupExecutor()
                .addTask(() -> argumentContext
                        .testArgument()
                        .payload(TansuTestEnvironment.class)
                        .destroy())
                .addTask(() ->
                        argumentContext.map().removeAs(NETWORK, Network.class).close())
                .addTask(() -> argumentContext.map().clear())
                .throwIfFailed();
    }

    /**
     * Method to create a KafkaProducer
     *
     * @param argumentContext argumentContext
     * @return a KafkaProducer
     */
    private static KafkaProducer<String, String> createKafkaProducer(ArgumentContext argumentContext) {
        Properties properties = new Properties();

        properties.setProperty(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                argumentContext
                        .testArgument()
                        .payload(TansuTestEnvironment.class)
                        .bootstrapServers());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    /**
     * Method to create a KafkaConsumer
     *
     * @param argumentContext argumentContext
     * @return a KafkaConsumer
     */
    private static KafkaConsumer<String, String> createKafkaConsumer(ArgumentContext argumentContext) {
        Properties properties = new Properties();

        properties.setProperty(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                argumentContext
                        .testArgument()
                        .payload(TansuTestEnvironment.class)
                        .bootstrapServers());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        return new KafkaConsumer<>(properties);
    }
}
