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
import java.util.Properties;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

/** Example */
public class EnvironmentTest {

    @Verifyica.ArgumentSupplier
    public static Argument<Environment> arguments() {
        Properties properties = new Properties();

        // ... configure properties for the environment ...

        String name = "Test Environment";
        Environment environment = new Environment(properties);

        return Argument.of(name, environment);
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        argumentContext.getArgument(Environment.class).getPayload().initialize();
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        HttpClient httpClient =
                argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        HttpClient httpClient =
                argumentContext.getArgument(Environment.class).getPayload().getHttpClient();

        // ... test code ...
    }

    // ... more tests

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        argumentContext.getArgument(Environment.class).getPayload().destroy();
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
