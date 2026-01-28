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

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class MapTest4 {

    /**
     * Enum to define keys for the maps used in the tests.
     */
    enum Key {
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

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Iterable<String> arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("test-" + i);
        }

        return collection;
    }

    @Verifyica.Test
    @Verifyica.Order(0)
    public void putIntoMaps(ArgumentContext argumentContext) {
        System.out.printf("putIntoMaps(%s)%n", argumentContext.testArgument().payload());

        String payload = argumentContext.testArgument().payload(String.class);

        argumentContext.map().put(Key.ARGUMENT_CONTEXT_KEY.scopedName(), "argument." + payload);
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void getOutOfMaps(ArgumentContext argumentContext) {
        System.out.printf("getOutOfMaps(%s)%n", argumentContext.testArgument().payload());

        String payload = argumentContext.testArgument().payload(String.class);

        assertThat(argumentContext.map().get(Key.ARGUMENT_CONTEXT_KEY.scopedName()))
                .isEqualTo("argument." + payload);
    }
}
