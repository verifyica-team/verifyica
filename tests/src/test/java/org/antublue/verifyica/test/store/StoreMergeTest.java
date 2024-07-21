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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
    public void test1(ArgumentContext argumentContext) {
        System.out.println("test1()");

        argumentContext.getObjectStore().clear();
        System.out.println(
                "argument context store before merge " + argumentContext.getObjectStore());

        Store<Object, Object> store = new Store<>();
        for (int i = 0; i < 10; i++) {
            store.put("key " + i, "value " + i);
        }
        System.out.println("store " + store);

        argumentContext.getObjectStore().merge(store);
        assertThat(argumentContext.getObjectStore()).isEqualTo(store);

        System.out.println(
                "argument context store after merge " + argumentContext.getObjectStore());
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) {
        System.out.println("test2()");

        argumentContext.getObjectStore().clear();
        System.out.println(
                "argument context store before merge " + argumentContext.getObjectStore());

        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key " + i, "value " + i);
        }

        System.out.println("map " + map);

        argumentContext.getObjectStore().merge(map);

        assertThat(argumentContext.getObjectStore().size()).isEqualTo(map.size());

        map.forEach(
                (key, value) -> {
                    Store<Object, Object> store = argumentContext.getObjectStore();
                    assertThat(store.containsKey(key)).isTrue();
                    assertThat(store.get(key) == value).isTrue();
                });

        System.out.println(
                "argument context store after merge " + argumentContext.getObjectStore());
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        System.out.println("test3()");

        argumentContext.getObjectStore().clear();
        System.out.println(
                "argument context store before merge " + argumentContext.getObjectStore());

        Properties properties = new Properties();
        for (int i = 0; i < 10; i++) {
            properties.setProperty("key " + i, "value " + i);
        }

        System.out.println("properties " + properties);

        argumentContext.getObjectStore().merge(properties);

        assertThat(argumentContext.getObjectStore().size()).isEqualTo(properties.size());

        properties.forEach(
                (key, value) -> {
                    Store<Object, Object> store = argumentContext.getObjectStore();
                    assertThat(store.containsKey(key)).isTrue();
                    assertThat(store.get(key) == value).isTrue();
                });

        System.out.println(
                "argument context store after merge " + argumentContext.getObjectStore());
    }
}
