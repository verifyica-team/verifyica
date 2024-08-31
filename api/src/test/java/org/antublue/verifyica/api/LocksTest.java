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

package org.antublue.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class LocksTest {

    @Test
    public void testMultithreading() throws InterruptedException {
        UUID uuid = UUID.randomUUID();
        AtomicInteger atomicInteger = new AtomicInteger();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                Locks.lock(uuid);
                                try {
                                    System.out.printf(
                                            "thread [%s] locked%n",
                                            Thread.currentThread().getName());

                                    atomicInteger.incrementAndGet();

                                    try {
                                        Thread.sleep(RandomSupport.randomLong(0, 1000));
                                    } catch (InterruptedException e) {
                                        // INTENTIONALLY BLANK
                                    }
                                } finally {
                                    atomicInteger.decrementAndGet();

                                    System.out.printf(
                                            "thread [%s] unlocked%n",
                                            Thread.currentThread().getName());
                                    Locks.unlock(uuid);
                                }
                            });
            threads[i].setName("thread-" + i);
            threads[i].setDaemon(true);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(atomicInteger).hasValue(0);
        Locks.assertEmpty();
    }
}
