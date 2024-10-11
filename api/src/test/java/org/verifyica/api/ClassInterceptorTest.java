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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class ClassInterceptorTest {

    @Test
    public void test() {
        RuntimeException runtimeException = new RuntimeException();
        ClassInterceptor classInterceptor = new MockClassInterceptor();

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> classInterceptor.rethrow(runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postInstantiate(null, null, null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postPrepare(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postBeforeAll(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postBeforeEach(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postTest(null, null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postAfterEach(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postAfterAll(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postAfterAll(null, runtimeException));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> classInterceptor.postConclude(null, runtimeException));
    }

    public static class MockClassInterceptor implements ClassInterceptor {

        // INTENTIONALLY BLANK
    }
}
