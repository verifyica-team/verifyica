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

/** Example */
public class EnvironmentTest {

    @Verifyica.ArgumentSupplier
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

        // ... more environments ...

        return environments;
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] initializing ...");

        argument.getPayload().initialize();

        System.out.println("[" + argument.getName() + "] initialized");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] test1");

        HttpClient httpClient =
                argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        Argument<Environment> argument = argumentContext.getArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] test2");

        HttpClient httpClient =
                argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    // ... more tests

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        Argument<Environment> argument = argumentContext.getArgument(Environment.class);

        System.out.println("[" + argument.getName() + "] destroying ...");

        argument.getPayload().destroy();

        System.out.println("[" + argument.getName() + "] destroyed");
    }

    /** Class to represent an environment */
    public static class Environment {

        private final Properties properties;
        private final HttpClient httpClient;

        /**
         * Constructor
         *
         * @param properties properties
         */
        public Environment(Properties properties) {
            this.properties = properties;
            this.httpClient = new HttpClient(properties);
        }

        /**
         * Initialize the environment
         *
         * @throws Throwable
         */
        public void initialize() throws Throwable {
            // code to initialize the environment
        }

        /**
         * Method to get an HttpClient for the environment
         *
         * @return an HttpClient for the environment
         */
        public HttpClient getHttpClient() {
            return httpClient;
        }

        /** Method to destroy the environment */
        public void destroy() {
            // code to destroy the environment
        }
    }

    /** Mock HTTP client */
    public static class HttpClient {

        private final Properties properties;

        /** Constructor */
        public HttpClient(Properties properties) {
            this.properties = properties;

            // ... code to initialize the HTTP client ...
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
