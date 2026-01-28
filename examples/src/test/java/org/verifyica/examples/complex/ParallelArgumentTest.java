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

package org.verifyica.examples.complex;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

public class ParallelArgumentTest {

    private static final Logger LOGGER = Logger.createLogger(ParallelArgumentTest.class);

    @Verifyica.ArgumentSupplier(parallelism = 2)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("string-" + i);
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        LOGGER.info("prepare()");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        LOGGER.info("beforeAll() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        LOGGER.info("beforeEach() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        LOGGER.info("test1() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        LOGGER.info("test2() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        LOGGER.info("test3() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        LOGGER.info("afterEach() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        LOGGER.info("afterAll() argument [%s]", argumentContext.getTestArgument());
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        LOGGER.info("conclude()");
    }
}
