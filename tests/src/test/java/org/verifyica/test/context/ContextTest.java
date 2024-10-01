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

package org.verifyica.test.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Context;
import org.verifyica.api.Verifyica;

public class ContextTest {

    // Only declared for testing
    private static Context engineContext;
    private static ClassContext classContext;
    private static ArgumentContext argumentContext;
    private static int value;

    @Verifyica.ArgumentSupplier
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Prepare
    public static void prepare(ClassContext classContext) {
        System.out.println("prepare()");

        assertThat(classContext).isNotNull();

        assertThat(classContext.getStore()).isNotNull();

        assertThat(classContext.getConfiguration())
                .isSameAs(classContext.getEngineContext().getConfiguration());

        assertThat(classContext.getConfiguration().getPropertiesFilename())
                .isEqualTo(classContext.getEngineContext().getConfiguration().getPropertiesFilename());

        assertThat(classContext.getTestInstance()).isNotNull();

        classContext
                .getConfiguration()
                .getPropertiesFilename()
                .ifPresent(path -> System.out.printf("properties filename [%s]%n", path));

        ContextTest.engineContext = classContext.getEngineContext();
        ContextTest.classContext = classContext;

        classContext.getStore().put("FOO", "BAR");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getName());

        assertThat(argumentContext).isNotNull();

        assertThat(argumentContext.getStore()).isNotNull();

        assertThat(argumentContext.getTestArgument()).isNotNull();

        assertThat(argumentContext.getClassContext().getStore().get("FOO", String.class))
                .isEqualTo("BAR");

        assertThat(argumentContext.getClassContext().getEngineContext()).isSameAs(ContextTest.engineContext);

        assertThat(argumentContext.getClassContext()).isSameAs(ContextTest.classContext);

        assertThat(argumentContext.getConfiguration())
                .isSameAs(argumentContext.getClassContext().getEngineContext().getConfiguration());

        ContextTest.argumentContext = argumentContext;
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getName());

        assertThat(argumentContext.getClassContext().getEngineContext()).isSameAs(ContextTest.engineContext);

        assertThat(argumentContext.getClassContext()).isSameAs(ContextTest.classContext);

        assertThat(argumentContext).isSameAs(ContextTest.argumentContext);

        assertThat(argumentContext.getConfiguration())
                .isSameAs(argumentContext.getClassContext().getEngineContext().getConfiguration());

        assertThat(argumentContext).isSameAs(ContextTest.argumentContext);
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) throws Throwable {
        assertThat(argumentContext.getClassContext().getEngineContext()).isSameAs(ContextTest.engineContext);

        assertThat(argumentContext.getClassContext()).isSameAs(ContextTest.classContext);

        assertThat(argumentContext).isSameAs(ContextTest.argumentContext);

        Class<?> contextTestClass = argumentContext.getClassContext().getTestClass();

        assertThat(contextTestClass).isNotNull();

        contextTestClass.getDeclaredField("value").set(null, 10);

        assertThat(ContextTest.value).isEqualTo(10);
    }

    @Verifyica.Test
    public void test4(ArgumentContext argumentContext) {
        assertThat(argumentContext.getClassContext().getEngineContext()).isSameAs(ContextTest.engineContext);

        assertThat(argumentContext.getClassContext()).isSameAs(ContextTest.classContext);

        assertThat(argumentContext).isSameAs(ContextTest.argumentContext);

        assertThat(argumentContext.getReadWriteLock())
                .isEqualTo(argumentContext.getStore().getReadWriteLock());

        assertThat(argumentContext.getClassContext().getLock())
                .isEqualTo(argumentContext.getClassContext().getStore().getLock());
        assertThat(argumentContext.getClassContext().getReadWriteLock())
                .isEqualTo(argumentContext.getClassContext().getStore().getReadWriteLock());

        assertThat(argumentContext.getClassContext().getEngineContext().getLock())
                .isEqualTo(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getStore()
                        .getLock());

        assertThat(argumentContext.getClassContext().getEngineContext().getReadWriteLock())
                .isEqualTo(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getStore()
                        .getReadWriteLock());
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext).isNotNull();

        assertThat(classContext.getStore()).isNotNull();

        assertThat(classContext.getConfiguration())
                .isSameAs(classContext.getEngineContext().getConfiguration());

        assertThat(classContext.getConfiguration().getPropertiesFilename())
                .isEqualTo(classContext.getEngineContext().getConfiguration().getPropertiesFilename());

        assertThat(classContext.getTestInstance()).isNotNull();
    }
}
