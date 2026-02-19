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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KeyedLatchManager Tests")
public class KeyedLatchManagerTest {

    @Test
    @DisplayName("Should create latch with given count")
    public void testCreateLatch() {
        String key = "testLatch";

        CountDownLatch latch = KeyedLatchManager.createLatch(key, 3);
        assertThat(latch).isNotNull();
        assertThat(latch.getCount()).isEqualTo(3);

        KeyedLatchManager.assertSize(1);
        assertThat(KeyedLatchManager.hasLatch(key)).isTrue();

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate latch")
    public void testCreateDuplicateLatch() {
        String key = "duplicateLatch";

        KeyedLatchManager.createLatch(key, 1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedLatchManager.createLatch(key, 1))
                .withMessageContaining("already exists");

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should get existing latch")
    public void testGetLatch() {
        String key = "getLatchKey";

        CountDownLatch created = KeyedLatchManager.createLatch(key, 2);
        CountDownLatch retrieved = KeyedLatchManager.getLatch(key);

        assertThat(retrieved).isSameAs(created);

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should return null for non-existent latch")
    public void testGetNonExistentLatch() {
        assertThat(KeyedLatchManager.getLatch("nonExistent")).isNull();
    }

    @Test
    @DisplayName("Should count down latch")
    public void testCountDown() {
        String key = "countDownKey";

        KeyedLatchManager.createLatch(key, 3);
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(3);

        KeyedLatchManager.countDown(key);
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(2);

        KeyedLatchManager.countDown(key);
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(1);

        KeyedLatchManager.countDown(key);
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(0);

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should await latch")
    public void testAwait() throws InterruptedException {
        String key = "awaitKey";
        AtomicBoolean threadCompleted = new AtomicBoolean(false);

        KeyedLatchManager.createLatch(key, 2);

        Thread waiter = new Thread(() -> {
            try {
                KeyedLatchManager.await(key);
                threadCompleted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        Thread.sleep(100);
        assertThat(threadCompleted.get()).isFalse();

        KeyedLatchManager.countDown(key);
        Thread.sleep(100);
        assertThat(threadCompleted.get()).isFalse();

        KeyedLatchManager.countDown(key);
        waiter.join(1000);
        assertThat(threadCompleted.get()).isTrue();

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should await latch with timeout")
    public void testAwaitWithTimeout() throws InterruptedException {
        String key = "awaitTimeoutKey";

        KeyedLatchManager.createLatch(key, 1);

        // Test timeout expiry
        boolean result = KeyedLatchManager.await(key, 100, TimeUnit.MILLISECONDS);
        assertThat(result).isFalse();
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(1);

        // Test successful await
        KeyedLatchManager.countDown(key);
        result = KeyedLatchManager.await(key, 100, TimeUnit.MILLISECONDS);
        assertThat(result).isTrue();

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should handle concurrent access")
    public void testConcurrentAccess() throws InterruptedException {
        String key = "concurrentKey";
        int threadCount = 5;

        KeyedLatchManager.createLatch(key, 1);

        AtomicBoolean[] completed = new AtomicBoolean[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            completed[i] = new AtomicBoolean(false);
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    KeyedLatchManager.await(key);
                    completed[index].set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }

        Thread.sleep(100);

        for (AtomicBoolean flag : completed) {
            assertThat(flag.get()).isFalse();
        }

        KeyedLatchManager.countDown(key);

        for (Thread thread : threads) {
            thread.join(1000);
        }

        for (AtomicBoolean flag : completed) {
            assertThat(flag.get()).isTrue();
        }

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should remove latch")
    public void testRemoveLatch() {
        String key = "removeKey";

        KeyedLatchManager.createLatch(key, 1);
        KeyedLatchManager.assertSize(1);

        CountDownLatch removed = KeyedLatchManager.removeLatch(key);
        assertThat(removed).isNotNull();
        assertThat(removed.getCount()).isEqualTo(1);

        KeyedLatchManager.assertSize(0);
        assertThat(KeyedLatchManager.hasLatch(key)).isFalse();
    }

    @Test
    @DisplayName("Should return null when removing non-existent latch")
    public void testRemoveNonExistentLatch() {
        CountDownLatch removed = KeyedLatchManager.removeLatch("nonExistent");
        assertThat(removed).isNull();
    }

    @Test
    @DisplayName("Should check if latch exists")
    public void testHasLatch() {
        String key = "hasLatchKey";

        assertThat(KeyedLatchManager.hasLatch(key)).isFalse();

        KeyedLatchManager.createLatch(key, 1);
        assertThat(KeyedLatchManager.hasLatch(key)).isTrue();

        KeyedLatchManager.removeLatch(key);
        assertThat(KeyedLatchManager.hasLatch(key)).isFalse();
    }

    @Test
    @DisplayName("Should trim key when accessing latch")
    public void testKeyTrimming() {
        String key = "trimKey";

        KeyedLatchManager.createLatch(key, 1);
        assertThat(KeyedLatchManager.hasLatch(" " + key)).isTrue();
        assertThat(KeyedLatchManager.hasLatch(key + " ")).isTrue();
        assertThat(KeyedLatchManager.hasLatch(" " + key + " ")).isTrue();

        KeyedLatchManager.assertSize(1);

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception for null key")
    public void testNullKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedLatchManager.createLatch(null, 1))
                .withMessage("key is null");
    }

    @Test
    @DisplayName("Should throw exception for blank key")
    public void testBlankKey() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedLatchManager.createLatch("   ", 1))
                .withMessage("key is blank");
    }

    @Test
    @DisplayName("Should throw exception for negative count")
    public void testNegativeCount() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> KeyedLatchManager.createLatch("key", -1))
                .withMessage("count cannot be negative");
    }

    @Test
    @DisplayName("Should create latch with zero count")
    public void testZeroCount() {
        String key = "zeroCountKey";

        KeyedLatchManager.createLatch(key, 0);
        assertThat(KeyedLatchManager.getCount(key)).isEqualTo(0);

        // Should not block since count is already 0
        boolean result = false;
        try {
            result = KeyedLatchManager.await(key, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(result).isTrue();

        KeyedLatchManager.removeLatch(key);
        KeyedLatchManager.assertSize(0);
    }

    @Test
    @DisplayName("Should throw exception for operations on non-existent latch")
    public void testOperationsOnNonExistentLatch() {
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedLatchManager.countDown("nonExistent"))
                .withMessageContaining("No latch exists");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedLatchManager.await("nonExistent"))
                .withMessageContaining("No latch exists");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> KeyedLatchManager.getCount("nonExistent"))
                .withMessageContaining("No latch exists");
    }

    @Test
    @DisplayName("Should handle multiple keys")
    public void testMultipleKeys() {
        String key1 = "latch1";
        String key2 = "latch2";
        String key3 = "latch3";

        KeyedLatchManager.createLatch(key1, 1);
        KeyedLatchManager.createLatch(key2, 2);
        KeyedLatchManager.createLatch(key3, 3);

        KeyedLatchManager.assertSize(3);

        assertThat(KeyedLatchManager.getCount(key1)).isEqualTo(1);
        assertThat(KeyedLatchManager.getCount(key2)).isEqualTo(2);
        assertThat(KeyedLatchManager.getCount(key3)).isEqualTo(3);

        KeyedLatchManager.removeLatch(key2);
        KeyedLatchManager.assertSize(2);

        KeyedLatchManager.removeLatch(key1);
        KeyedLatchManager.removeLatch(key3);
        KeyedLatchManager.assertSize(0);
    }
}
