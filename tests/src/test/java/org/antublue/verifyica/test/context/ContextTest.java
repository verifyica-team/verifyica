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

package org.antublue.verifyica.test.context;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.ClassContext;
import org.antublue.verifyica.api.Context;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class ContextTest {

    // Only declared for testing
    private static Context rootContext;
    private static ClassContext classContext;
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
        System.out.println(format("prepare()"));

        assertThat(classContext.getClass().getSimpleName()).startsWith("Immutable");
        assertThat(classContext.getEngineContext().getClass().getSimpleName())
                .startsWith("Immutable");
        assertThat(classContext).isNotNull();
        assertThat(classContext.getStore()).isNotNull();

        ContextTest.rootContext = classContext.getEngineContext();
        ContextTest.classContext = classContext;

        classContext.getStore().put("FOO", "BAR");
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.println(format("test1(%s)", argumentContext.getTestArgument().getName()));

        assertThat(argumentContext).isNotNull();
        assertThat(argumentContext.getStore()).isNotNull();
        assertThat(argumentContext.getTestArgument()).isNotNull();
        assertThat(argumentContext.getClassContext().getStore().get("FOO", String.class))
                .isEqualTo("BAR");
        assertThat(argumentContext.getClassContext() == ContextTest.classContext);
        assertThat(argumentContext.getClassContext().getEngineContext() == ContextTest.rootContext);
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        System.out.println(format("test2(%s)", argumentContext.getTestArgument().getName()));

        assertThat(argumentContext.getClass().getSimpleName()).startsWith("Immutable");
        assertThat(argumentContext.getClassContext().getClass().getSimpleName())
                .startsWith("Immutable");
        assertThat(argumentContext.getClassContext().getEngineContext().getClass().getSimpleName())
                .startsWith("Immutable");
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) throws Throwable {
        assertThat(ContextTest.value).isEqualTo(0);

        Class<ContextTest> contextTest =
                argumentContext.getClassContext().getTestClass(ContextTest.class);
        contextTest.getDeclaredField("value").set(null, 10);

        assertThat(ContextTest.value).isEqualTo(10);
    }
}
