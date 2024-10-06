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

package org.verifyica.examples.testcontainers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.api.Runner;
import org.verifyica.api.Verifyica;

public class NginxTest {

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<NginxTestEnvironment> arguments() {
        return Stream.of(
                new NginxTestEnvironment("nginx:1.24.0"),
                new NginxTestEnvironment("nginx:1.25.2"),
                new NginxTestEnvironment("nginx:1.26.2"),
                new NginxTestEnvironment("nginx:1.27.2"));
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(NginxTestEnvironment nginxTestEnvironment) {
        info("initialize test environment ...");

        Network network = Network.newNetwork();
        network.getId();

        nginxTestEnvironment.initialize(network);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testGet(NginxTestEnvironment nginxTestEnvironment) throws Throwable {
        info("testing testGet() ...");

        int port = nginxTestEnvironment.getNginxContainer().getMappedPort(80);
        String content = doGet("http://localhost:" + port);

        assertThat(content).contains("Welcome to nginx!");
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(NginxTestEnvironment nginxTestEnvironment) throws Throwable {
        info("destroy test environment ...");

        new Runner()
                .perform(
                        () -> Optional.ofNullable(nginxTestEnvironment).ifPresent(NginxTestEnvironment::destroy),
                        () -> Optional.ofNullable(networkThreadLocal.get()).ifPresent(Network::close),
                        networkThreadLocal::remove)
                .assertSuccessful();
    }

    public static String doGet(String urlString) throws Throwable {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    /** Class to implement a NginxTestEnvironment */
    public static class NginxTestEnvironment implements Argument<NginxTestEnvironment> {

        private final String dockerImageName;
        private NginxContainer<?> nginxContainer;

        /**
         * Constructor
         *
         * @param dockerImageName the name
         */
        public NginxTestEnvironment(String dockerImageName) {
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
        public NginxTestEnvironment getPayload() {
            return this;
        }

        /**
         * Method to initialize the MongoDBTestEnvironment using a specific network
         *
         * @param network the network
         */
        public void initialize(Network network) {
            info("initializing test environment [%s] ...", dockerImageName);

            nginxContainer = new NginxContainer<>(DockerImageName.parse(dockerImageName));
            nginxContainer.withNetwork(network);
            nginxContainer.start();

            info("test environment [%s] initialized", dockerImageName);
        }

        public NginxContainer getNginxContainer() {
            return nginxContainer;
        }

        /** Method to destroy the MongoDBTestEnvironment */
        public void destroy() {
            info("destroying test environment [%s] ...", dockerImageName);

            if (nginxContainer != null) {
                nginxContainer.stop();
                nginxContainer = null;
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
