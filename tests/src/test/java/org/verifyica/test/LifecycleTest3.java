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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineContext;
import org.verifyica.api.Verifyica;

public class LifecycleTest3 {

    private static final int ARGUMENT_COUNT = 3;

    // Antipattern, but used for testing
    public static List<String> actual = Collections.synchronizedList(new ArrayList<>());

    public String string;

    @Verifyica.ClassInterceptorSupplier
    public static Object classInterceptors() {
        return new ConcreteClassInterceptor();
    }

    @Verifyica.ArgumentSupplier(parallelism = 4)
    public static Collection<AutoClosableArgument> arguments() {
        Collection<AutoClosableArgument> autoClosableArguments = new ArrayList<>();
        for (int i = 0; i < ARGUMENT_COUNT; i++) {
            autoClosableArguments.add(new AutoClosableArgument("argument[" + i + "]"));
        }
        return autoClosableArguments;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.printf("  %s prepare()%n", classContext.getTestClass().getName());

        assertThat(classContext).isNotNull();
        assertThat(classContext.getTestInstance()).isNotNull();

        LifecycleTest3 lifecycleTest1 = classContext.getTestInstance(LifecycleTest3.class);

        assertThat(lifecycleTest1).isNotNull();
        lifecycleTest1.string = "FOO";

        actual.add("prepare");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        System.out.printf(
                "  %s beforeAll()%n", argumentContext.getTestArgument().getPayload());

        actual.add("beforeAll");
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.printf(
                "  %s beforeEach()%n", argumentContext.getTestArgument().getPayload());

        actual.add("beforeEach");
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void test1(ArgumentContext argumentContext) {
        System.out.printf("  %s test1()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test1");
    }

    @Verifyica.Test
    @Verifyica.Order(2)
    public void test2(ArgumentContext argumentContext) {
        System.out.printf("  %s test2()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test2");
    }

    @Verifyica.Test
    @Verifyica.Order(3)
    public void test3(ArgumentContext argumentContext) {
        System.out.printf("  %s test3()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test3");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.printf(
                "  %s afterEach()%n", argumentContext.getTestArgument().getPayload());

        actual.add("afterEach");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.printf("  %s afterAll()%n", argumentContext.getTestArgument().getPayload());

        actual.add("afterAll");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.printf("  %s conclude()%n", classContext.getTestClass().getName());

        assertThat(classContext).isNotNull();
        assertThat(classContext.getTestInstance()).isNotNull();

        LifecycleTest3 lifecycleTest1 = classContext.getTestInstance(LifecycleTest3.class);

        assertThat(lifecycleTest1).isNotNull();
        assertThat(lifecycleTest1.string).isEqualTo("FOO");

        actual.add("conclude");
    }

    public static class ConcreteClassInterceptor implements ClassInterceptor {

        @Override
        public Predicate<ClassContext> predicate() {
            return classContext -> classContext.getTestClass() == LifecycleTest3.class;
        }

        @Override
        public void preInstantiate(EngineContext engineContext, Class<?> testClass) {
            System.out.printf("%s preInstantiate()%n", getClass().getName());

            actual.add("preInstantiate");
        }

        @Override
        public void postInstantiate(
                EngineContext engineContext, Class<?> testClass, Object testInstance, Throwable throwable) {
            System.out.printf("%s postInstantiate()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postInstantiate");
        }

        @Override
        public void prePrepare(ClassContext classContext) {
            System.out.printf("%s prePrepare()%n", getClass().getName());

            assertThat(classContext).isNotNull();

            actual.add("prePrepare");
        }

        @Override
        public void postPrepare(ClassContext classContext, Throwable throwable) {
            System.out.printf("%s postPrepare()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postPrepare");
        }

        @Override
        public void preBeforeAll(ArgumentContext argumentContext) {
            System.out.printf("%s preBeforeAll()%n", getClass().getName());

            actual.add("preBeforeAll");
        }

        @Override
        public void postBeforeAll(ArgumentContext argumentContext, Throwable throwable) {
            System.out.printf("%s postBeforeAll()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postBeforeAll");
        }

        @Override
        public void preBeforeEach(ArgumentContext argumentContext) {
            System.out.printf("%s preBeforeEach()%n", getClass().getName());

            actual.add("preBeforeEach");
        }

        @Override
        public void postBeforeEach(ArgumentContext argumentContext, Throwable throwable) {
            System.out.printf("%s postBeforeEach()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postBeforeEach");
        }

        @Override
        public void preTest(ArgumentContext argumentContext, Method testMethod) {
            System.out.printf("%s preTest()%n", getClass().getName());

            actual.add("preTest");
        }

        @Override
        public void postTest(ArgumentContext argumentContext, Method testMethod, Throwable throwable) {
            System.out.printf("%s postTest()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postTest");
        }

        @Override
        public void preAfterEach(ArgumentContext argumentContext) {
            System.out.printf("%s preAfterEach()%n", getClass().getName());

            actual.add("preAfterEach");
        }

        @Override
        public void postAfterEach(ArgumentContext argumentContext, Throwable throwable) {
            System.out.printf("%s postAfterEach()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postAfterEach");
        }

        @Override
        public void preAfterAll(ArgumentContext argumentContext) {
            System.out.printf("%s preAfterAll()%n", getClass().getName());

            actual.add("preAfterAll");
        }

        @Override
        public void postAfterAll(ArgumentContext argumentContext, Throwable throwable) {
            System.out.printf("%s postAfterAll()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postAfterAll");
        }

        @Override
        public void preConclude(ClassContext classContext) {
            System.out.printf("%s preConclude()%n", getClass().getName());

            actual.add("preConclude");
        }

        @Override
        public void postConclude(ClassContext classContext, Throwable throwable) {
            System.out.printf("%s postConclude()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postConclude");
        }

        @Override
        public void onDestroy(ClassContext classContext) {
            System.out.printf("%s onDestroy()%n", getClass().getName());

            actual.add("onDestroy");

            // Prepare methods should be executed 1 time
            assertThat(actual.stream().filter(s -> s.equals("prePrepare")).collect(Collectors.toList()))
                    .hasSize(1);
            assertThat(actual.stream().filter(s -> s.equals("prepare")).collect(Collectors.toList()))
                    .hasSize(1);
            assertThat(actual.stream().filter(s -> s.equals("postPrepare")).collect(Collectors.toList()))
                    .hasSize(1);

            // BeforeAll methods should be executed 3 test arguments = 3 times
            assertThat(actual.stream().filter(s -> s.equals("preBeforeAll")).collect(Collectors.toList()))
                    .hasSize(3);
            assertThat(actual.stream().filter(s -> s.equals("beforeAll")).collect(Collectors.toList()))
                    .hasSize(3);
            assertThat(actual.stream().filter(s -> s.equals("postBeforeAll")).collect(Collectors.toList()))
                    .hasSize(3);

            // BeforeEach methods should be executed 3 x test arguments x 3 test methods = 9 times
            assertThat(actual.stream().filter(s -> s.equals("preBeforeEach")).collect(Collectors.toList()))
                    .hasSize(9);
            assertThat(actual.stream().filter(s -> s.equals("beforeEach")).collect(Collectors.toList()))
                    .hasSize(9);
            assertThat(actual.stream().filter(s -> s.equals("postBeforeEach")).collect(Collectors.toList()))
                    .hasSize(9);

            // PreTest methods should be executed 3 x test arguments x 3 test methods = 9 times
            assertThat(actual.stream().filter(s -> s.equals("preTest")).collect(Collectors.toList()))
                    .hasSize(9);

            // Test1 methods should executed 3 x test arguments x 1 test1 method = 3 times
            assertThat(actual.stream().filter(s -> s.equals("test1")).collect(Collectors.toList()))
                    .hasSize(3);

            // Test2 methods should executed 3 x test arguments x 1 test1 method = 3 times
            assertThat(actual.stream().filter(s -> s.equals("test2")).collect(Collectors.toList()))
                    .hasSize(3);

            // Test3 methods should executed 3 x test arguments x 1 test1 method = 3 times
            assertThat(actual.stream().filter(s -> s.equals("test3")).collect(Collectors.toList()))
                    .hasSize(3);

            // PostTest methods should be executed 3 x test arguments x 3 test methods = 9 times
            assertThat(actual.stream().filter(s -> s.equals("postTest")).collect(Collectors.toList()))
                    .hasSize(9);

            // AfterEach methods should be executed 3 x test arguments x 3 test methods = 9 times
            assertThat(actual.stream().filter(s -> s.equals("preAfterEach")).collect(Collectors.toList()))
                    .hasSize(9);
            assertThat(actual.stream().filter(s -> s.equals("afterEach")).collect(Collectors.toList()))
                    .hasSize(9);
            assertThat(actual.stream().filter(s -> s.equals("postAfterEach")).collect(Collectors.toList()))
                    .hasSize(9);

            // AfterAll methods should be executed 3 test arguments = 3 times
            assertThat(actual.stream().filter(s -> s.equals("postAfterAll")).collect(Collectors.toList()))
                    .hasSize(3);
            assertThat(actual.stream().filter(s -> s.equals("afterAll")).collect(Collectors.toList()))
                    .hasSize(3);
            assertThat(actual.stream().filter(s -> s.equals("postAfterAll")).collect(Collectors.toList()))
                    .hasSize(3);

            // Conclude methods should be executed 1 time
            assertThat(actual.stream().filter(s -> s.equals("preConclude")).collect(Collectors.toList()))
                    .hasSize(1);
            assertThat(actual.stream().filter(s -> s.equals("conclude")).collect(Collectors.toList()))
                    .hasSize(1);
            assertThat(actual.stream().filter(s -> s.equals("postConclude")).collect(Collectors.toList()))
                    .hasSize(1);

            // OnDestroy method should be executed 1 time
            assertThat(actual.stream().filter(s -> s.equals("onDestroy")).collect(Collectors.toList()))
                    .hasSize(1);
        }
    }

    public static class AutoClosableArgument implements Argument<String>, AutoCloseable {

        private final String value;

        public AutoClosableArgument(String value) {
            this.value = value;
        }

        @Override
        public String getName() {
            return value;
        }

        @Override
        public String getPayload() {
            return value;
        }

        @Override
        public void close() {
            System.out.println("argumentClose()");
            actual.add("argumentClose");
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
