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

package org.verifyica.test.argument;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class NullArgumentTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return null;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        throw new IllegalStateException("Should not be executed");
    }
}
