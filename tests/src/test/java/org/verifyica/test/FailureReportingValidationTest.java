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

package org.verifyica.test;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;
import org.verifyica.test.logger.Logger;
import org.verifyica.test.logger.LoggerFactory;

// Uncomment to test
@Verifyica.Disabled
public class FailureReportingValidationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailureReportingValidationTest.class);

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare() {
        LOGGER.info("prepare()");

        // Uncomment to fail the test class
        // throw new RuntimeException("test1() failed");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        LOGGER.info("beforeAll() argument [%s]", argument);

        // Uncomment to fail a test argument
        /*
        if (argument.contains("1")) {
            throw new RuntimeException("test1() failed");
        }
        */
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        LOGGER.info("beforeEach() argument [%s]", argument);
    }

    @Verifyica.Test
    public void test1(String argument) {
        LOGGER.info("test1() argument [%s]", argument);

        // Uncomment to fail a test
        /*
        if (argument.contains("1")) {
            throw new RuntimeException("test1() failed");
        }
        */
    }

    @Verifyica.Test
    public void test2(String argument) {
        LOGGER.info("test2() argument [%s]", argument);
    }

    @Verifyica.Test
    public void test3(String argument) {
        LOGGER.info("test3() argument [%s]", argument);
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        LOGGER.info("afterEach() argument [%s]", argument);
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        LOGGER.info("afterAll() argument [%s]", argument);
    }

    @Verifyica.Conclude
    public void conclude() {
        LOGGER.info("conclude()");
    }
}
