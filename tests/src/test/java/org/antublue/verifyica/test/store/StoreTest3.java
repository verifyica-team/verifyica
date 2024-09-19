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
import org.antublue.verifyica.api.Key;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.Verifyica;

public class StoreTest3 {

    private static final Key CLASS_CONTEXT_STORE_KEY =
            Key.of(StoreTest3.class, "class.context.key");

    @Verifyica.ArgumentSupplier
    public static String arguments() {
        return "ignored";
    }

    @Verifyica.Test
    @Verifyica.Order(order = 1)
    public void test1(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test1(%s)%n", argumentContext.getTestArgument().getPayload());

        Store store = argumentContext.getClassContext().getStore();
        store.getLock().lock();
        try {
            store.put(CLASS_CONTEXT_STORE_KEY.append("foo"), "FOO");
            store.put(CLASS_CONTEXT_STORE_KEY.append("bar"), "BAR");
        } finally {
            store.getLock().unlock();
        }
    }

    @Verifyica.Test
    @Verifyica.Order(order = 2)
    public void test2(ArgumentContext argumentContext) throws Throwable {
        System.out.printf("test2(%s)%n", argumentContext.getTestArgument().getPayload());

        Store store = argumentContext.getClassContext().getStore();
        store.getLock().lock();
        try {
            assertThat(store.get(CLASS_CONTEXT_STORE_KEY.append("foo"))).isEqualTo("FOO");
            assertThat(store.get(CLASS_CONTEXT_STORE_KEY.append("bar"))).isEqualTo("BAR");
        } finally {
            store.getLock().unlock();
        }
    }
}
