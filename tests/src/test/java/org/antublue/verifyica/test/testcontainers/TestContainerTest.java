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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

/** Example */
public class TestContainerTest {

    // The network key
    private static final String NETWORK = "network";

    /* ... using parallelism = 2 to test 2 environments in parallel ... */
    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Collection<Argument<Environment>> arguments() {
        Collection<Argument<Environment>> environments = new ArrayList<>();

        // Create environment 1

        Properties properties = new Properties();
        // ... configure properties for the environment ...
        String name = "Test Environment 1";
        Environment environment = new Environment(properties);

        environments.add(Argument.of(name, environment));

        // Create environment 2

        properties = new Properties();
        // ... configure properties for the environment ...
        name = "Test Environment 2";
        environment = new Environment(properties);

        environments.add(Argument.of(name, environment));

        // Create environment 3

        properties = new Properties();
        // ... configure properties for the environment ...
        name = "Test Environment 3";
        environment = new Environment(properties);

        environments.add(Argument.of(name, environment));

        // ... more environments ...

        return environments;
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] initializing ...");

        // Create the network
        Network network = Network.newNetwork();

        // Get the network ID to force network creation
        network.getId();

        // Store the network in the argument context store
        argumentContext.getStore().put(NETWORK, network);

        // Initialize the environment
        argument.getPayload().initialize(network);

        System.out.println("[" + argument.getName() + "] initialized");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] test1 ...");

        HttpClient httpClient =
                argumentContext.getTestArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] test2 ..");

        HttpClient httpClient =
                argumentContext.getTestArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    // ... more tests

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        Argument<Environment> argument = argumentContext.getTestArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] destroying ...");

        // Destroy the environment
        argument.getPayload().destroy();

        // Remove the network
        argumentContext
                .getStore()
                .removeOptional("network", Network.class)
                .ifPresent(network -> network.close());

        System.out.println("[" + argument.getName() + "] destroyed");
    }

    /** Class to represent an environment */
    public static class Environment {

        private final Properties properties;
        private GenericContainer<?> genericContainer;
        private HttpClient httpClient;

        /**
         * Constructor
         *
         * @param properties properties
         */
        public Environment(Properties properties) {
            this.properties = properties;
        }

        /**
         * Initialize the environment
         *
         * @throws Throwable Throwable
         */
        public void initialize(Network network) throws Throwable {
            // code to initialize the environment / generic container

            // Define the generic container with the network
            this.genericContainer = null;
            // this.genericContainer = new GenericContainer<>("<DOCKER IMAGE
            // NAME>").withNetwork(network);

            // Start the generic container
            // this.genericContainer.start();
        }

        /**
         * Method to get an HttpClient for the environment
         *
         * @return an HttpClient for the environment
         */
        public HttpClient getHttpClient() {
            if (httpClient == null) {
                // Create the HTTP client
                httpClient = new HttpClient(genericContainer);
            }

            return httpClient;
        }

        /** Method to destroy the environment */
        public void destroy() {
            // code to destroy the environment / generic container

            /* ... stop the generic container ... */
            if (genericContainer != null && genericContainer.isRunning()) {
                genericContainer.stop();
                genericContainer = null;
            }
        }
    }

    /** Mock HTTP client */
    public static class HttpClient {

        private final GenericContainer<?> genericContainer;

        /** Constructor */
        public HttpClient(GenericContainer<?> genericContainer) {
            this.genericContainer = genericContainer;

            // ... code to create the HTTP client using the generic container ...
        }

        /**
         * Method to send a GET request
         *
         * @param path path
         * @return the response
         * @throws IOException IOException
         */
        public String doGet(String path) throws IOException {

            // ... code to perform the HTTP GET request ...

            return "success";
        }
    }
}
