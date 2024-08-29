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

package org.antublue.verifyica.test.interceptor.engine;

import static org.antublue.verifyica.api.Fail.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;

@Verifyica.AutowiredInterceptor
public class EngineInterceptorTest2 implements EngineInterceptor {

    @Override
    public void onTestDiscovery(
            EngineInterceptorContext engineInterceptorContext,
            List<ClassDefinition> classDefinitions) {
        System.out.printf("%s onTestDiscovery()%n", getClass().getName());

        for (ClassDefinition classDefinition : classDefinitions) {
            if (classDefinition.getTestClass() == EngineInterceptorTest2.class) {
                // Filter test method "test2"
                classDefinition
                        .getTestMethods()
                        .removeIf(method -> method.getName().equals("test2"));
            }
        }
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(
                        argumentContext.getClassContext().getEngineContext()
                                == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY, String.class))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());

        fail("Should not execute... filtered by interceptor");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test3(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test3(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(
                        argumentContext.getClassContext().getEngineContext()
                                == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY, String.class))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }

    @Verifyica.Test
    public void test4(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test4(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(
                        argumentContext.getClassContext().getEngineContext()
                                == argumentContext.getClassContext().getEngineContext())
                .isTrue();

        // Validate that the interceptor added a global String to the EngineContext Store
        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY))
                .isNotNull();

        assertThat(
                        argumentContext
                                .getClassContext()
                                .getEngineContext()
                                .getStore()
                                .get(ExampleAutowiredEngineInterceptor1.KEY, String.class))
                .isEqualTo(ExampleAutowiredEngineInterceptor1.VALUE);
    }
}
