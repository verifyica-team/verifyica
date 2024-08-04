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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
public class StoreLockTest2 {

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<String> arguments() {
        Collection<String> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add("String " + i);
        }

        return collection;
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) throws Throwable {
        Store classContextStore = argumentContext.getClassContext().getStore();
        try {
            classContextStore.getReadWriteLock().writeLock().lock();
            System.out.println(
                    format(
                            "%s test() locked class context store",
                            argumentContext.getTestArgument(String.class).getName()));

            System.out.println("test()");

            Thread.sleep(1000);
        } finally {
            System.out.println(
                    format(
                            "%s test() unlocked class context",
                            argumentContext.getTestArgument(String.class).getName()));
            classContextStore.getReadWriteLock().writeLock().unlock();
        }
    }
}
