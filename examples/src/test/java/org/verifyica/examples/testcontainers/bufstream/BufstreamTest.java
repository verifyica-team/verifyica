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
import org.verifyica.api.CleanupPlan;
import org.verifyica.api.RandomUtil;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
// @Verifyica.Disabled
public class BufstreamTest {

    private static final Logger LOGGER = Logger.createLogger(BufstreamTest.class);

    private static final String TOPIC = "test";
    private static final String GROUP_ID = "test-group-id";
    private static final String EARLIEST = "earliest";

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> messageThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<BufstreamTestEnvironment> arguments() throws IOException {
        return BufstreamTestEnvironment.createTestEnvironments();
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(BufstreamTestEnvironment bufstreamTestEnvironment) throws IOException {
        LOGGER.info("[%s] initialize test environment ...", bufstreamTestEnvironment.name());

        Network network = Network.newNetwork();
        network.getId();

        networkThreadLocal.set(network);
        bufstreamTestEnvironment.initialize(network);

        assertThat(bufstreamTestEnvironment.isRunning()).isTrue();

        LOGGER.info("bootstrap servers: %s", bufstreamTestEnvironment.bootstrapServers());
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testProduce(BufstreamTestEnvironment bufstreamTestEnvironment)
            throws ExecutionException, InterruptedException {
        LOGGER.info("[%s] testing testProduce() ...", bufstreamTestEnvironment.name());

        String message = RandomUtil.alphaString(16);
        LOGGER.info("[%s] producing message [%s] ...", bufstreamTestEnvironment.name(), message);

        messageThreadLocal.set(message);

        try (KafkaProducer<String, String> producer = createKafkaProducer(bufstreamTestEnvironment)) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, message);
            producer.send(producerRecord).get();
        }

        LOGGER.info("[%s] message [%s] produced", bufstreamTestEnvironment.name(), message);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testConsume1(BufstreamTestEnvironment bufstreamTestEnvironment) {
        LOGGER.info("[%s] testing testConsume1() ...", bufstreamTestEnvironment.name());
        LOGGER.info("[%s] consuming message ...", bufstreamTestEnvironment.name());

        String expectedMessage = messageThreadLocal.get();
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(bufstreamTestEnvironment)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info("[%s] consumed message [%s]", bufstreamTestEnvironment.name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testConsume2(BufstreamTestEnvironment bufstreamTestEnvironment) {
        LOGGER.info("[%s] testing testConsume2() ...", bufstreamTestEnvironment.name());
        LOGGER.info("[%s] consuming message ...", bufstreamTestEnvironment.name());

        String expectedMessage = messageThreadLocal.get();
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(bufstreamTestEnvironment)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(5_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info("[%s] consumed message [%s]", bufstreamTestEnvironment.name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isFalse();
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(BufstreamTestEnvironment bufstreamTestEnvironment) throws Throwable {
        LOGGER.info("[%s] destroy test environment ...", bufstreamTestEnvironment.name());

        new CleanupPlan()
                .addAction(bufstreamTestEnvironment::destroy)
                .addActionIfPresent(networkThreadLocal::get, Network::close)
                .addAction(networkThreadLocal::remove)
                .addAction(messageThreadLocal::remove)
                .verify();
    }

    /**
     * Method to create a KafkaProducer
     *
     * @param bufstreamTestEnvironment bufstreamTestEnvironment
     * @return a KafkaProducer
     */
    private static KafkaProducer<String, String> createKafkaProducer(
            BufstreamTestEnvironment bufstreamTestEnvironment) {
        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bufstreamTestEnvironment.bootstrapServers());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    /**
     * Method to create a KafkaConsumer
     *
     * @param bufstreamTestEnvironment bufstreamTestEnvironment
     * @return a KafkaConsumer
     */
    private static KafkaConsumer<String, String> createKafkaConsumer(
            BufstreamTestEnvironment bufstreamTestEnvironment) {
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bufstreamTestEnvironment.bootstrapServers());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        return new KafkaConsumer<>(properties);
    }
}
