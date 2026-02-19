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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KeyedSemaphoreManager Tests")
public class KeyedSemaphoreManagerTest {

    @Test
    @DisplayName("Should create semaphore with given permits")
    public void testCreateSemaphore() {
        String key = "testSemaphore";

        Semaphore semaphore = KeyedSemaphoreManager.createSemaphore(key, 3);
        assertThat(semaphore).isNotNull();
        assertThat(semaphore.availablePermits()).isEqualTo(3);

        KeyedSemaphoreManager.assertSize(1);
        assertThat(KeyedSemaphoreManager.hasSemaphore(key)).isTrue();

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should create semaphore with fairness")
    public void testCreateSemaphoreWithFairness() {
        String key = "fairSemaphore";

        Semaphore semaphore = KeyedSemaphoreManager.createSemaphore(key, 2, true);
        assertThat(semaphore).isNotNull();
        assertThat(semaphore.availablePermits()).isEqualTo(2);
        assertThat(semaphore.isFair()).isTrue();

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate semaphore")
    public void testCreateDuplicateSemaphore() {
        String key = "duplicateSemaphore";

        KeyedSemaphoreManager.createSemaphore(key, 1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.createSemaphore(key, 1))
                .withMessageContaining("already exists");

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should get existing semaphore")
    public void testGetSemaphore() {
        String key = "getSemaphoreKey";

        Semaphore created = KeyedSemaphoreManager.createSemaphore(key, 2);
        Semaphore retrieved = KeyedSemaphoreManager.getSemaphore(key);

        assertThat(retrieved).isSameAs(created);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should return null for non-existent semaphore")
    public void testGetNonExistentSemaphore() {
        assertThat(KeyedSemaphoreManager.getSemaphore("nonExistent")).isNull();
    }

    @Test
    @DisplayName("Should acquire and release permits")
    public void testAcquireAndRelease() throws InterruptedException {
        String key = "acquireReleaseKey";

        KeyedSemaphoreManager.createSemaphore(key, 2);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(2);

        KeyedSemaphoreManager.acquire(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(1);

        KeyedSemaphoreManager.acquire(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(0);

        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(1);

        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(2);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should acquire multiple permits")
    public void testAcquireMultiple() throws InterruptedException {
        String key = "acquireMultipleKey";

        KeyedSemaphoreManager.createSemaphore(key, 5);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(5);

        KeyedSemaphoreManager.acquire(key, 3);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(2);

        KeyedSemaphoreManager.release(key, 3);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(5);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should try to acquire permit")
    public void testTryAcquire() {
        String key = "tryAcquireKey";

        KeyedSemaphoreManager.createSemaphore(key, 1);

        assertThat(KeyedSemaphoreManager.tryAcquire(key)).isTrue();
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(0);

        assertThat(KeyedSemaphoreManager.tryAcquire(key)).isFalse();

        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.tryAcquire(key)).isTrue();

        KeyedSemaphoreManager.release(key);
        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should try to acquire permit with timeout")
    public void testTryAcquireWithTimeout() throws InterruptedException {
        String key = "tryAcquireTimeoutKey";

        KeyedSemaphoreManager.createSemaphore(key, 1);

        assertThat(KeyedSemaphoreManager.tryAcquire(key, 100, TimeUnit.MILLISECONDS))
                .isTrue();
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(0);

        assertThat(KeyedSemaphoreManager.tryAcquire(key, 100, TimeUnit.MILLISECONDS))
                .isFalse();

        KeyedSemaphoreManager.release(key);
        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should drain all permits")
    public void testDrainPermits() {
        String key = "drainKey";

        KeyedSemaphoreManager.createSemaphore(key, 5);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(5);

        int drained = KeyedSemaphoreManager.drainPermits(key);
        assertThat(drained).isEqualTo(5);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(0);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle concurrent access")
    public void testConcurrentAccess() throws InterruptedException {
        String key = "concurrentKey";
        int maxConcurrent = 3;
        int threadCount = 10;

        KeyedSemaphoreManager.createSemaphore(key, maxConcurrent);

        AtomicInteger currentConcurrent = new AtomicInteger(0);
        AtomicInteger maxObservedConcurrent = new AtomicInteger(0);

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    KeyedSemaphoreManager.acquire(key);

                    int current = currentConcurrent.incrementAndGet();
                    maxObservedConcurrent.updateAndGet(max -> Math.max(max, current));

                    Thread.sleep(50);

                    currentConcurrent.decrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    KeyedSemaphoreManager.release(key);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertThat(maxObservedConcurrent.get()).isLessThanOrEqualTo(maxConcurrent);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(maxConcurrent);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should remove semaphore")
    public void testRemoveSemaphore() {
        String key = "removeKey";

        KeyedSemaphoreManager.createSemaphore(key, 1);
        KeyedSemaphoreManager.assertSize(1);

        Semaphore removed = KeyedSemaphoreManager.removeSemaphore(key);
        assertThat(removed).isNotNull();
        assertThat(removed.availablePermits()).isEqualTo(1);

        KeyedSemaphoreManager.assertSize(0);
        assertThat(KeyedSemaphoreManager.hasSemaphore(key)).isFalse();
    }

    @Test
    @DisplayName("Should return null when removing non-existent semaphore")
    public void testRemoveNonExistentSemaphore() {
        Semaphore removed = KeyedSemaphoreManager.removeSemaphore("nonExistent");
        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("Should check if semaphore exists")
    public void testHasSemaphore() {
        String key = "hasSemaphoreKey";

        assertThat(KeyedSemaphoreManager.hasSemaphore(key)).isFalse();

        KeyedSemaphoreManager.createSemaphore(key, 1);
        assertThat(KeyedSemaphoreManager.hasSemaphore(key)).isTrue();

        KeyedSemaphoreManager.removeSemaphore(key);
        assertThat(KeyedSemaphoreManager.hasSemaphore(key)).isFalse();
    }

    @Test
    @DisplayName("Should trim key when accessing semaphore")
    public void testKeyTrimming() {
        String key = "trimKey";

        KeyedSemaphoreManager.createSemaphore(key, 1);
        assertThat(KeyedSemaphoreManager.hasSemaphore(" " + key)).isTrue();
        assertThat(KeyedSemaphoreManager.hasSemaphore(key + " ")).isTrue();
        assertThat(KeyedSemaphoreManager.hasSemaphore(" " + key + " ")).isTrue();

        KeyedSemaphoreManager.assertSize(1);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception for null key")
    public void testNullKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.createSemaphore(null, 1))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key")
    public void testBlankKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.createSemaphore("   ", 1))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for negative permits")
    public void testNegativePermits() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.createSemaphore("key", -1))
                .withMessage("permits cannot be negative");
    }

    @Test
    @DisplayName("Should create semaphore with zero permits")
    public void testZeroPermits() {
        String key = "zeroPermitsKey";

        KeyedSemaphoreManager.createSemaphore(key, 0);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(0);

        assertThat(KeyedSemaphoreManager.tryAcquire(key)).isFalse();

        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(1);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception for operations on non-existent semaphore")
    public void testOperationsOnNonExistentSemaphore() {
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.acquire("nonExistent"))
                .withMessageContaining("No semaphore exists");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.release("nonExistent"))
                .withMessageContaining("No semaphore exists");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedSemaphoreManager.availablePermits("nonExistent"))
                .withMessageContaining("No semaphore exists");
    }

    @Test
    @DisplayName("Should handle multiple keys")
    public void testMultipleKeys() {
        String key1 = "semaphore1";
        String key2 = "semaphore2";
        String key3 = "semaphore3";

        KeyedSemaphoreManager.createSemaphore(key1, 1);
        KeyedSemaphoreManager.createSemaphore(key2, 2);
        KeyedSemaphoreManager.createSemaphore(key3, 3);

        KeyedSemaphoreManager.assertSize(3);

        assertThat(KeyedSemaphoreManager.availablePermits(key1)).isEqualTo(1);
        assertThat(KeyedSemaphoreManager.availablePermits(key2)).isEqualTo(2);
        assertThat(KeyedSemaphoreManager.availablePermits(key3)).isEqualTo(3);

        KeyedSemaphoreManager.removeSemaphore(key2);
        KeyedSemaphoreManager.assertSize(2);

        KeyedSemaphoreManager.removeSemaphore(key1);
        KeyedSemaphoreManager.removeSemaphore(key3);
        KeyedSemaphoreManager.assertSize(0);
    }

    @Test
    @DisplayName("Should increase permits on release")
    public void testReleaseIncreasesPermits() {
        String key = "releaseKey";

        KeyedSemaphoreManager.createSemaphore(key, 1);

        // Release can increase permits beyond initial count
        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(2);

        KeyedSemaphoreManager.release(key);
        assertThat(KeyedSemaphoreManager.availablePermits(key)).isEqualTo(3);

        KeyedSemaphoreManager.removeSemaphore(key);
        KeyedSemaphoreManager.assertSize(0);
    }
}
