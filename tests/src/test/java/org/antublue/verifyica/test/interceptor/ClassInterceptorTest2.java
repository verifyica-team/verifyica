/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.test.interceptor;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ArgumentInterceptorContext;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.engine.interceptor.ClassInterceptorRegistry;

/** Example test */
public class ClassInterceptorTest2 {

    static {
        // Directly register a ClassInterceptor
        ClassInterceptorRegistry.getInstance()
                .register(ClassInterceptorTest2.class, new ConcreteClasInterceptor());
    }

    // Anti-pattern, but used for testing
    public static List<String> actual = new ArrayList<>();

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s prepare()", classContext.getTestClass().getName()));

        actual.add("prepare");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeAll()", argumentContext.getTestArgument().getPayload()));

        actual.add("beforeAll");
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeEach()", argumentContext.getTestArgument().getPayload()));

        actual.add("beforeEach");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test1()", argumentContext.getTestArgument().getPayload()));

        actual.add("test1");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test2()", argumentContext.getTestArgument().getPayload()));

        actual.add("test2");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test3()", argumentContext.getTestArgument().getPayload()));

        actual.add("test3");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterEach()", argumentContext.getTestArgument().getPayload()));

        actual.add("afterEach");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterAll()", argumentContext.getTestArgument().getPayload()));

        actual.add("afterAll");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s conclude()", classContext.getTestClass().getName()));

        actual.add("conclude");
    }

    public static class ConcreteClasInterceptor implements ClassInterceptor {

        @Override
        public void preInstantiate(
                EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
                throws Throwable {
            System.out.println(format("%s preInstantiate()", getClass().getName()));

            actual.add("preInstantiate");
        }

        @Override
        public void postInstantiate(
                EngineInterceptorContext engineInterceptorContext,
                Class<?> testClass,
                Object testInstance,
                Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postInstantiate()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postInstantiate");
        }

        @Override
        public void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
            System.out.println(format("%s prePrepare()", getClass().getName()));

            actual.add("prePrepare");
        }

        @Override
        public void postPrepare(
                ClassInterceptorContext classInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postPrepare()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postPrepare");
        }

        @Override
        public void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.println(format("%s preBeforeAll()", getClass().getName()));

            actual.add("preBeforeAll");
        }

        @Override
        public void postBeforeAll(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postBeforeAll()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postBeforeAll");
        }

        @Override
        public void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.println(format("%s preBeforeEach()", getClass().getName()));

            actual.add("preBeforeEach");
        }

        @Override
        public void postBeforeEach(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postBeforeEach()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postBeforeEach");
        }

        @Override
        public void preTest(
                ArgumentInterceptorContext argumentInterceptorContext, Method testMethod)
                throws Throwable {
            System.out.println(format("%s preTest()", getClass().getName()));

            actual.add("preTest");
        }

        @Override
        public void postTest(
                ArgumentInterceptorContext argumentInterceptorContext,
                Method testMethod,
                Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postTest()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postTest");
        }

        @Override
        public void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.println(format("%s preAfterEach()", getClass().getName()));

            actual.add("preAfterEach");
        }

        @Override
        public void postAfterEach(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postAfterEach()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postAfterEach");
        }

        @Override
        public void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.println(format("%s preAfterAll()", getClass().getName()));

            actual.add("preAfterAll");
        }

        @Override
        public void postAfterAll(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postAfterAll()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postAfterAll");
        }

        @Override
        public void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
            System.out.println(format("%s preConclude()", getClass().getName()));

            actual.add("preConclude");
        }

        @Override
        public void postConclude(
                ClassInterceptorContext classInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s postConclude()", getClass().getName()));

            assertThat(throwable).isNull();
            actual.add("postConclude");
        }

        @Override
        public void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
            List<String> expected = new ArrayList<>();

            expected.add("preInstantiate");
            expected.add("postInstantiate");

            String state = "prepare";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            state = "beforeAll";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            for (int i = 1; i <= 3; i++) {
                state = "beforeEach";
                expected.add("pre" + capitalize(state));
                expected.add(state);
                expected.add("post" + capitalize(state));

                state = "test";
                expected.add("pre" + capitalize(state));
                expected.add(state + i);
                expected.add("post" + capitalize(state));

                state = "afterEach";
                expected.add("pre" + capitalize(state));
                expected.add(state);
                expected.add("post" + capitalize(state));
            }

            state = "afterAll";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            state = "conclude";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            assertThat(actual.size()).isEqualTo(expected.size());

            int pad = pad(expected);

            for (int i = 0; i < expected.size(); i++) {
                System.out.println(
                        format(
                                "expected [%" + pad + "s] / actual [%" + pad + "s]",
                                expected.get(i),
                                actual.get(i)));
            }
        }
    }

    private static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    private static int pad(List<String> strings) {
        int pad = 0;

        for (String string : strings) {
            pad = string.length() > pad ? string.length() : pad;
        }

        return pad;
    }
}
