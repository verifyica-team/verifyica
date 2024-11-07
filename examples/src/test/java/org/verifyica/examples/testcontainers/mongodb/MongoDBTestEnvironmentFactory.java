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

package org.verifyica.examples.testcontainers.mongodb;

import java.util.stream.Stream;

/** Class to implement MongoDBTestEnvironmentFactory */
public class MongoDBTestEnvironmentFactory {

    /** Constructor */
    private MongoDBTestEnvironmentFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a Stream of MongoDBTestEnvironments
     *
     * @return a Stream of MongoDBTestEnvironments
     */
    public static Stream<MongoDBTestEnvironment> createTestEnvironments() {
        return Stream.of(
                new MongoDBTestEnvironment("mongo:4.4"),
                new MongoDBTestEnvironment("mongo:5.0"),
                new MongoDBTestEnvironment("mongo:6.0"),
                new MongoDBTestEnvironment("mongo:7.0"));
    }
}
