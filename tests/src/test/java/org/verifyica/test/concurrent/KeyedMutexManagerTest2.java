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

package org.verifyica.test.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.verifyica.test.support.AssertionSupport.assertArgumentContext;

import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.Argument;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.concurrent.KeyedMutexManager;

public class KeyedMutexManagerTest2 {

    private static final String LOCK_KEY = KeyedMutexManagerTest2.class.getName();

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Collection<Argument<String>> arguments() {
        Collection<Argument<String>> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Argument.ofString("String " + i));
        }

        return collection;
    }

    @Verifyica.Test
    public void test1(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);

        System.out.printf("test1(%s)%n", argumentContext.getArgument());
    }

    @Verifyica.Test
    public void test2(ArgumentContext argumentContext) throws InterruptedException {
        KeyedMutexManager.lock(LOCK_KEY);
        try {
            System.out.printf("test2(%s) locked%n", argumentContext.getArgument());
            System.out.printf("test2(%s)%n", argumentContext.getArgument());

            assertThat(argumentContext).isNotNull();
            assertThat(argumentContext.getMap()).isNotNull();
            assertThat(argumentContext.getArgument()).isNotNull();

            Thread.sleep(1_000);

            System.out.printf("test2(%s) unlocked%n", argumentContext.getArgument());
        } finally {
            KeyedMutexManager.unlock(LOCK_KEY);
        }
    }

    @Verifyica.Test
    public void test3(ArgumentContext argumentContext) {
        assertArgumentContext(argumentContext);

        System.out.printf("test3(%s)%n", argumentContext.getArgument());
    }
}
