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

package org.antublue.verifyica.test.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.ArgumentContext;
import org.antublue.verifyica.api.Verifyica;

/** Example test */
@SuppressWarnings("unchecked")
public class ClassContextSemaphoreTest {

    private static final String SEMAPHORE_KEY = "semaphoreKey";

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) throws Throwable {
        // ... test 1 code ...

        Thread.sleep(1000);
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws Throwable {
        try {
            acquireSemaphore(argumentContext, SEMAPHORE_KEY);
            // ... test 2 code ...
            Thread.sleep(1000);
        } finally {
            releaseSemaphore(argumentContext, SEMAPHORE_KEY);
        }
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) throws Throwable {
        // ... test 3 code ...

        Thread.sleep(1000);
    }

    private static void acquireSemaphore(ArgumentContext argumentContext, String semaphoreKey)
            throws Throwable {
        Semaphore.class
                .cast(
                        argumentContext
                                .getClassContext()
                                .getStore()
                                .computeIfAbsent(semaphoreKey, o -> new Semaphore(1)))
                .acquire();
    }

    private static void releaseSemaphore(ArgumentContext argumentContext, String semaphoreKey) {
        argumentContext.getClassContext().getStore().get(semaphoreKey, Semaphore.class).release();
    }
}
