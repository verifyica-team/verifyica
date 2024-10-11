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

package org.verifyica.test.assumption;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.Assumptions;
import org.verifyica.api.Verifyica;

@Verifyica.Disabled
public class SkipAllTest {

    @Verifyica.ArgumentSupplier
    public static Object arguments() {
        return "test";
    }

    @Verifyica.Prepare
    public void prepare() {
        // Assumptions.assumeTrue(false);
    }

    @Verifyica.BeforeAll
    public void beforeAll(String argument) {
        // Assumptions.assumeTrue(false);
    }

    @Verifyica.BeforeEach
    public void beforeEach(String argument) {
        Assumptions.assumeTrue(false);
        assertThat(false).withFailMessage("Shouldn't be executed").isTrue();
    }

    @Verifyica.Test
    public void test(String argument) throws Throwable {
        assertThat(false).withFailMessage("Shouldn't be executed").isTrue();
    }

    @Verifyica.AfterEach
    public void afterEach(String argument) {
        // assertThat(false).withFailMessage("Shouldn't be executed").isTrue();
        System.out.println("afterEach()");
    }

    @Verifyica.AfterAll
    public void afterAll(String argument) {
        // assertThat(false).withFailMessage("Shouldn't be executed").isTrue();
        System.out.println("afterAll()");
    }

    @Verifyica.Conclude
    public void conclude() {
        System.out.println("conclude()");
    }
}
