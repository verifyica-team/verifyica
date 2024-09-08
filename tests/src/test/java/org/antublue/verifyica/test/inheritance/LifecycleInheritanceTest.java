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

package org.antublue.verifyica.test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;

@Verifyica.Testable
public class LifecycleInheritanceTest {

    private static final int ARGUMENT_COUNT = 1;

    // Antipattern, but used for testing
    public static List<String> actual = new ArrayList<>();

    public abstract static class BaseClass {

        @Verifyica.Prepare
        public void baseClassPrepare(ClassContext classContext) {
            System.out.println("baseClassPrepare()");

            actual.add("baseClassPrepare");
        }

        @Verifyica.Prepare
        public void baseClassPrepare2(ClassContext classContext) {
            System.out.println("baseClassPrepare2()");

            actual.add("baseClassPrepare2");
        }

        @Verifyica.Test
        public void test(ArgumentContext argumentContext) {
            System.out.println("baseClassTest()");

            actual.add("baseClassTest");
        }

        @Verifyica.Test
        public void test3(ArgumentContext argumentContext) {
            System.out.println("baseClassTest3()");

            actual.add("baseClassTest3");
        }

        @Verifyica.Conclude
        public void baseClassConclude2(ClassContext classContext) {
            System.out.println("baseClassConclude2()");

            actual.add("baseClassConclude2");
        }

        @Verifyica.Conclude
        public void baseClassConclude(ClassContext classContext) {
            System.out.println("baseClassConclude()");

            actual.add("baseClassConclude");

            List<String> expected = new ArrayList<>();

            expected.add("baseClassPrepare");
            expected.add("baseClassPrepare2");
            expected.add("subClassPrepare");
            expected.add("subClassPrepare2");
            expected.add("baseClassTest");
            expected.add("subClassTest2");
            expected.add("baseClassTest3");
            expected.add("subClassConclude2");
            expected.add("subClassConclude");
            expected.add("baseClassConclude2");
            expected.add("baseClassConclude");

            assertThat(actual.size()).isEqualTo(expected.size());

            int pad = pad(expected);

            for (int i = 0; i < expected.size(); i++) {
                System.out.printf(
                        "expected [%-" + pad + "s] actual [%-" + pad + "s]%n",
                        expected.get(i),
                        actual.get(i));

                assertThat(actual.get(i)).isEqualTo(expected.get(i));
            }
        }
    }

    public static class SubClass extends BaseClass {

        @Verifyica.ArgumentSupplier
        public static Collection<Argument<String>> arguments() {
            Collection<Argument<String>> collection = new ArrayList<>();
            for (int i = 0; i < ARGUMENT_COUNT; i++) {
                collection.add(Argument.of("argument[" + i + "]", String.valueOf(i)));
            }
            return collection;
        }

        @Verifyica.Prepare
        public void subClassPrepare(ClassContext classContext) {
            System.out.println("subClassPrepare()");

            actual.add("subClassPrepare");
        }

        @Verifyica.Prepare
        public void subClassPrepare2(ClassContext classContext) {
            System.out.println("subClassPrepare2()");

            actual.add("subClassPrepare2");
        }

        @Verifyica.Test
        public void test2(ArgumentContext argumentContext) {
            System.out.println("subClassTest2()");

            actual.add("subClassTest2");
        }

        @Verifyica.Conclude
        public void subClassConclude2(ClassContext classContext) {
            System.out.println("subClassConclude2()");

            actual.add("subClassConclude2");
        }

        @Verifyica.Conclude
        public void subClassConclude(ClassContext classContext) {
            System.out.println("subClassConclude()");

            actual.add("subClassConclude");
        }
    }

    /*
    public String string;

    @Verifyica.ClassInterceptorSupplier
    public static Object classInterceptors() {
        return new ConcreteClassInterceptor();
    }

    @Verifyica.ArgumentSupplier
    public static Collection<AutoClosableArgument> arguments() {
        Collection<AutoClosableArgument> autoClosableArguments = new ArrayList<>();
        for (int i = 0; i < ARGUMENT_COUNT; i++) {
            autoClosableArguments.add(new AutoClosableArgument("argument[" + i + "]"));
        }
        return autoClosableArguments;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.printf("  %s prepare()%n", classContext.getTestClass().getName());

        assertThat(classContext).isNotNull();
        assertThat(classContext.getTestInstance()).isNotNull();

        LifecycleInheritanceTest lifecycleTest = classContext.getTestInstance(LifecycleInheritanceTest.class);

        assertThat(lifecycleTest).isNotNull();
        lifecycleTest.string = "FOO";

        actual.add("prepare");
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s beforeAll()%n", argumentContext.getTestArgument().getPayload());

        actual.add("beforeAll");
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s beforeEach()%n", argumentContext.getTestArgument().getPayload());

        actual.add("beforeEach");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test1()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test1");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test2()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test2");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s test3()%n", argumentContext.getTestArgument().getPayload());

        actual.add("test3");
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s afterEach()%n", argumentContext.getTestArgument().getPayload());

        actual.add("afterEach");
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("  %s afterAll()%n", argumentContext.getTestArgument().getPayload());

        actual.add("afterAll");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.printf("  %s conclude()%n", classContext.getTestClass().getName());

        assertThat(classContext).isNotNull();
        assertThat(classContext.getTestInstance()).isNotNull();

        LifecycleInheritanceTest lifecycleTest = classContext.getTestInstance(LifecycleInheritanceTest.class);

        assertThat(lifecycleTest).isNotNull();
        assertThat(lifecycleTest.string).isEqualTo("FOO");

        actual.add("conclude");
    }

    public static class ConcreteClassInterceptor implements ClassInterceptor {

        @Override
        public void preInstantiate(
                EngineInterceptorContext engineInterceptorContext, Class<?> testClass)
                throws Throwable {
            System.out.printf("%s preInstantiate()%n", getClass().getName());

            actual.add("preInstantiate");
        }

        @Override
        public void postInstantiate(
                EngineInterceptorContext engineInterceptorContext,
                Class<?> testClass,
                Object testInstance,
                Throwable throwable)
                throws Throwable {
            System.out.printf("%s postInstantiate()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postInstantiate");
        }

        @Override
        public void prePrepare(ClassInterceptorContext classInterceptorContext) throws Throwable {
            System.out.printf("%s prePrepare()%n", getClass().getName());

            assertThat(classInterceptorContext).isNotNull();
            assertThat(classInterceptorContext.getClassContext()).isNotNull();

            actual.add("prePrepare");
        }

        @Override
        public void postPrepare(
                ClassInterceptorContext classInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postPrepare()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postPrepare");
        }

        @Override
        public void preBeforeAll(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.printf("%s preBeforeAll()%n", getClass().getName());

            actual.add("preBeforeAll");
        }

        @Override
        public void postBeforeAll(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postBeforeAll()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postBeforeAll");
        }

        @Override
        public void preBeforeEach(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.printf("%s preBeforeEach()%n", getClass().getName());

            actual.add("preBeforeEach");
        }

        @Override
        public void postBeforeEach(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postBeforeEach()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postBeforeEach");
        }

        @Override
        public void preTest(
                ArgumentInterceptorContext argumentInterceptorContext, Method testMethod)
                throws Throwable {
            System.out.printf("%s preTest()%n", getClass().getName());

            actual.add("preTest");
        }

        @Override
        public void postTest(
                ArgumentInterceptorContext argumentInterceptorContext,
                Method testMethod,
                Throwable throwable)
                throws Throwable {
            System.out.printf("%s postTest()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postTest");
        }

        @Override
        public void preAfterEach(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.printf("%s preAfterEach()%n", getClass().getName());

            actual.add("preAfterEach");
        }

        @Override
        public void postAfterEach(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postAfterEach()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postAfterEach");
        }

        @Override
        public void preAfterAll(ArgumentInterceptorContext argumentInterceptorContext)
                throws Throwable {
            System.out.printf("%s preAfterAll()%n", getClass().getName());

            actual.add("preAfterAll");
        }

        @Override
        public void postAfterAll(
                ArgumentInterceptorContext argumentInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postAfterAll()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postAfterAll");
        }

        @Override
        public void preConclude(ClassInterceptorContext classInterceptorContext) throws Throwable {
            System.out.printf("%s preConclude()%n", getClass().getName());

            actual.add("preConclude");
        }

        @Override
        public void postConclude(
                ClassInterceptorContext classInterceptorContext, Throwable throwable)
                throws Throwable {
            System.out.printf("%s postConclude()%n", getClass().getName());

            assertThat(throwable).isNull();
            actual.add("postConclude");
        }

        @Override
        public void onDestroy(ClassInterceptorContext classInterceptorContext) throws Throwable {
            System.out.printf("%s onDestroy()%n", getClass().getName());

            assertThat(classInterceptorContext).isNotNull();
            assertThat(classInterceptorContext.getClassContext()).isNotNull();
            assertThat(classInterceptorContext.getClassContext().getTestInstance()).isNotNull();

            LifecycleInheritanceTest lifecycleTest =
                    classInterceptorContext.getClassContext().getTestInstance(LifecycleInheritanceTest.class);

            assertThat(lifecycleTest).isNotNull();

            List<String> expected = new ArrayList<>();

            expected.add("preInstantiate");
            expected.add("postInstantiate");

            String state = "prepare";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            for (int i = 0; i < ARGUMENT_COUNT; i++) {
                state = "beforeAll";
                expected.add("pre" + capitalize(state));
                expected.add(state);
                expected.add("post" + capitalize(state));

                for (int j = 1; j < 4; j++) {
                    state = "beforeEach";
                    expected.add("pre" + capitalize(state));
                    expected.add(state);
                    expected.add("post" + capitalize(state));

                    state = "test";
                    expected.add("pre" + capitalize(state));
                    expected.add(state + j);
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

                expected.add("argumentClose");
            }

            state = "conclude";
            expected.add("pre" + capitalize(state));
            expected.add(state);
            expected.add("post" + capitalize(state));

            assertThat(actual.size()).isEqualTo(expected.size());

            int pad = pad(expected);

            for (int i = 0; i < expected.size(); i++) {
                System.out.printf(
                        "expected [%-" + pad + "s] actual [%-" + pad + "s]%n",
                        expected.get(i),
                        actual.get(i));

                assertThat(actual.get(i)).isEqualTo(expected.get(i));
            }
        }
    }
    */

    private static int pad(List<String> strings) {
        int pad = 0;

        for (String string : strings) {
            pad = Math.max(string.length(), pad);
        }

        return pad;
    }
}
