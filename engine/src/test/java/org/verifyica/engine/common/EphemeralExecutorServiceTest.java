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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();

            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThat(executor.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when thread factory is null")
        public void shouldThrowExceptionWhenThreadFactoryIsNull() {
            assertThatThrownBy(() -> new EphemeralExecutorService(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("threadFactory is null");
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    public class ExecuteTests {

        @Test
        @DisplayName("Should execute runnable in new thread")
        public void shouldExecuteRunnableInNewThread() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final Thread callingThread = Thread.currentThread();
            final AtomicBoolean differentThread = new AtomicBoolean(false);
            final CountDownLatch latch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final int taskCount = 5;
            final CountDownLatch latch = new CountDownLatch(taskCount);
            final AtomicInteger counter = new AtomicInteger(0);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final AtomicBoolean isDaemon = new AtomicBoolean(false);
            final CountDownLatch latch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");
        }

        @Test
        @DisplayName("Should throw exception when runnable is null")
        public void shouldThrowExceptionWhenRunnableIsNull() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThatThrownBy(() -> executor.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("runnable is null");
        }

        @Test
        @DisplayName("Should throw exception when thread factory returns null")
        public void shouldThrowExceptionWhenThreadFactoryReturnsNull() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return null;
                }
            };

            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("thread is null");
        }

        @Test
        @DisplayName("Should throw RejectedExecutionException when thread fails to start")
        public void shouldThrowRejectedExecutionExceptionWhenThreadFailsToStart() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r) {
                        @Override
                        public synchronized void start() {
                            throw new OutOfMemoryError("Unable to create native thread");
                        }
                    };
                }
            };

            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessageContaining("Failed to start thread")
                    .hasCauseInstanceOf(OutOfMemoryError.class);
        }

        @Test
        @DisplayName("Should track running threads")
        public void shouldTrackRunningThreads() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch startLatch = new CountDownLatch(3);
            final CountDownLatch finishLatch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should allow running tasks to complete")
        public void shouldAllowRunningTasksToComplete() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final AtomicBoolean completed = new AtomicBoolean(false);
            final CountDownLatch startLatch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdownNow();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should interrupt running threads")
        public void shouldInterruptRunningThreads() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final AtomicBoolean interrupted = new AtomicBoolean(false);
            final CountDownLatch startLatch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            final List<Runnable> remaining = executor.shutdownNow();

            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdownNow();
            executor.shutdownNow();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should interrupt all running threads when called multiple times")
        public void shouldInterruptAllRunningThreadsWhenCalledMultipleTimes() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final AtomicBoolean interrupted1 = new AtomicBoolean(false);
            final AtomicBoolean interrupted2 = new AtomicBoolean(false);
            final CountDownLatch startLatch = new CountDownLatch(2);

            executor.execute(() -> {
                startLatch.countDown();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    interrupted1.set(true);
                    Thread.currentThread().interrupt();
                }
            });

            executor.execute(() -> {
                startLatch.countDown();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    interrupted2.set(true);
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.await(1, TimeUnit.SECONDS);
            executor.shutdownNow();
            executor.shutdownNow();
            Thread.sleep(100);

            assertThat(interrupted1).isTrue();
            assertThat(interrupted2).isTrue();
        }
    }

    @Nested
    @DisplayName("IsTerminated Tests")
    public class IsTerminatedTests {

        @Test
        @DisplayName("Should be false initially")
        public void shouldBeFalseInitially() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            assertThat(executor.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("Should be true when shutdown with no running threads")
        public void shouldBeTrueWhenShutdownWithNoRunningThreads() {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should be false when shutdown with running threads")
        public void shouldBeFalseWhenShutdownWithRunningThreads() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch startLatch = new CountDownLatch(1);
            final CountDownLatch finishLatch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);

            executor.shutdown();
            final boolean terminated = executor.awaitTermination(1, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should wait for tasks to complete")
        public void shouldWaitForTasksToComplete() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    Thread.sleep(100);
                    latch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();
            final boolean terminated = executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
            assertThat(latch.getCount()).isZero();
        }

        @Test
        @DisplayName("Should return false when timeout expires")
        public void shouldReturnFalseWhenTimeoutExpires() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();
            final boolean terminated = executor.awaitTermination(100, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();

            latch.countDown();
        }

        @Test
        @DisplayName("Should handle very short timeout")
        public void shouldHandleVeryShortTimeout() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();
            // Use a very short timeout to test the spin loop path
            final boolean terminated = executor.awaitTermination(1, TimeUnit.NANOSECONDS);

            assertThat(terminated).isFalse();

            latch.countDown();
        }

        @Test
        @DisplayName("Should respond to interruption")
        public void shouldRespondToInterruption() throws InterruptedException {
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            executor.shutdown();

            final Thread testThread = new Thread(() -> {
                try {
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // Expected
                }
            });

            testThread.start();
            testThread.interrupt();
            testThread.join(1000);

            assertThat(testThread.isAlive()).isFalse();
            latch.countDown();
        }
    }

    @Nested
    @DisplayName("Thread Factory Tests")
    public class ThreadFactoryTests {

        @Test
        @DisplayName("Should use provided thread factory")
        public void shouldUseProvidedThreadFactory() throws InterruptedException {
            final AtomicBoolean factoryUsed = new AtomicBoolean(false);
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    factoryUsed.set(true);
                    return super.newThread(r);
                }
            };

            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final CountDownLatch latch = new CountDownLatch(1);

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
            final PlatformThreadFactory threadFactory = new PlatformThreadFactory();
            final EphemeralExecutorService executor = new EphemeralExecutorService(threadFactory);
            final int taskCount = 100;
            final CountDownLatch latch = new CountDownLatch(taskCount);
            final AtomicInteger counter = new AtomicInteger(0);

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
