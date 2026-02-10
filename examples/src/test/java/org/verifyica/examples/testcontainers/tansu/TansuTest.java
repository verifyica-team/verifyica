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
import org.verifyica.api.CleanupPlan;
import org.verifyica.api.RandomSupport;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
public class TansuTest {

    private static final Logger LOGGER = Logger.createLogger(TansuTest.class);

    private static final String TOPIC = "test";
    private static final String GROUP_ID = "test-group-id";
    private static final String EARLIEST = "earliest";

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> messageThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<TansuTestEnvironment> arguments() throws IOException {
        return TansuTestEnvironment.createTestEnvironments();
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(TansuTestEnvironment tansuTestEnvironment)
            throws ExecutionException, InterruptedException, IOException {
        LOGGER.info("[%s] initialize test environment ...", tansuTestEnvironment.name());

        Network network = Network.newNetwork();
        network.getId();

        networkThreadLocal.set(network);
        tansuTestEnvironment.initialize(network);

        assertThat(tansuTestEnvironment.isRunning()).isTrue();

        LOGGER.info("bootstrap servers: %s", tansuTestEnvironment.bootstrapServers());

        tansuTestEnvironment.createTopic(TOPIC);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testProduce(TansuTestEnvironment tansuTestEnvironment) throws ExecutionException, InterruptedException {
        LOGGER.info("[%s] testing testProduce() ...", tansuTestEnvironment.name());

        String message = RandomSupport.alphaString(16);
        LOGGER.info("[%s] producing message [%s] ...", tansuTestEnvironment.name(), message);

        messageThreadLocal.set(message);

        try (KafkaProducer<String, String> producer = createKafkaProducer(tansuTestEnvironment)) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, message);
            producer.send(producerRecord).get();
        }

        LOGGER.info("[%s] message [%s] produced", tansuTestEnvironment.name(), message);
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void testConsume1(TansuTestEnvironment tansuTestEnvironment) {
        LOGGER.info("[%s] testing testConsume1() ...", tansuTestEnvironment.name());
        LOGGER.info("[%s] consuming message ...", tansuTestEnvironment.name());

        String expectedMessage = messageThreadLocal.get();
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(tansuTestEnvironment)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(10_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info("[%s] consumed message [%s]", tansuTestEnvironment.name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void testConsume2(TansuTestEnvironment tansuTestEnvironment) {
        LOGGER.info("[%s] testing testConsume2() ...", tansuTestEnvironment.name());
        LOGGER.info("[%s] consuming message ...", tansuTestEnvironment.name());

        String expectedMessage = messageThreadLocal.get();
        boolean messageMatched = false;

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer(tansuTestEnvironment)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(5_000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                LOGGER.info("[%s] consumed message [%s]", tansuTestEnvironment.name(), consumerRecord.value());
                assertThat(consumerRecord.value()).isEqualTo(expectedMessage);
                messageMatched = true;
            }
        }

        assertThat(messageMatched).isFalse();
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(TansuTestEnvironment tansuTestEnvironment) throws Throwable {
        LOGGER.info("[%s] destroy test environment ...", tansuTestEnvironment.name());

        new CleanupPlan()
                .addAction(tansuTestEnvironment::destroy)
                .addActionIfPresent(networkThreadLocal::get, Network::close)
                .addAction(networkThreadLocal::remove)
                .addAction(messageThreadLocal::remove)
                .verify();
    }

    /**
     * Method to create a KafkaProducer
     *
     * @param tansuTestEnvironment tansuTestEnvironment
     * @return a KafkaProducer
     */
    private static KafkaProducer<String, String> createKafkaProducer(TansuTestEnvironment tansuTestEnvironment) {
        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, tansuTestEnvironment.bootstrapServers());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    /**
     * Method to create a KafkaConsumer
     *
     * @param tansuTestEnvironment tansuTestEnvironment
     * @return a KafkaConsumer
     */
    private static KafkaConsumer<String, String> createKafkaConsumer(TansuTestEnvironment tansuTestEnvironment) {
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, tansuTestEnvironment.bootstrapServers());
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        properties.setProperty(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "true");

        return new KafkaConsumer<>(properties);
    }
}
