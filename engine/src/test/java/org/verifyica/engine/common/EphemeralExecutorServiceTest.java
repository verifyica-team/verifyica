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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

@DisplayName("EphemeralExecutorService Tests")
public class EphemeralExecutorServiceTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create executor with thread factory")
        public void shouldCreateExecutorWithThreadFactory() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();

            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThat(executor.isShutdown()).isFalse();
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    public class ExecuteTests {

        @Test
        @DisplayName("Should execute runnable in new thread")
        public void shouldExecuteRunnableInNewThread() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            Thread callingThread = Thread.currentThread();
            AtomicBoolean differentThread = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                differentThread.set(Thread.currentThread() != callingThread);
                latch.countDown();
            });

            latch.await(1, TimeUnit.SECONDS);

            assertThat(differentThread).isTrue();
        }

        @Test
        @DisplayName("Should execute multiple runnables concurrently")
        public void shouldExecuteMultipleRunnablesConcurrently() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            int taskCount = 5;
            CountDownLatch latch = new CountDownLatch(taskCount);
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            latch.await(2, TimeUnit.SECONDS);

            assertThat(counter.get()).isEqualTo(taskCount);
        }

        @Test
        @DisplayName("Should create daemon threads")
        public void shouldCreateDaemonThreads() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            AtomicBoolean isDaemon = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                isDaemon.set(Thread.currentThread().isDaemon());
                latch.countDown();
            });

            latch.await(1, TimeUnit.SECONDS);

            assertThat(isDaemon).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when executing after shutdown")
        public void shouldThrowExceptionWhenExecutingAfterShutdown() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");
        }

        @Test
        @DisplayName("Should track running threads")
        public void shouldTrackRunningThreads() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch startLatch = new CountDownLatch(3);
            CountDownLatch finishLatch = new CountDownLatch(1);

            for (int i = 0; i < 3; i++) {
                executor.execute(() -> {
                    startLatch.countDown();
                    try {
                        finishLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            startLatch.await(1, TimeUnit.SECONDS);
            assertThat(executor.isTerminated()).isFalse();

            finishLatch.countDown();
            Thread.sleep(100);
        }
    }

    @Nested
    @DisplayName("Shutdown Tests")
    public class ShutdownTests {

        @Test
        @DisplayName("Should change state to shutdown")
        public void shouldChangeStateToShutdown() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should allow running tasks to complete")
        public void shouldAllowRunningTasksToComplete() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            AtomicBoolean completed = new AtomicBoolean(false);
            CountDownLatch startLatch = new CountDownLatch(1);

            executor.execute(() -> {
                startLatch.countDown();
                try {
                    Thread.sleep(100);
                    completed.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.await(1, TimeUnit.SECONDS);
            executor.shutdown();
            Thread.sleep(200);

            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();
            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("ShutdownNow Tests")
    public class ShutdownNowTests {

        @Test
        @DisplayName("Should change state to shutdown")
        public void shouldChangeStateToShutdown() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdownNow();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should interrupt running threads")
        public void shouldInterruptRunningThreads() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            AtomicBoolean interrupted = new AtomicBoolean(false);
            CountDownLatch startLatch = new CountDownLatch(1);

            executor.execute(() -> {
                startLatch.countDown();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.await(1, TimeUnit.SECONDS);
            executor.shutdownNow();
            Thread.sleep(100);

            assertThat(interrupted).isTrue();
        }

        @Test
        @DisplayName("Should return empty list")
        public void shouldReturnEmptyList() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            List<Runnable> remaining = executor.shutdownNow();

            assertThat(remaining).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsTerminated Tests")
    public class IsTerminatedTests {

        @Test
        @DisplayName("Should be false initially")
        public void shouldBeFalseInitially() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThat(executor.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("Should be true when shutdown with no running threads")
        public void shouldBeTrueWhenShutdownWithNoRunningThreads() {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should be false when shutdown with running threads")
        public void shouldBeFalseWhenShutdownWithRunningThreads() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(1);

            executor.execute(() -> {
                startLatch.countDown();
                try {
                    finishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.await(1, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(executor.isTerminated()).isFalse();

            finishLatch.countDown();
        }

        @Test
        @DisplayName("Should become true after all threads complete")
        public void shouldBecomeTrueAfterAllThreadsComplete() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });

            executor.shutdown();
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(50);

            assertThat(executor.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("AwaitTermination Tests")
    public class AwaitTerminationTests {

        @Test
        @DisplayName("Should return true when no tasks running")
        public void shouldReturnTrueWhenNoTasksRunning() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();
            boolean terminated = executor.awaitTermination(1, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should wait for tasks to complete")
        public void shouldWaitForTasksToComplete() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    Thread.sleep(100);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();
            boolean terminated = executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
            assertThat(latch.getCount()).isZero();
        }

        @Test
        @DisplayName("Should return false when timeout expires")
        public void shouldReturnFalseWhenTimeoutExpires() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();
            boolean terminated = executor.awaitTermination(100, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();

            latch.countDown();
        }
    }

    @Nested
    @DisplayName("Thread Factory Tests")
    public class ThreadFactoryTests {

        @Test
        @DisplayName("Should use provided thread factory")
        public void shouldUseProvidedThreadFactory() throws InterruptedException {
            AtomicBoolean factoryUsed = new AtomicBoolean(false);
            PlatformThreadFactory threadFactory = new PlatformThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    factoryUsed.set(true);
                    return super.newThread(r);
                }
            };

            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> latch.countDown());

            latch.await(1, TimeUnit.SECONDS);

            assertThat(factoryUsed).isTrue();
        }
    }

    @Nested
    @DisplayName("Stress Tests")
    public class StressTests {

        @Test
        @DisplayName("Should handle many concurrent tasks")
        public void shouldHandleManyConcurrentTasks() throws InterruptedException {
            PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            int taskCount = 100;
            CountDownLatch latch = new CountDownLatch(taskCount);
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < taskCount; i++) {
                executor.execute(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            assertThat(counter.get()).isEqualTo(taskCount);
        }
    }
}
