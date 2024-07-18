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

package org.antublue.verifyica.test.store;

import static org.assertj.core.api.Assertions.assertThat;

import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class StoreMergeTest {

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "dummy";
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        System.out.println("argument context store before merge " + argumentContext.getStore());

        Store store = new Store();

        for (int i = 0; i < 10; i++) {
            store.put("key " + i, "value " + i);
        }

        System.out.println("store " + store);

        argumentContext.getStore().merge(store);

        assertThat(argumentContext.getStore()).isEqualTo(store);

        System.out.println("argument context store after merge " + argumentContext.getStore());
    }
}
