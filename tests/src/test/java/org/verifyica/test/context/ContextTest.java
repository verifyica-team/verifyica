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

package org.verifyica.test.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;
import static org.verifyica.test.support.AssertionSupport.assertClassContext;

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
        assertClassContext(classContext);

        System.out.println("prepare()");

        classContext
                .getConfiguration()
                .getPropertiesPath()
                .ifPresent(path -> System.out.printf("properties filename [%s]%n", path));

        ContextTest.engineContext = classContext.getEngineContext();
        ContextTest.classContext = classContext;

        classContext.getMap().put("FOO", "BAR");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);
        ;

        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getName());

        assertThat(argumentContext.getClassContext().getMap().get("FOO")).isEqualTo("BAR");

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
    public void test3(ArgumentContext argumentContext) throws NoSuchFieldException, IllegalAccessException {
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
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        assertClassContext(classContext);

        System.out.println("conclude()");
    }
}
