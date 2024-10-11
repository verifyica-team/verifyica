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

package org.verifyica.test;

import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class EmptyArgumentTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return Argument.EMPTY;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.printf("prepare()%n");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("beforeAll()%n");
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("beforeEach()%n");
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test()%n");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("afterEach()%n");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("afterAll()%n");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.printf("conclude()%n");
    }
}
