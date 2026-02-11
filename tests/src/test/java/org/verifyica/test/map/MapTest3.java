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

public class MapTest3 {

    /**
     * Enum to define keys for the maps used in the tests.
     */
    enum Key {
        ENGINE_CONTEXT_KEY,
        CLASS_CONTEXT_KEY,
        ARGUMENT_CONTEXT_KEY;

        /**
         * Returns a scoped name for the key, which is the class name + "." + the enum name.
         *
         * @return the scoped name
         */
        public String scopedName() {
            return getClass().getName() + "." + name();
        }
    }

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "test";
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void putIntoMaps(ArgumentContext argumentContext) {
        System.out.printf("putIntoMaps(%s)%n", argumentContext.getArgument().getPayload());

        argumentContext
                .getClassContext()
                .getEngineContext()
                .getMap()
                .put(Key.ENGINE_CONTEXT_KEY.scopedName(), "engine");

        argumentContext.getClassContext().getMap().put(Key.CLASS_CONTEXT_KEY.scopedName(), "class");

        argumentContext.getMap().put(Key.ARGUMENT_CONTEXT_KEY.scopedName(), "argument");
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void getOutOfMaps(ArgumentContext argumentContext) {
        System.out.printf("getOutOfMaps(%s)%n", argumentContext.getArgument().getPayload());

        assertThat(argumentContext
                        .getClassContext()
                        .getEngineContext()
                        .getMap()
                        .get(Key.ENGINE_CONTEXT_KEY.scopedName()))
                .isEqualTo("engine");

        assertThat(argumentContext.getClassContext().getMap().get(Key.CLASS_CONTEXT_KEY.scopedName()))
                .isEqualTo("class");

        assertThat(argumentContext.getMap().get(Key.ARGUMENT_CONTEXT_KEY.scopedName()))
                .isEqualTo("argument");
    }

    @Verifyica.Conclude
    public static void conclude(ClassContext classContext) {
        System.out.println("conclude()");

        assertThat(classContext.getEngineContext().getMap().remove(Key.ENGINE_CONTEXT_KEY.scopedName()))
                .isEqualTo("engine");

        assertThat(classContext.getMap().remove(Key.CLASS_CONTEXT_KEY.scopedName()))
                .isEqualTo("class");
    }
}
