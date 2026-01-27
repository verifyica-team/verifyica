/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.examples.simple;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

@Verifyica.Disabled
public class ParallelArgumentTest2 {

    private static final Logger LOGGER = Logger.createLogger(ParallelArgumentTest2.class);

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare() {
        LOGGER.info("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        LOGGER.info("beforeAll() argument [%s]", argument);
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        LOGGER.info("beforeEach() argument [%s]", argument);
    }

    @Verifyica.Test
    public void test1(String argument) {
        LOGGER.info("test1() argument [%s]", argument);
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
