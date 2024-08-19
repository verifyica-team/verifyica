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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptorContext;
import org.antublue.verifyica.test.support.RandomSupport;

/** Example test */
public class EngineInterceptorTest5 {

    @Verifyica.AutowiredInterceptor
    public static class LocalEngineInterceptor implements EngineInterceptor {

        @Override
        public void onTestDiscovery(
                EngineInterceptorContext engineInterceptorContext,
                ClassDefinition classDefinition) {
            if (classDefinition.getTestClass() == EngineInterceptorTest5.class) {
                List<Argument<?>> arguments = classDefinition.getTestArguments();
                assertThat(arguments).hasSize(10);
                if (arguments.size() > 1) {
                    arguments.subList(0, arguments.size() - 1).clear();
                }
                assertThat(arguments).hasSize(1);
            }
        }
    }

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println("prepare()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        System.out.printf(
                "beforeAll(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.printf(
                "beforeEach(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test1(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);

        Thread.sleep(RandomSupport.randomLong(0, 1000));
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "test2(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);

        Thread.sleep(RandomSupport.randomLong(0, 1000));
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.printf(
                "afterEach(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.printf(
                "afterAll(index=%d name=%s)%n",
                argumentContext.getTestArgumentIndex(), argumentContext.getTestArgument());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getTestArgumentIndex()).isEqualTo(0);
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();
    }
}
