/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.test.threadlocal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class ClassThreadLocalTest2 {

    ThreadLocal<Integer> threadLocalValue = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Prepare
    public void prepare(ClassContext classContext) {
        System.out.println("prepare()");
        System.out.printf("test argument parallelism [%d]%n", classContext.getTestArgumentParallelism());

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();

        classContext
                .getEngineContext()
                .getConfiguration()
                .getPropertiesPath()
                .ifPresent(path -> System.out.printf("properties filename [%s]%n", path));

        synchronized (classContext.getTestInstance()) {
            assertThat(threadLocalValue.get()).isNull();
            threadLocalValue.set(1);
            assertThat(threadLocalValue.get()).isNotNull();
            assertThat(threadLocalValue.get()).isEqualTo(1);
        }
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) {
        System.out.printf(
                "beforeAll(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.BeforeEach
    public void beforeEach(ArgumentContext argumentContext) {
        System.out.printf(
                "beforeEach(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        System.out.printf(
                "test1(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        System.out.printf(
                "test2(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        System.out.printf(
                "test3(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterEach
    public void afterEach(ArgumentContext argumentContext) {
        System.out.printf(
                "afterEach(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        System.out.printf(
                "afterAll(index=[%d], name=[%s])%n",
                argumentContext.getTestArgumentIndex(),
                argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getMap()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
    }

    @Verifyica.Conclude
    public void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext).isNotNull();
        assertThat(classContext.getMap()).isNotNull();
    }
}
