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

package org.verifyica.api.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class KeyedMutexManagerTest {

    @Test
    public void testLockUnlockSequence() {
        String key = "testKey";

        for (int i = 0; i < 10; i++) {
            KeyedMutexManager.assertSize(0);
            assertThat(KeyedMutexManager.isLocked(key)).isFalse();

            KeyedMutexManager.lock(key);
            KeyedMutexManager.assertSize(1);
            assertThat(KeyedMutexManager.isLocked(key)).isTrue();

            KeyedMutexManager.unlock(key);
            assertThat(KeyedMutexManager.isLocked(key)).isFalse();

            KeyedMutexManager.assertSize(0);
        }

        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testUnlockWithoutLock() {
        assertThatExceptionOfType(IllegalMonitorStateException.class)
                .isThrownBy(() -> KeyedMutexManager.unlock("noSuchKey"));

        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testReentrantLocking() {
        String key = "reentrantKey";

        KeyedMutexManager.lock(key);
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.lock(key);
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.lock(key);
        KeyedMutexManager.assertSize(1);

        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        KeyedMutexManager.unlock(key);
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.unlock(key);
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.unlock(key);
        assertThat(KeyedMutexManager.isLocked(key)).isFalse();
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testTryLock() {
        String key = "tryLockKey";

        assertThat(KeyedMutexManager.tryLock(key)).isTrue();
        KeyedMutexManager.assertSize(1);
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        // Reentrant tryLock should succeed
        assertThat(KeyedMutexManager.tryLock(key)).isTrue();
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testTryLockWithTimeout() throws InterruptedException {
        String key = "tryLockTimeoutKey";

        assertThat(KeyedMutexManager.tryLock(key, 100, TimeUnit.MILLISECONDS)).isTrue();
        KeyedMutexManager.assertSize(1);
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int threadCount = 50;
        String key = "concurrentKey";
        AtomicInteger counter = new AtomicInteger();

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                KeyedMutexManager.lock(key);
                try {
                    assertThat(counter.incrementAndGet()).isEqualTo(1);

                    try {
                        Thread.sleep(new Random().nextInt(50));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    assertThat(counter.get()).isEqualTo(1);
                    assertThat(counter.decrementAndGet()).isEqualTo(0);
                } finally {
                    KeyedMutexManager.unlock(key);
                }
            });
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

        assertThat(counter).hasValue(0);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testDifferentThreadsCannotUnlock() throws InterruptedException {
        String key = "differentThreadKey";
        CountDownLatch latch = new CountDownLatch(1);

        Thread lockThread = new Thread(() -> {
            KeyedMutexManager.lock(key);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            KeyedMutexManager.unlock(key);
        });
        lockThread.start();

        Thread unlockThread = new Thread(() -> {
            assertThatExceptionOfType(IllegalMonitorStateException.class)
                    .isThrownBy(() -> KeyedMutexManager.unlock(key));
            latch.countDown();
        });
        unlockThread.start();
        unlockThread.join();

        lockThread.join();

        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testKeyTrimming() {
        String key = "trimKey";

        KeyedMutexManager.lock(key);
        KeyedMutexManager.lock(" " + key);
        KeyedMutexManager.lock(key + " ");
        KeyedMutexManager.lock(" " + key + " ");

        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.unlock(key);
        KeyedMutexManager.unlock(key);
        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testNullKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock(null))
                .withMessage("key is null");
    }

    @Test
    public void testBlankKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock("   "))
                .withMessage("key is blank");
    }

    @Test
    public void testMultipleKeys() {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";

        KeyedMutexManager.lock(key1);
        KeyedMutexManager.lock(key2);
        KeyedMutexManager.lock(key3);

        KeyedMutexManager.assertSize(3);

        assertThat(KeyedMutexManager.isLocked(key1)).isTrue();
        assertThat(KeyedMutexManager.isLocked(key2)).isTrue();
        assertThat(KeyedMutexManager.isLocked(key3)).isTrue();

        KeyedMutexManager.unlock(key2);
        KeyedMutexManager.assertSize(2);
        assertThat(KeyedMutexManager.isLocked(key2)).isFalse();

        KeyedMutexManager.unlock(key1);
        KeyedMutexManager.unlock(key3);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    public void testTryLockFailure() throws InterruptedException {
        String key = "contendedKey";
        CountDownLatch threadStarted = new CountDownLatch(1);
        CountDownLatch canRelease = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            KeyedMutexManager.lock(key);
            threadStarted.countDown();
            try {
                canRelease.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            KeyedMutexManager.unlock(key);
        });
        holder.start();

        threadStarted.await();

        // Try to acquire lock that's held by another thread
        assertThat(KeyedMutexManager.tryLock(key)).isFalse();
        assertThat(KeyedMutexManager.tryLock(key, 50, TimeUnit.MILLISECONDS)).isFalse();

        canRelease.countDown();
        holder.join();

        KeyedMutexManager.assertSize(0);
    }
}
