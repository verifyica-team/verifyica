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

@DisplayName("PlatformThreadFactory Tests")
class PlatformThreadFactoryTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create thread factory")
        void shouldCreateThreadFactory() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            assertThat(factory).isNotNull();
        }
    }

    @Nested
    @DisplayName("NewThread Tests")
    class NewThreadTests {

        @Test
        @DisplayName("Should create platform thread")
        void shouldCreatePlatformThread() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread).isNotNull().isInstanceOf(Thread.class);
        }

        @Test
        @DisplayName("Should create thread with provided runnable")
        void shouldCreateThreadWithProvidedRunnable() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = factory.newThread(() -> latch.countDown());
            thread.start();

            boolean completed = latch.await(1, TimeUnit.SECONDS);

            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("Should create thread in NEW state")
        void shouldCreateThreadInNewState() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getState()).isEqualTo(Thread.State.NEW);
        }

        @Test
        @DisplayName("Should create multiple distinct threads")
        void shouldCreateMultipleDistinctThreads() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread1 = factory.newThread(() -> {});
            Thread thread2 = factory.newThread(() -> {});
            Thread thread3 = factory.newThread(() -> {});

            assertThat(thread1).isNotSameAs(thread2).isNotSameAs(thread3);
            assertThat(thread2).isNotSameAs(thread3);
        }

        @Test
        @DisplayName("Should create threads with different IDs")
        void shouldCreateThreadsWithDifferentIds() {
            PlatformThreadFactory factory = new PlatformThreadFactory();
            Set<Long> threadIds = new HashSet<>();

            for (int i = 0; i < 10; i++) {
                Thread thread = factory.newThread(() -> {});
                threadIds.add(thread.getId());
            }

            assertThat(threadIds).hasSize(10);
        }

        @Test
        @DisplayName("Should create thread that can execute runnable")
        void shouldCreateThreadThatCanExecuteRunnable() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
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
        void shouldHandleNullRunnableGracefully() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            assertThatCode(() -> factory.newThread(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Thread Characteristics Tests")
    class ThreadCharacteristicsTests {

        @Test
        @DisplayName("Should create platform thread not virtual thread")
        void shouldCreatePlatformThreadNotVirtualThread() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(ThreadTool.isVirtual(thread)).isFalse();
        }

        @Test
        @DisplayName("Should create thread that can be started")
        void shouldCreateThreadThatCanBeStarted() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = factory.newThread(() -> latch.countDown());

            assertThatCode(() -> thread.start()).doesNotThrowAnyException();

            latch.await(1, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("Should create thread with default priority")
        void shouldCreateThreadWithDefaultPriority() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
        }

        @Test
        @DisplayName("Should create thread that is not daemon by default")
        void shouldCreateThreadThatIsNotDaemonByDefault() {
            PlatformThreadFactory factory = new PlatformThreadFactory();

            Thread thread = factory.newThread(() -> {});

            assertThat(thread.isDaemon()).isFalse();
        }
    }

    @Nested
    @DisplayName("Concurrent Usage Tests")
    class ConcurrentUsageTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent thread creation")
        void shouldBeThreadSafeForConcurrentThreadCreation() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
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
    class IntegrationTests {

        @Test
        @DisplayName("Should create threads that can run concurrently")
        void shouldCreateThreadsThatCanRunConcurrently() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
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
        void shouldCreateThreadsWithIndependentExecution() throws InterruptedException {
            PlatformThreadFactory factory = new PlatformThreadFactory();
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
    }
}
