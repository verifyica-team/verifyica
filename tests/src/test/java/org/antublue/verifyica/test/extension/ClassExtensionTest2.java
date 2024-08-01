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

package org.antublue.verifyica.test.extension;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.UUID;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.extension.ArgumentExtensionContext;
import org.antublue.verifyica.api.extension.ClassExtension;
import org.antublue.verifyica.api.extension.ClassExtensionContext;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.extension.ClassExtensionRegistry;

/** Example test */
public class ClassExtensionTest2 {

    static {
        // Directly register an extension
        ClassExtensionRegistry.getInstance()
                .register(ClassExtensionTest2.class, new ConcreteClasExtension1());
    }

    // Anti-pattern, but used for testing
    public static String value;

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s prepare()", classContext.getTestClass().getName()));
        System.out.println(format("  %s value [%s]", ClassExtensionTest2.class.getName(), value));

        assertThat(value).isNotNull();
        assertThat(value).isNotEmpty();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeAll()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s beforeEach()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test1()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test2()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("  %s test3()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterEach()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws Throwable {
        System.out.println(
                format("  %s afterAll()", argumentContext.getTestArgument().getPayload()));
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) throws Throwable {
        System.out.println(format("  %s conclude()", classContext.getTestClass().getName()));
    }

    public static class ConcreteClasExtension1 implements ClassExtension {

        @Override
        public void beforeInstantiate(
                EngineExtensionContext engineExtensionContext, Class<?> testClass)
                throws Throwable {
            System.out.println(format("%s beforeInstantiate()", getClass().getName()));

            testClass.getField("value").set(null, UUID.randomUUID().toString());
        }

        @Override
        public void afterInstantiate(
                EngineExtensionContext engineExtensionContext,
                Class<?> testClass,
                Object testInstance,
                Throwable throwable)
                throws Throwable {
            System.out.println(format("%s afterInstantiate()", getClass().getName()));

            assertThat(testClass).isNotNull();
            assertThat(testInstance).isNotNull();
            assertThat(testInstance).isInstanceOf(ClassExtensionTest2.class);
            assertThat(throwable).isNull();
        }

        @Override
        public void beforePrepare(ClassExtensionContext classExtensionContext) throws Throwable {
            System.out.println(format("%s beforePrepare()", getClass().getName()));
        }

        @Override
        public void afterPrepare(ClassExtensionContext classExtensionContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s afterPrepare()", getClass().getName()));

            assertThat(throwable).isNull();
        }

        @Override
        public void beforeTest(ArgumentExtensionContext argumentExtensionContext, Method testMethod)
                throws Throwable {
            System.out.println(format("%s beforeTest()", getClass().getName()));
        }

        @Override
        public void afterTest(
                ArgumentExtensionContext argumentExtensionContext,
                Method testMethod,
                Throwable throwable)
                throws Throwable {
            System.out.println(format("%s afterTest()", getClass().getName()));

            assertThat(throwable).isNull();
        }

        @Override
        public void beforeConclude(ClassExtensionContext classExtensionContext) throws Throwable {
            System.out.println(format("%s beforeConclude()", getClass().getName()));
        }

        @Override
        public void afterConclude(ClassExtensionContext classExtensionContext, Throwable throwable)
                throws Throwable {
            System.out.println(format("%s afterConclude()", getClass().getName()));

            assertThat(throwable).isNull();
        }
    }
}
