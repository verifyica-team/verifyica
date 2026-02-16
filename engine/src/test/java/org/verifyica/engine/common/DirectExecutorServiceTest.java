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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

@DisplayName("DirectExecutorService Tests")
class DirectExecutorServiceTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create executor service in running state")
        void shouldCreateExecutorServiceInRunningState() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isShutdown()).isFalse();
            assertThat(executor.isTerminated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    class ExecuteTests {

        @Test
        @DisplayName("Should execute runnable immediately")
        void shouldExecuteRunnableImmediately() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicBoolean executed = new AtomicBoolean(false);

            executor.execute(() -> executed.set(true));

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("Should execute runnable on calling thread")
        void shouldExecuteRunnableOnCallingThread() {
            DirectExecutorService executor = new DirectExecutorService();
            Thread callingThread = Thread.currentThread();
            AtomicBoolean sameThread = new AtomicBoolean(false);

            executor.execute(() -> sameThread.set(Thread.currentThread() == callingThread));

            assertThat(sameThread).isTrue();
        }

        @Test
        @DisplayName("Should execute multiple runnables sequentially")
        void shouldExecuteMultipleRunnablesSequentially() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should execute runnables in order")
        void shouldExecuteRunnablesInOrder() {
            DirectExecutorService executor = new DirectExecutorService();
            StringBuilder result = new StringBuilder();

            executor.execute(() -> result.append("A"));
            executor.execute(() -> result.append("B"));
            executor.execute(() -> result.append("C"));

            assertThat(result.toString()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Should not execute after shutdown")
        void shouldNotExecuteAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicBoolean executed = new AtomicBoolean(false);

            executor.shutdown();
            executor.execute(() -> executed.set(true));

            assertThat(executed).isFalse();
        }

        @Test
        @DisplayName("Should propagate exceptions from runnable")
        void shouldPropagateExceptionsFromRunnable() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThatThrownBy(() -> executor.execute(() -> {
                        throw new RuntimeException("Test exception");
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");
        }
    }

    @Nested
    @DisplayName("Shutdown Tests")
    class ShutdownTests {

        @Test
        @DisplayName("Should change state to shutdown")
        void shouldChangeStateToShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        void shouldChangeStateToTerminated() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should be idempotent")
        void shouldBeIdempotent() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();
            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("ShutdownNow Tests")
    class ShutdownNowTests {

        @Test
        @DisplayName("Should change state to shutdown")
        void shouldChangeStateToShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdownNow();

            assertThat(executor.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        void shouldChangeStateToTerminated() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdownNow();

            assertThat(executor.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list")
        void shouldReturnEmptyList() {
            DirectExecutorService executor = new DirectExecutorService();

            List<Runnable> remaining = executor.shutdownNow();

            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("Should be idempotent")
        void shouldBeIdempotent() {
            DirectExecutorService executor = new DirectExecutorService();

            List<Runnable> first = executor.shutdownNow();
            List<Runnable> second = executor.shutdownNow();

            assertThat(first).isEmpty();
            assertThat(second).isEmpty();
            assertThat(executor.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("AwaitTermination Tests")
    class AwaitTerminationTests {

        @Test
        @DisplayName("Should return true immediately after shutdown")
        void shouldReturnTrueImmediatelyAfterShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            boolean terminated = executor.awaitTermination(1, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should return false when not shutdown")
        void shouldReturnFalseWhenNotShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();

            boolean terminated = executor.awaitTermination(1, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should return immediately when already shutdown")
        void shouldReturnImmediatelyWhenAlreadyShutdown() throws InterruptedException {
            DirectExecutorService executor = new DirectExecutorService();
            executor.shutdown();

            long startTime = System.currentTimeMillis();
            boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;

            assertThat(terminated).isTrue();
            assertThat(duration).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("State Tests")
    class StateTests {

        @Test
        @DisplayName("Should not be shutdown initially")
        void shouldNotBeShutdownInitially() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("Should not be terminated initially")
        void shouldNotBeTerminatedInitially() {
            DirectExecutorService executor = new DirectExecutorService();

            assertThat(executor.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("Should be both shutdown and terminated after shutdown")
        void shouldBeBothShutdownAndTerminatedAfterShutdown() {
            DirectExecutorService executor = new DirectExecutorService();

            executor.shutdown();

            assertThat(executor.isShutdown()).isTrue();
            assertThat(executor.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should execute tasks before shutdown but not after")
        void shouldExecuteTasksBeforeShutdownButNotAfter() {
            DirectExecutorService executor = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            executor.execute(() -> counter.incrementAndGet());
            executor.execute(() -> counter.incrementAndGet());

            executor.shutdown();

            executor.execute(() -> counter.incrementAndGet());

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle rapid execute and shutdown")
        void shouldHandleRapidExecuteAndShutdown() {
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
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent shutdown calls")
        void shouldHandleConcurrentShutdownCalls() throws InterruptedException {
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
