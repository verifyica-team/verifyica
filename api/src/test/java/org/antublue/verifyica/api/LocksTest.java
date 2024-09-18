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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class LocksTest {

    @Test
    public void testLockUnlockSequence() {
        String key = "key";

        for (int i = 0; i < 10; i++) {
            Locks.assertEmpty();

            assertThat(Locks.isLocked(key)).isFalse();

            Locks.lock(key);
            Locks.assertNotEmpty();
            Locks.assertSize(1);

            assertThat(Locks.isLocked(key)).isTrue();

            Locks.unlock(key);

            assertThat(Locks.isLocked(key)).isFalse();

            Locks.assertEmpty();
            Locks.assertSize(0);
        }

        Locks.assertEmpty();
        Locks.assertSize(0);
    }

    @Test
    public void testUnlockWithoutLock() {
        assertThatExceptionOfType(IllegalMonitorStateException.class)
                .isThrownBy(() -> Locks.unlock("key"));

        Locks.assertEmpty();
        Locks.assertSize(0);
    }

    @Test
    public void testMultiLockUnlock() {
        String key = "key";

        Locks.lock(key);

        Locks.assertSize(1);

        Locks.lock(key);

        Locks.assertSize(1);

        Locks.lock(key);

        Locks.assertSize(1);

        assertThat(Locks.tryLock(key)).isTrue();
        assertThat(Locks.tryLock(key)).isTrue();
        assertThat(Locks.tryLock(key)).isTrue();

        Locks.assertSize(1);

        assertThat(Locks.isLocked(key)).isTrue();

        Locks.assertNotEmpty();
        Locks.assertSize(1);

        Locks.unlock(key);

        Locks.assertEmpty();

        assertThatExceptionOfType(IllegalMonitorStateException.class)
                .isThrownBy(() -> Locks.unlock(key));

        assertThat(Locks.isLocked(key)).isFalse();

        Locks.assertEmpty();
        Locks.assertSize(0);
    }

    @Test
    public void testMultithreading() throws InterruptedException {
        int threadCount = 100;
        UUID uuid = UUID.randomUUID();
        AtomicInteger atomicInteger = new AtomicInteger();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                Locks.lock(uuid);
                                try {
                                    System.out.printf(
                                            "thread [%s] locked%n",
                                            Thread.currentThread().getName());

                                    assertThat(atomicInteger.incrementAndGet()).isEqualTo(1);

                                    try {
                                        Thread.sleep(RandomSupport.randomLong(0, 200));
                                    } catch (InterruptedException e) {
                                        // INTENTIONALLY BLANK
                                    }
                                } finally {
                                    assertThat(atomicInteger.decrementAndGet()).isEqualTo(0);

                                    System.out.printf(
                                            "thread [%s] unlocked%n",
                                            Thread.currentThread().getName());
                                    Locks.unlock(uuid);
                                }
                            });
            threads[i].setName("thread-" + i);
            threads[i].setDaemon(true);
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < threads.length; i++) {
            indices.add(i);
        }

        Collections.shuffle(indices);

        for (int index : indices) {
            threads[index].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(atomicInteger).hasValue(0);

        Locks.assertEmpty();
    }

    @Test
    public void testDifferentLockUnlockThreads() throws InterruptedException {
        String key = "key";
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread lockThread =
                new Thread(
                        () -> {
                            Locks.lock(key);

                            try {
                                countDownLatch.await();
                            } catch (InterruptedException e) {
                                // INTENTIONALLY BLANK
                            }

                            Locks.unlock(key);
                        });
        lockThread.start();

        Thread unlockThread =
                new Thread(
                        () -> {
                            assertThatExceptionOfType(IllegalMonitorStateException.class)
                                    .isThrownBy(() -> Locks.unlock(key));
                            countDownLatch.countDown();
                        });
        unlockThread.start();
        unlockThread.join();

        lockThread.join();

        Locks.assertEmpty();
    }
}
