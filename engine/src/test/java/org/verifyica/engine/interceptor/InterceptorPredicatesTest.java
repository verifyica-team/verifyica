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

package org.verifyica.engine.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineInterceptor;
import org.verifyica.api.Verifyica;

@DisplayName("InterceptorPredicates Tests")
public class InterceptorPredicatesTest {

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should match valid engine interceptor")
    public void autowiredEngineInterceptorClassShouldMatchValidEngineInterceptor() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(ValidEngineInterceptor.class))
                .isTrue();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match non-public class")
    public void autowiredEngineInterceptorClassShouldNotMatchNonPublicClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(NonPublicEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match abstract class")
    public void autowiredEngineInterceptorClassShouldNotMatchAbstractClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(AbstractEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match non-engine interceptor")
    public void autowiredEngineInterceptorClassShouldNotMatchNonEngineInterceptor() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(NonEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match disabled class")
    public void autowiredEngineInterceptorClassShouldNotMatchDisabledClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(DisabledEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match non-autowired class")
    public void autowiredEngineInterceptorClassShouldNotMatchNonAutowiredClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(NonAutowiredEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_ENGINE_INTERCEPTOR_CLASS should not match class without default constructor")
    public void autowiredEngineInterceptorClassShouldNotMatchClassWithoutDefaultConstructor() {
        assertThat(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS.test(
                        NoDefaultConstructorEngineInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should match valid class interceptor")
    public void autowiredClassInterceptorClassShouldMatchValidClassInterceptor() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(ValidClassInterceptor.class))
                .isTrue();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match non-public class")
    public void autowiredClassInterceptorClassShouldNotMatchNonPublicClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(NonPublicClassInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match abstract class")
    public void autowiredClassInterceptorClassShouldNotMatchAbstractClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(AbstractClassInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match non-class interceptor")
    public void autowiredClassInterceptorClassShouldNotMatchNonClassInterceptor() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(NonClassInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match disabled class")
    public void autowiredClassInterceptorClassShouldNotMatchDisabledClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(DisabledClassInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match non-autowired class")
    public void autowiredClassInterceptorClassShouldNotMatchNonAutowiredClass() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(NonAutowiredClassInterceptor.class))
                .isFalse();
    }

    @Test
    @DisplayName("AUTOWIRED_CLASS_INTERCEPTOR_CLASS should not match class without default constructor")
    public void autowiredClassInterceptorClassShouldNotMatchClassWithoutDefaultConstructor() {
        assertThat(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS.test(
                        NoDefaultConstructorClassInterceptor.class))
                .isFalse();
    }

    // Valid engine interceptor
    @Verifyica.Autowired
    public static class ValidEngineInterceptor implements EngineInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Non-public engine interceptor
    @Verifyica.Autowired
    static class NonPublicEngineInterceptor implements EngineInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Abstract engine interceptor
    @Verifyica.Autowired
    public abstract static class AbstractEngineInterceptor implements EngineInterceptor {
        // INTENTIONALLY EMPTY
    }

    // Non-engine interceptor
    @Verifyica.Autowired
    public static class NonEngineInterceptor {
        // INTENTIONALLY EMPTY
    }

    // Disabled engine interceptor
    @Verifyica.Autowired
    @Verifyica.Disabled
    public static class DisabledEngineInterceptor implements EngineInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Non-autowired engine interceptor
    public static class NonAutowiredEngineInterceptor implements EngineInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Engine interceptor without default constructor
    @Verifyica.Autowired
    public static class NoDefaultConstructorEngineInterceptor implements EngineInterceptor {

        public NoDefaultConstructorEngineInterceptor(String arg) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Valid class interceptor
    @Verifyica.Autowired
    public static class ValidClassInterceptor implements ClassInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Non-public class interceptor
    @Verifyica.Autowired
    static class NonPublicClassInterceptor implements ClassInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Abstract class interceptor
    @Verifyica.Autowired
    public abstract static class AbstractClassInterceptor implements ClassInterceptor {
        // INTENTIONALLY EMPTY
    }

    // Non-class interceptor
    @Verifyica.Autowired
    public static class NonClassInterceptor {
        // INTENTIONALLY EMPTY
    }

    // Disabled class interceptor
    @Verifyica.Autowired
    @Verifyica.Disabled
    public static class DisabledClassInterceptor implements ClassInterceptor {

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Non-autowired class interceptor
    public static class NonAutowiredClassInterceptor implements ClassInterceptor {
        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }

    // Class interceptor without default constructor
    @Verifyica.Autowired
    public static class NoDefaultConstructorClassInterceptor implements ClassInterceptor {
        public NoDefaultConstructorClassInterceptor(String arg) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void initialize(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }

        @Override
        public void destroy(org.verifyica.api.EngineContext engineContext) {
            // INTENTIONALLY EMPTY
        }
    }
}
