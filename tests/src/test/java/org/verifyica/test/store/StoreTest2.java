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

package org.verifyica.test.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Key;
import org.verifyica.api.Verifyica;

public class StoreTest2 {

    private static final Key ARGUMENT_CONTEXT_KEY = Key.of("argument.context.key");
    private static final Key CLASS_CONTEXT_KEY = Key.of("class.context.key");
    private static final Key ENGINE_CONTEXT_KEY = Key.of("engine.context.key");

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test
    @Verifyica.Order(order = 0)
    public void putIntoStores(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "putIntoStores(%s)%n", argumentContext.getTestArgument().getPayload());

        argumentContext.getStore().put(ARGUMENT_CONTEXT_KEY, "argument");
        argumentContext.getClassContext().getStore().put(CLASS_CONTEXT_KEY, "class");
        argumentContext.getClassContext().getEngineContext().getStore().put(ENGINE_CONTEXT_KEY, "engine");
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void getOutOfStores(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "getOutOfStores(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext.getStore().get(ARGUMENT_CONTEXT_KEY, String.class))
                .isEqualTo("argument");

        assertThat(argumentContext.getClassContext().getStore().get(CLASS_CONTEXT_KEY, String.class))
                .isEqualTo("class");

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getStore()
                        .get(ENGINE_CONTEXT_KEY, String.class))
                .isEqualTo("engine");

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getStore()
                        .get(ENGINE_CONTEXT_KEY, String.class))
                .isEqualTo("engine");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext.getStore().remove(CLASS_CONTEXT_KEY, String.class))
                .isEqualTo("class");

        assertThat(classContext.getEngineContext().getStore().remove(ENGINE_CONTEXT_KEY, String.class))
                .isEqualTo("engine");
    }
}