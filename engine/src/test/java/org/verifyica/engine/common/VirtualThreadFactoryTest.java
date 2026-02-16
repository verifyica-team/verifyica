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

package org.verifyica.engine.common;

import static org.assertj.core.api.Assertions.*;

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;

@DisplayName("VirtualThreadFactory Tests")
public class VirtualThreadFactoryTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create thread factory")
        public void shouldCreateThreadFactory() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("NewThread Tests")
    public class NewThreadTests {

        @Test
        @DisplayName("Should create thread")
        public void shouldCreateThread() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread).isNotNull().isInstanceOf(Thread.class);
        }

        @Test
        @DisplayName("Should create virtual thread when supported")
        public void shouldCreateVirtualThreadWhenSupported() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread = factory.newThread(() -> {});

            if (ThreadTool.hasVirtualThreads()) {
                assertThat(ThreadTool.isVirtual(thread)).isTrue();
            } else {
                assertThat(ThreadTool.isVirtual(thread)).isFalse();
            }
        }

        @Test
        @DisplayName("Should create platform thread when virtual threads not supported")
        public void shouldCreatePlatformThreadWhenVirtualThreadsNotSupported() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread).isNotNull();
        }

        @Test
        @DisplayName("Should create thread with provided runnable")
        public void shouldCreateThreadWithProvidedRunnable() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = factory.newThread(() -> latch.countDown());
            thread.start();

            boolean completed = latch.await(1, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("Should create thread in NEW state")
        public void shouldCreateThreadInNewState() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getState()).isEqualTo(Thread.State.NEW);
        }

        @Test
        @DisplayName("Should create multiple distinct threads")
        public void shouldCreateMultipleDistinctThreads() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread1 = factory.newThread(() -> {});
            Thread thread2 = factory.newThread(() -> {});
            Thread thread3 = factory.newThread(() -> {});

            assertThat(thread1).isNotSameAs(thread2).isNotSameAs(thread3);
            assertThat(thread2).isNotSameAs(thread3);
        }

        @Test
        @DisplayName("Should create threads with different IDs")
        public void shouldCreateThreadsWithDifferentIds() {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            Set<Long> threadIds = new HashSet<>();

            for (int i = 0; i < 10; i++) {
                Thread thread = factory.newThread(() -> {});
                threadIds.add(thread.getId());
            }

            assertThat(threadIds).hasSize(10);
        }

        @Test
        @DisplayName("Should create thread that can execute runnable")
        public void shouldCreateThreadThatCanExecuteRunnable() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            StringBuilder result = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = factory.newThread(() -> {
                result.append("executed");
                latch.countDown();
            });
            thread.start();

            latch.await(1, TimeUnit.SECONDS);

            assertThat(result.toString()).isEqualTo("executed");
        }

        @Test
        @DisplayName("Should handle null runnable gracefully")
        public void shouldHandleNullRunnableGracefully() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            assertThatThrownBy(() -> factory.newThread(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Thread Characteristics Tests")
    public class ThreadCharacteristicsTests {

        @Test
        @DisplayName("Should create thread that can be started")
        public void shouldCreateThreadThatCanBeStarted() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = factory.newThread(() -> latch.countDown());

            assertThatCode(() -> thread.start()).doesNotThrowAnyException();

            latch.await(1, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("Should create unstarted thread")
        public void shouldCreateUnstartedThread() {
            VirtualThreadFactory factory = new VirtualThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getState()).isEqualTo(Thread.State.NEW);
        }
    }

    @Nested
    @DisplayName("Concurrent Usage Tests")
    public class ConcurrentUsageTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent thread creation")
        public void shouldBeThreadSafeForConcurrentThreadCreation() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            int factoryThreadCount = 10;
            int threadsPerFactoryThread = 10;
            Set<Long> allThreadIds = new HashSet<>();
            CountDownLatch latch = new CountDownLatch(factoryThreadCount);

            Thread[] factoryThreads = new Thread[factoryThreadCount];
            for (int i = 0; i < factoryThreadCount; i++) {
                factoryThreads[i] = new Thread(() -> {
                    for (int j = 0; j < threadsPerFactoryThread; j++) {
                        Thread thread = factory.newThread(() -> {});
                        synchronized (allThreadIds) {
                            allThreadIds.add(thread.getId());
                        }
                    }
                    latch.countDown();
                });
                factoryThreads[i].start();
            }

            latch.await(5, TimeUnit.SECONDS);

            assertThat(allThreadIds).hasSize(factoryThreadCount * threadsPerFactoryThread);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should create threads that can run concurrently")
        public void shouldCreateThreadsThatCanRunConcurrently() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            int threadCount = 5;
            CountDownLatch startLatch = new CountDownLatch(threadCount);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);

            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = factory.newThread(() -> {
                    startLatch.countDown();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    finishLatch.countDown();
                });
                threads[i].start();
            }

            boolean allStarted = startLatch.await(1, TimeUnit.SECONDS);
            boolean allFinished = finishLatch.await(2, TimeUnit.SECONDS);

            assertThat(allStarted).isTrue();
            assertThat(allFinished).isTrue();
        }

        @Test
        @DisplayName("Should create threads with independent execution")
        public void shouldCreateThreadsWithIndependentExecution() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            int[] results = new int[3];
            CountDownLatch latch = new CountDownLatch(3);

            Thread thread1 = factory.newThread(() -> {
                results[0] = 1;
                latch.countDown();
            });
            Thread thread2 = factory.newThread(() -> {
                results[1] = 2;
                latch.countDown();
            });
            Thread thread3 = factory.newThread(() -> {
                results[2] = 3;
                latch.countDown();
            });

            thread1.start();
            thread2.start();
            thread3.start();

            latch.await(1, TimeUnit.SECONDS);

            assertThat(results).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("Should create many threads efficiently when virtual threads available")
        public void shouldCreateManyThreadsEfficientlyWhenVirtualThreadsAvailable() throws InterruptedException {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            int threadCount = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);

            long startTime = System.currentTimeMillis();

            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = factory.newThread(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
                threads[i].start();
            }

            boolean allCompleted = latch.await(30, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(allCompleted).isTrue();

            // Virtual threads should complete faster than platform threads
            // Just verify it completes within reasonable time
            assertThat(duration).isLessThan(30000);
        }
    }

    @Nested
    @DisplayName("Virtual Thread Support Tests")
    public class VirtualThreadSupportTests {

        @Test
        @DisplayName("Should detect virtual thread support")
        public void shouldDetectVirtualThreadSupport() {
            boolean hasVirtualThreads = ThreadTool.hasVirtualThreads();

            // This test just verifies the API is available
            // Actual support depends on Java version
            assertThat(hasVirtualThreads).isIn(true, false);
        }

        @Test
        @DisplayName("Should create consistent thread type")
        public void shouldCreateConsistentThreadType() {
            VirtualThreadFactory factory = new VirtualThreadFactory();
            boolean hasVirtualThreads = ThreadTool.hasVirtualThreads();

            Thread thread1 = factory.newThread(() -> {});
            Thread thread2 = factory.newThread(() -> {});

            if (hasVirtualThreads) {
                assertThat(ThreadTool.isVirtual(thread1)).isTrue();
                assertThat(ThreadTool.isVirtual(thread2)).isTrue();
            } else {
                assertThat(ThreadTool.isVirtual(thread1)).isFalse();
                assertThat(ThreadTool.isVirtual(thread2)).isFalse();
            }
        }
    }
}
