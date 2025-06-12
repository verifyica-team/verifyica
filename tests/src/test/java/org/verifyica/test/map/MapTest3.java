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

package org.verifyica.test.map;

import static org.assertj.core.api.Assertions.assertThat;

import org.verifyica.api.ArgumentContext;
import org.verifyica.api.ClassContext;
import org.verifyica.api.Verifyica;

public class MapTest3 {

    enum Key {
        MAP_TEST_3_ENGINE_CONTEXT_KEY,
        MAP_TEST_3_CLASS_CONTEXT_KEY,
        MAP_TEST_3_ARGUMENT_CONTEXT_KEY
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "test";
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void putIntoMaps(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("putIntoMaps(%s)%n", argumentContext.getTestArgument().getPayload());

        argumentContext.getMap().put(Key.MAP_TEST_3_ARGUMENT_CONTEXT_KEY.name(), "argument");
        argumentContext.getClassContext().getMap().put(Key.MAP_TEST_3_CLASS_CONTEXT_KEY.name(), "class");
        argumentContext
                .getClassContext()
                .getEngineContext()
                .getMap()
                .put(Key.MAP_TEST_3_ENGINE_CONTEXT_KEY.name(), "engine");
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void getOutOfMaps(ArgumentContext argumentContext) throws Throwable {
        System.out.printf(
                "getOutOfMaps(%s)%n", argumentContext.getTestArgument().getPayload());

        assertThat(argumentContext.getMap().get(Key.MAP_TEST_3_ARGUMENT_CONTEXT_KEY.name()))
                .isEqualTo("argument");

        assertThat(argumentContext.getClassContext().getMap().get(Key.MAP_TEST_3_CLASS_CONTEXT_KEY.name()))
                .isEqualTo("class");

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(Key.MAP_TEST_3_ENGINE_CONTEXT_KEY.name()))
                .isEqualTo("engine");

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(Key.MAP_TEST_3_ENGINE_CONTEXT_KEY.name()))
                .isEqualTo("engine");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext.getMap().remove(Key.MAP_TEST_3_CLASS_CONTEXT_KEY.name()))
                .isEqualTo("class");

        assertThat(classContext.getEngineContext().getMap().remove(Key.MAP_TEST_3_ENGINE_CONTEXT_KEY.name()))
                .isEqualTo("engine");
    }
}
