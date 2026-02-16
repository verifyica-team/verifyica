/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.test.map;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class MapTest1 {

    private static final String PREFIX = MapTest1.class.getName();
    private static final String ARGUMENT_CONTEXT_KEY = PREFIX + "argument.context.key";
    private static final String CLASS_CONTEXT_KEY = PREFIX + "class.context.key";
    private static final String ENGINE_CONTEXT_KEY = PREFIX + "engine.context.key";

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "test";
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void putIntoMaps(ArgumentContext argumentContext) {
        System.out.printf("putIntoMaps(%s)%n", argumentContext.getArgument().getPayload());

        argumentContext.getClassContext().getEngineContext().getMap().put(ENGINE_CONTEXT_KEY, "engine");
        argumentContext.getClassContext().getMap().put(CLASS_CONTEXT_KEY, "class");
        argumentContext.getMap().put(ARGUMENT_CONTEXT_KEY, "argument");
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void getOutOfMaps(ArgumentContext argumentContext) {
        System.out.printf("getOutOfMaps(%s)%n", argumentContext.getArgument().getPayload());

        assertThat(argumentContext.getClassContext().getEngineContext().getMap().get(ENGINE_CONTEXT_KEY))
                .isEqualTo("engine");

        assertThat(argumentContext.getClassContext().getMap().get(CLASS_CONTEXT_KEY))
                .isEqualTo("class");

        assertThat(argumentContext.getMap().get(ARGUMENT_CONTEXT_KEY)).isEqualTo("argument");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext.getEngineContext().getMap().remove(ENGINE_CONTEXT_KEY))
                .isEqualTo("engine");

        assertThat(classContext.getMap().remove(CLASS_CONTEXT_KEY)).isEqualTo("class");
    }
}
