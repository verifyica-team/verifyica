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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class LockManagerTest {

    @Test
    public void testLockUnlockSequence() {
        String key = "key";

        for (int i = 0; i < 10; i++) {
            LockManager.assertSize(0);

            assertThat(LockManager.isLocked(key)).isFalse();

            LockManager.lock(key);

            LockManager.assertSize(1);

            assertThat(LockManager.isLocked(key)).isTrue();

            LockManager.unlock(key);

            assertThat(LockManager.isLocked(key)).isFalse();

            LockManager.assertSize(0);
        }

        LockManager.assertSize(0);
        LockManager.assertSize(0);
    }

    @Test
    public void testLockUnlockSequence2() {
        Key key = Key.of("key");

        for (int i = 0; i < 10; i++) {
            LockManager.assertSize(0);

            assertThat(LockManager.isLocked(key)).isFalse();

            LockManager.lock(key);

            LockManager.assertSize(1);

            assertThat(LockManager.isLocked(key)).isTrue();

            LockManager.unlock(key);

            assertThat(LockManager.isLocked(key)).isFalse();

            LockManager.assertSize(0);
        }

        LockManager.assertSize(0);
        LockManager.assertSize(0);
    }

    @Test
    public void testUnlockWithoutLock() {
        assertThatExceptionOfType(IllegalMonitorStateException.class).isThrownBy(() -> LockManager.unlock("key"));

        LockManager.assertSize(0);
        LockManager.assertSize(0);
    }

    @Test
    public void testMultiLockUnlock() {
        String key = "key";

        LockManager.lock(key);

        LockManager.assertSize(1);

        LockManager.lock(key);

        LockManager.assertSize(1);

        LockManager.lock(key);

        LockManager.assertSize(1);

        assertThat(LockManager.tryLock(key)).isTrue();
        assertThat(LockManager.tryLock(key)).isTrue();
        assertThat(LockManager.tryLock(key)).isTrue();

        LockManager.assertSize(1);

        assertThat(LockManager.isLocked(key)).isTrue();

        LockManager.assertSize(1);

        LockManager.unlock(key);

        LockManager.assertSize(0);

        assertThatExceptionOfType(IllegalMonitorStateException.class).isThrownBy(() -> LockManager.unlock(key));

        assertThat(LockManager.isLocked(key)).isFalse();

        LockManager.assertSize(0);
        LockManager.assertSize(0);
    }

    @Test
    public void testMultithreading() throws InterruptedException {
        int threadCount = 100;
        UUID uuid = UUID.randomUUID();
        AtomicInteger atomicInteger = new AtomicInteger();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                LockManager.lock(uuid.toString());
                try {
                    System.out.printf(
                            "[%s] locked by thread   [%-9s]%n",
                            uuid, Thread.currentThread().getName());

                    assertThat(atomicInteger.incrementAndGet()).isEqualTo(1);

                    try {
                        Thread.sleep(RandomSupport.randomLong(0, 200));
                    } catch (InterruptedException e) {
                        // INTENTIONALLY BLANK
                    }
                } finally {
                    assertThat(atomicInteger.decrementAndGet()).isEqualTo(0);

                    System.out.printf(
                            "[%s] unlocked by thread [%-9s]%n",
                            uuid, Thread.currentThread().getName());
                    LockManager.unlock(uuid.toString());
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

        LockManager.assertSize(0);
    }

    @Test
    public void testDifferentLockUnlockThreads() throws InterruptedException {
        String key = "key";
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread lockThread = new Thread(() -> {
            LockManager.lock(key);

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                // INTENTIONALLY BLANK
            }

            LockManager.unlock(key);
        });
        lockThread.start();

        Thread unlockThread = new Thread(() -> {
            assertThatExceptionOfType(IllegalMonitorStateException.class).isThrownBy(() -> LockManager.unlock(key));
            countDownLatch.countDown();
        });
        unlockThread.start();
        unlockThread.join();

        lockThread.join();

        LockManager.assertSize(0);
    }
}
