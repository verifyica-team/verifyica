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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
        @DisplayName("Should create directExecutorService service in running state")
        public void shouldCreateExecutorServiceInRunningState() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            assertThat(directExecutorService.isShutdown()).isFalse();
            assertThat(directExecutorService.isTerminated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    public class ExecuteTests {

        @Test
        @DisplayName("Should execute runnable immediately")
        public void shouldExecuteRunnableImmediately() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            AtomicBoolean executed = new AtomicBoolean(false);

            directExecutorService.execute(() -> executed.set(true));

            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("Should execute runnable on calling thread")
        public void shouldExecuteRunnableOnCallingThread() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            Thread thread = Thread.currentThread();
            AtomicBoolean sameThread = new AtomicBoolean(false);

            directExecutorService.execute(() -> sameThread.set(Thread.currentThread() == thread));

            assertThat(sameThread).isTrue();
        }

        @Test
        @DisplayName("Should execute multiple runnables sequentially")
        public void shouldExecuteMultipleRunnablesSequentially() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            directExecutorService.execute(counter::incrementAndGet);
            directExecutorService.execute(counter::incrementAndGet);
            directExecutorService.execute(counter::incrementAndGet);

            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should execute runnables in order")
        public void shouldExecuteRunnablesInOrder() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            StringBuilder result = new StringBuilder();

            directExecutorService.execute(() -> result.append("A"));
            directExecutorService.execute(() -> result.append("B"));
            directExecutorService.execute(() -> result.append("C"));

            assertThat(result.toString()).isEqualTo("ABC");
        }

        @Test
        @DisplayName("Should throw RejectedExecutionException after shutdown")
        public void shouldThrowRejectedExecutionExceptionAfterShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();

            assertThatThrownBy(() -> directExecutorService.execute(() -> {}))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");
        }

        @Test
        @DisplayName("Should propagate exceptions from runnable")
        public void shouldPropagateExceptionsFromRunnable() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            assertThatThrownBy(() -> directExecutorService.execute(() -> {
                        throw new RuntimeException("Test exception");
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when runnable is null")
        public void shouldThrowIllegalArgumentExceptionWhenRunnableIsNull() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            assertThatThrownBy(() -> directExecutorService.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("runnable is null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null runnable after shutdown")
        public void shouldThrowIllegalArgumentExceptionForNullRunnableAfterShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            directExecutorService.shutdown();

            assertThatThrownBy(() -> directExecutorService.execute(null))
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
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();

            assertThat(directExecutorService.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        public void shouldChangeStateToTerminated() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();

            assertThat(directExecutorService.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();
            directExecutorService.shutdown();

            assertThat(directExecutorService.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("ShutdownNow Tests")
    public class ShutdownNowTests {

        @Test
        @DisplayName("Should change state to shutdown")
        public void shouldChangeStateToShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdownNow();

            assertThat(directExecutorService.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should change state to terminated")
        public void shouldChangeStateToTerminated() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdownNow();

            assertThat(directExecutorService.isTerminated()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list")
        public void shouldReturnEmptyList() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            List<Runnable> remaining = directExecutorService.shutdownNow();

            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("Should be idempotent")
        public void shouldBeIdempotent() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            List<Runnable> first = directExecutorService.shutdownNow();
            List<Runnable> second = directExecutorService.shutdownNow();

            assertThat(first).isEmpty();
            assertThat(second).isEmpty();
            assertThat(directExecutorService.isShutdown()).isTrue();
        }

        @Test
        @DisplayName("Should work after shutdown")
        public void shouldWorkAfterShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();
            List<Runnable> remaining = directExecutorService.shutdownNow();

            assertThat(remaining).isEmpty();
            assertThat(directExecutorService.isShutdown()).isTrue();
        }
    }

    @Nested
    @DisplayName("AwaitTermination Tests")
    public class AwaitTerminationTests {

        @Test
        @DisplayName("Should return true immediately after shutdown")
        public void shouldReturnTrueImmediatelyAfterShutdown() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            directExecutorService.shutdown();

            boolean terminated = directExecutorService.awaitTermination(1, TimeUnit.SECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should return false when not shutdown")
        public void shouldReturnFalseWhenNotShutdown() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            boolean terminated = directExecutorService.awaitTermination(1, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should return immediately when already shutdown")
        public void shouldReturnImmediatelyWhenAlreadyShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            directExecutorService.shutdown();

            assertTimeoutPreemptively(Duration.ofMillis(250), () -> {
                boolean terminated = directExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                assertThat(terminated).isTrue();
            });
        }

        @Test
        @DisplayName("Should return false with zero timeout when not shutdown")
        public void shouldReturnFalseWithZeroTimeoutWhenNotShutdown() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            boolean terminated = directExecutorService.awaitTermination(0, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should return true with zero timeout when already shutdown")
        public void shouldReturnTrueWithZeroTimeoutWhenAlreadyShutdown() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            directExecutorService.shutdown();

            boolean terminated = directExecutorService.awaitTermination(0, TimeUnit.MILLISECONDS);

            assertThat(terminated).isTrue();
        }

        @Test
        @DisplayName("Should return false with negative timeout when not shutdown")
        public void shouldReturnFalseWithNegativeTimeoutWhenNotShutdown() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            boolean terminated = directExecutorService.awaitTermination(-1, TimeUnit.MILLISECONDS);

            assertThat(terminated).isFalse();
        }

        @Test
        @DisplayName("Should throw InterruptedException when thread is interrupted")
        public void shouldThrowInterruptedExceptionWhenThreadIsInterrupted() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            CountDownLatch started = new CountDownLatch(1);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            Thread thread = new Thread(() -> {
                try {
                    started.countDown();
                    directExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                }
            });
            thread.start();

            assertThat(started.await(1, TimeUnit.SECONDS))
                    .as("worker thread did not start in time")
                    .isTrue();

            thread.interrupt();
            thread.join(1_000);

            assertThat(thread.isAlive()).isFalse();
            assertThat(interrupted.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("State Tests")
    public class StateTests {

        @Test
        @DisplayName("Should not be shutdown initially")
        public void shouldNotBeShutdownInitially() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            assertThat(directExecutorService.isShutdown()).isFalse();
        }

        @Test
        @DisplayName("Should not be terminated initially")
        public void shouldNotBeTerminatedInitially() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            assertThat(directExecutorService.isTerminated()).isFalse();
        }

        @Test
        @DisplayName("Should be both shutdown and terminated after shutdown")
        public void shouldBeBothShutdownAndTerminatedAfterShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();

            directExecutorService.shutdown();

            assertThat(directExecutorService.isShutdown()).isTrue();
            assertThat(directExecutorService.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should execute tasks before shutdown but reject after")
        public void shouldExecuteTasksBeforeShutdownButRejectAfter() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            directExecutorService.execute(counter::incrementAndGet);
            directExecutorService.execute(counter::incrementAndGet);

            directExecutorService.shutdown();

            assertThatThrownBy(() -> directExecutorService.execute(counter::incrementAndGet))
                    .isInstanceOf(RejectedExecutionException.class)
                    .hasMessage("Executor has been shut down");

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle rapid execute and shutdown")
        public void shouldHandleRapidExecuteAndShutdown() {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < 100; i++) {
                directExecutorService.execute(counter::incrementAndGet);
            }

            directExecutorService.shutdown();

            assertThat(counter.get()).isEqualTo(100);
            assertThat(directExecutorService.isTerminated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent shutdown calls")
        public void shouldHandleConcurrentShutdownCalls() throws InterruptedException {
            DirectExecutorService directExecutorService = new DirectExecutorService();
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(directExecutorService::shutdown);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(directExecutorService.isShutdown()).isTrue();
        }
    }
}
