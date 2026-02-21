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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KeyedMutexManager Tests")
public class KeyedMutexManagerTest {

    @Test
    @DisplayName("Should perform lock and unlock sequence")
    public void testLockUnlockSequence() {
        final String key = "testKey";

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
    @DisplayName("Should throw exception when unlocking without lock")
    public void testUnlockWithoutLock() {
        assertThatExceptionOfType(IllegalMonitorStateException.class)
                .isThrownBy(() -> KeyedMutexManager.unlock("noSuchKey"));

        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should support reentrant locking")
    public void testReentrantLocking() {
        final String key = "reentrantKey";

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
    @DisplayName("Should try to acquire lock")
    public void testTryLock() {
        final String key = "tryLockKey";

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
    @DisplayName("Should try to acquire lock with timeout")
    public void testTryLockWithTimeout() throws InterruptedException {
        final String key = "tryLockTimeoutKey";

        assertThat(KeyedMutexManager.tryLock(key, 100, TimeUnit.MILLISECONDS)).isTrue();
        KeyedMutexManager.assertSize(1);
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle concurrent access")
    public void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 50;
        final String key = "concurrentKey";
        final AtomicInteger counter = new AtomicInteger();

        final Thread[] threads = new Thread[threadCount];
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

        final List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < threads.length; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        for (final int index : indices) {
            threads[index].start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        assertThat(counter).hasValue(0);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should prevent different threads from unlocking")
    public void testDifferentThreadsCannotUnlock() throws InterruptedException {
        final String key = "differentThreadKey";
        final CountDownLatch latch = new CountDownLatch(1);

        final Thread lockThread = new Thread(() -> {
            KeyedMutexManager.lock(key);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            KeyedMutexManager.unlock(key);
        });
        lockThread.start();

        final Thread unlockThread = new Thread(() -> {
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
    @DisplayName("Should trim key when accessing mutex")
    public void testKeyTrimming() {
        final String key = "trimKey";

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
    @DisplayName("Should throw exception for null key on lock")
    public void testNullKeyOnLock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock(null))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key on lock")
    public void testBlankKeyOnLock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock("   "))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for null key on tryLock")
    public void testNullKeyOnTryLock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.tryLock(null))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key on tryLock")
    public void testBlankKeyOnTryLock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.tryLock("   "))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for null key on tryLock with timeout")
    public void testNullKeyOnTryLockWithTimeout() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.tryLock(null, 100, TimeUnit.MILLISECONDS))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key on tryLock with timeout")
    public void testBlankKeyOnTryLockWithTimeout() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.tryLock("   ", 100, TimeUnit.MILLISECONDS))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for null timeUnit on tryLock with timeout")
    public void testNullTimeUnitOnTryLockWithTimeout() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.tryLock("key", 100, null))
                .withMessage("timeUnit is null");
    }

    @Test
    @DisplayName("Should throw exception for null key on unlock")
    public void testNullKeyOnUnlock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.unlock(null))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key on unlock")
    public void testBlankKeyOnUnlock() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.unlock("   "))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for null key on isLocked")
    public void testNullKeyOnIsLocked() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.isLocked(null))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key on isLocked")
    public void testBlankKeyOnIsLocked() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.isLocked("   "))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should return false for isLocked when key is not locked")
    public void testIsLockedReturnsFalseForUnlockedKey() {
        final String key = "unlockedKey";

        assertThat(KeyedMutexManager.isLocked(key)).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple keys")
    public void testMultipleKeys() {
        final String key1 = "key1";
        final String key2 = "key2";
        final String key3 = "key3";

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
    @DisplayName("Should fail tryLock when mutex is held")
    public void testTryLockFailure() throws InterruptedException {
        final String key = "contendedKey";
        final CountDownLatch threadStarted = new CountDownLatch(1);
        final CountDownLatch canRelease = new CountDownLatch(1);

        final Thread holder = new Thread(() -> {
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

    @Test
    @DisplayName("Should cleanup mutex reference after all threads release")
    public void testCleanupAfterAllThreadsRelease() throws InterruptedException {
        final String key = "cleanupKey";
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger();

        final Thread thread = new Thread(() -> {
            KeyedMutexManager.lock(key);
            try {
                result.set(42);
            } finally {
                KeyedMutexManager.unlock(key);
            }
            latch.countDown();
        });
        thread.start();

        latch.await();

        // After thread completes and unlocks, the mutex reference should be cleaned up
        KeyedMutexManager.assertSize(0);
        assertThat(result).hasValue(42);
    }

    @Test
    @DisplayName("Should throw exception when unlocking already unlocked key")
    public void testUnlockAlreadyUnlockedKey() {
        final String key = "unlockTwiceKey";

        KeyedMutexManager.lock(key);
        KeyedMutexManager.unlock(key);

        // Second unlock should fail since key is no longer locked
        assertThatExceptionOfType(IllegalMonitorStateException.class)
                .isThrownBy(() -> KeyedMutexManager.unlock(key))
                .withMessageContaining("is not locked");
    }

    @Test
    @DisplayName("Should handle tryLock with zero timeout")
    public void testTryLockWithZeroTimeout() throws InterruptedException {
        final String key = "zeroTimeoutKey";

        assertThat(KeyedMutexManager.tryLock(key, 0, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle tryLock with very long timeout")
    public void testTryLockWithLongTimeout() throws InterruptedException {
        final String key = "longTimeoutKey";

        assertThat(KeyedMutexManager.tryLock(key, 1, TimeUnit.HOURS)).isTrue();
        assertThat(KeyedMutexManager.isLocked(key)).isTrue();

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle reentrant tryLock with timeout")
    public void testReentrantTryLockWithTimeout() throws InterruptedException {
        final String key = "reentrantTimeoutKey";

        KeyedMutexManager.lock(key);
        KeyedMutexManager.assertSize(1);

        // Reentrant tryLock with timeout should succeed
        assertThat(KeyedMutexManager.tryLock(key, 100, TimeUnit.MILLISECONDS)).isTrue();
        KeyedMutexManager.assertSize(1);

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle assertSize with incorrect size")
    public void testAssertSizeWithIncorrectSize() {
        final String key = "assertSizeKey";

        KeyedMutexManager.lock(key);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedMutexManager.assertSize(0))
                .withMessage("mutexReferences size is incorrect");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedMutexManager.assertSize(2))
                .withMessage("mutexReferences size is incorrect");

        KeyedMutexManager.unlock(key);
        KeyedMutexManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle empty string key")
    public void testEmptyStringKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock(""))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should handle tab and newline in blank key")
    public void testTabAndNewlineInBlankKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedMutexManager.lock("\t\n"))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should handle concurrent tryLock attempts")
    public void testConcurrentTryLock() throws InterruptedException {
        final int threadCount = 20;
        final String key = "concurrentTryLockKey";
        final AtomicInteger successCount = new AtomicInteger();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(threadCount);

        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await();
                    if (KeyedMutexManager.tryLock(key)) {
                        try {
                            successCount.incrementAndGet();
                            Thread.sleep(10);
                        } finally {
                            KeyedMutexManager.unlock(key);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
            threads[i].start();
        }

        startLatch.countDown();
        completeLatch.await();

        // Only one thread should have successfully acquired the lock
        assertThat(successCount).hasValue(1);
        KeyedMutexManager.assertSize(0);
    }
}
