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

package org.verifyica.engine.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.verifyica.api.Verifyica;

@DisplayName("ResolverPredicates Tests")
public class ResolverPredicatesTest {

    @Test
    @DisplayName("CLASS_INTERCEPTOR_SUPPLIER should match valid method")
    public void classInterceptorSupplierShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("classInterceptorSupplier");

        assertThat(ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER.test(method)).isTrue();
    }

    @Test
    @DisplayName("CLASS_INTERCEPTOR_SUPPLIER should not match non-static method")
    public void classInterceptorSupplierShouldNotMatchNonStaticMethod() throws NoSuchMethodException {
        Method method = InvalidClass.class.getMethod("nonStaticSupplier");

        assertThat(ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER.test(method)).isFalse();
    }

    @Test
    @DisplayName("CLASS_INTERCEPTOR_SUPPLIER should not match method with parameters")
    public void classInterceptorSupplierShouldNotMatchMethodWithParameters() throws NoSuchMethodException {
        Method method = InvalidClass.class.getMethod("supplierWithParams", String.class);

        assertThat(ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER.test(method)).isFalse();
    }

    @Test
    @DisplayName("CLASS_INTERCEPTOR_SUPPLIER should not match void method")
    public void classInterceptorSupplierShouldNotMatchVoidMethod() throws NoSuchMethodException {
        Method method = InvalidClass.class.getMethod("voidSupplier");

        assertThat(ResolverPredicates.CLASS_INTERCEPTOR_SUPPLIER.test(method)).isFalse();
    }

    @Test
    @DisplayName("ARGUMENT_SUPPLIER_METHOD should match valid method")
    public void argumentSupplierMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("argumentSupplier");

        assertThat(ResolverPredicates.ARGUMENT_SUPPLIER_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("TEST_METHOD should match valid method")
    public void testMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("testMethod");

        assertThat(ResolverPredicates.TEST_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("TEST_METHOD should not match static method")
    public void testMethodShouldNotMatchStaticMethod() throws NoSuchMethodException {
        Method method = InvalidClass.class.getMethod("staticTestMethod");

        assertThat(ResolverPredicates.TEST_METHOD.test(method)).isFalse();
    }

    @Test
    @DisplayName("TEST_METHOD should not match non-void method")
    public void testMethodShouldNotMatchNonVoidMethod() throws NoSuchMethodException {
        Method method = InvalidClass.class.getMethod("testMethodWithReturn");

        assertThat(ResolverPredicates.TEST_METHOD.test(method)).isFalse();
    }

    @Test
    @DisplayName("PREPARE_METHOD should match valid method")
    public void prepareMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("prepare");

        assertThat(ResolverPredicates.PREPARE_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("BEFORE_ALL_METHOD should match valid method")
    public void beforeAllMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("beforeAll");

        assertThat(ResolverPredicates.BEFORE_ALL_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("BEFORE_EACH_METHOD should match valid method")
    public void beforeEachMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("beforeEach");

        assertThat(ResolverPredicates.BEFORE_EACH_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("AFTER_EACH_METHOD should match valid method")
    public void afterEachMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("afterEach");

        assertThat(ResolverPredicates.AFTER_EACH_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("AFTER_ALL_METHOD should match valid method")
    public void afterAllMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("afterAll");

        assertThat(ResolverPredicates.AFTER_ALL_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("CONCLUDE_METHOD should match valid method")
    public void concludeMethodShouldMatchValidMethod() throws NoSuchMethodException {
        Method method = ValidClass.class.getMethod("conclude");

        assertThat(ResolverPredicates.CONCLUDE_METHOD.test(method)).isTrue();
    }

    @Test
    @DisplayName("TEST_CLASS should match valid test class")
    public void testClassShouldMatchValidTestClass() {
        assertThat(ResolverPredicates.TEST_CLASS.test(ValidTestClass.class)).isTrue();
    }

    @Test
    @DisplayName("TEST_CLASS should not match abstract class")
    public void testClassShouldNotMatchAbstractClass() {
        assertThat(ResolverPredicates.TEST_CLASS.test(AbstractTestClass.class)).isFalse();
    }

    @Test
    @DisplayName("TEST_CLASS should not match disabled class")
    public void testClassShouldNotMatchDisabledClass() {
        assertThat(ResolverPredicates.TEST_CLASS.test(DisabledTestClass.class)).isFalse();
    }

    @Test
    @DisplayName("TEST_CLASS should not match class without default constructor")
    public void testClassShouldNotMatchClassWithoutDefaultConstructor() {
        assertThat(ResolverPredicates.TEST_CLASS.test(NoDefaultConstructorClass.class))
                .isFalse();
    }

    // Test classes

    public static class ValidClass {
        
        @Verifyica.ClassInterceptorSupplier
        public static Object classInterceptorSupplier() {
            return null;
        }

        @Verifyica.ArgumentSupplier
        public static Object argumentSupplier() {
            return null;
        }

        @Verifyica.Test
        public void testMethod() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.Prepare
        public void prepare() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.BeforeAll
        public void beforeAll() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.BeforeEach
        public void beforeEach() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.AfterEach
        public void afterEach() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.AfterAll
        public void afterAll() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.Conclude
        public void conclude() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class InvalidClass {

        @Verifyica.ClassInterceptorSupplier
        public Object nonStaticSupplier() {
            return null;
        }

        @Verifyica.ClassInterceptorSupplier
        public static Object supplierWithParams(String param) {
            return null;
        }

        @Verifyica.ClassInterceptorSupplier
        public static void voidSupplier() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.Test
        public static void staticTestMethod() {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.Test
        public String testMethodWithReturn() {
            return "";
        }
    }

    public static class ValidTestClass {

        @Verifyica.ArgumentSupplier
        public static Object argumentSupplier() {
            return null;
        }

        @Verifyica.Test
        public void testMethod() {
            // INTENTIONALLY EMPTY
        }
    }

    public abstract static class AbstractTestClass {

        @Verifyica.ArgumentSupplier
        public static Object argumentSupplier() {
            return null;
        }

        @Verifyica.Test
        public void testMethod() {
            // INTENTIONALLY EMPTY
        }
    }

    @Verifyica.Disabled
    public static class DisabledTestClass {

        @Verifyica.ArgumentSupplier
        public static Object argumentSupplier() {
            return null;
        }

        @Verifyica.Test
        public void testMethod() {
            // INTENTIONALLY EMPTY
        }
    }

    public static class NoDefaultConstructorClass {

        @SuppressWarnings("PMD.UnusedFormalParameter")
        public NoDefaultConstructorClass(String arg) {
            // INTENTIONALLY EMPTY
        }

        @Verifyica.ArgumentSupplier
        public static Object argumentSupplier() {
            return null;
        }

        @Verifyica.Test
        public void testMethod() {
            // INTENTIONALLY EMPTY
        }
    }
}
