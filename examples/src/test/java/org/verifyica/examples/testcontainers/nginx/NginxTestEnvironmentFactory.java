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

package org.verifyica.examples.testcontainers.nginx;

import java.util.stream.Stream;

/** Class to implement NginxTestEnvironmentFactory */
public class NginxTestEnvironmentFactory {

    /** Constructor */
    private NginxTestEnvironmentFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a Stream of NginxTestEnvironments
     *
     * @return a Stream of NginxTestEnvironments
     */
    public static Stream<NginxTestEnvironment> createTestEnvironments() {
        return Stream.of(
                new NginxTestEnvironment("nginx:1.24.0"),
                new NginxTestEnvironment("nginx:1.25.2"),
                new NginxTestEnvironment("nginx:1.26.2"),
                new NginxTestEnvironment("nginx:1.27.2"));
    }
}
