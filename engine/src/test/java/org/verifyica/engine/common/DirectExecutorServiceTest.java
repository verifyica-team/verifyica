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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

@DisplayName("DirectExecutorService Tests")
public class DirectExecutorServiceTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create executor service in running state")
        public void shouldCreateExecutorServiceInRunningState() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isShutdown()).isFalse();
            assertThat(executor.isTerminated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    public class ExecuteTests {

        @Test
        @DisplayName("Should execute runnable immediately")
        public void shouldExecuteRunnableImmediately() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicBoolean executed = new AtomicBoolean(false);

            executor.execute(() -> executed.set(true));

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("Should execute runnable on calling thread")
        public void shouldExecuteRunnableOnCallingThread() {
            DirectExecutorService executor = new DirectExecutorService();
            Thread callingThread = Thread.currentThread();
            AtomicBoolean sameThread = new AtomicBoolean(false);

            executor.execute(() -> sameThread.set(Thread.currentThread() == callingThread));

            assertThat(sameThread).isTrue();
        }

        @Test
        @DisplayName("Should execute multiple runnables sequentially")
        public void shouldExecuteMultipleRunnablesSequentially() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should execute runnables in order")
        public void shouldExecuteRunnablesInOrder() {
            DirectExecutorService executor = new DirectExecutorService();
            StringBuilder result = new StringBuilder();

            executor.execute(() -> result.append("A"));
            executor.execute(() -> result.append("B"));
            executor.execute(() -> result.append("C"));

            assertThat(result.toString()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Should throw RejectedExecutionException after shutdown")
        public void shouldThrowRejectedExecutionExceptionAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThatThrownBy(() -> executor.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");
        }

        @Test
        @DisplayName("Should propagate exceptions from runnable")
        public void shouldPropagateExceptionsFromRunnable() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThatThrownBy(() -> executor.execute(() -> {
                        throw new RuntimeException("Test exception");
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when runnable is null")
        public void shouldThrowIllegalArgumentExceptionWhenRunnableIsNull() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThatThrownBy(() -> executor.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("runnable is null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null runnable after shutdown")
        public void shouldThrowIllegalArgumentExceptionForNullRunnableAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            assertThatThrownBy(() -> executor.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("runnable is null");
        }
    }

    @Nested
    @DisplayName("Shutdown Tests")
    public class ShutdownTests {

        @Test
        @DisplayName("Should change state to shutdown")
        public void shouldChangeStateToShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        public void shouldChangeStateToTerminated() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            DirectExecutorService executor = new DirectExecutorService();

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
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdownNow();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        public void shouldChangeStateToTerminated() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdownNow();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list")
        public void shouldReturnEmptyList() {
            DirectExecutorService executor = new DirectExecutorService();

            List<Runnable> remaining = executor.shutdownNow();

            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            DirectExecutorService executor = new DirectExecutorService();

            List<Runnable> first = executor.shutdownNow();
            List<Runnable> second = executor.shutdownNow();

            assertThat(first).isEmpty();
            assertThat(second).isEmpty();
            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should work after shutdown")
        public void shouldWorkAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();
            List<Runnable> remaining = executor.shutdownNow();

            assertThat(remaining).isEmpty();
            assertThat(executor.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("AwaitTermination Tests")
    public class AwaitTerminationTests {

        @Test
        @DisplayName("Should return true immediately after shutdown")
        public void shouldReturnTrueImmediatelyAfterShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            boolean terminated = executor.awaitTermination(1, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should return false when not shutdown")
        public void shouldReturnFalseWhenNotShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();

            boolean terminated = executor.awaitTermination(1, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should return immediately when already shutdown")
        public void shouldReturnImmediatelyWhenAlreadyShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            long startTime = System.currentTimeMillis();
            boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(terminated).isTrue();
            assertThat(duration).isLessThan(100);
        }

        @Test
        @DisplayName("Should return false with zero timeout when not shutdown")
        public void shouldReturnFalseWithZeroTimeoutWhenNotShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();

            boolean terminated = executor.awaitTermination(0, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should return true with zero timeout when already shutdown")
        public void shouldReturnTrueWithZeroTimeoutWhenAlreadyShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            boolean terminated = executor.awaitTermination(0, TimeUnit.MILLISECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should return false with negative timeout when not shutdown")
        public void shouldReturnFalseWithNegativeTimeoutWhenNotShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();

            boolean terminated = executor.awaitTermination(-1, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should throw InterruptedException when thread is interrupted")
        public void shouldThrowInterruptedExceptionWhenThreadIsInterrupted() {
            DirectExecutorService executor = new DirectExecutorService();

            Thread testThread = new Thread(() -> {
                try {
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // Expected
                }
            });
            testThread.start();
            testThread.interrupt();

            assertThat(testThread.isInterrupted()).isTrue();
        }
    }

    @Nested
    @DisplayName("State Tests")
    public class StateTests {

        @Test
        @DisplayName("Should not be shutdown initially")
        public void shouldNotBeShutdownInitially() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("Should not be terminated initially")
        public void shouldNotBeTerminatedInitially() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("Should be both shutdown and terminated after shutdown")
        public void shouldBeBothShutdownAndTerminatedAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
            assertThat(executor.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should execute tasks before shutdown but reject after")
        public void shouldExecuteTasksBeforeShutdownButRejectAfter() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());

            executor.shutdown();

            assertThatThrownBy(() -> executor.execute(() -> counter.incrementAndGet()))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle rapid execute and shutdown")
        public void shouldHandleRapidExecuteAndShutdown() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < 100; i++) {
                executor.execute(() -> counter.incrementAndGet());
            }

            executor.shutdown();

            assertThat(counter.get()).isEqualTo(100);
            assertThat(executor.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent shutdown calls")
        public void shouldHandleConcurrentShutdownCalls() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> executor.shutdown());
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(executor.isShutdown()).isTrue();
        }
    }
}
