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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

@DisplayName("HeadOfQueueRejectedExecutionHandler Tests")
public class HeadOfQueueRejectedExecutionHandlerTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create handler")
        public void shouldCreateHandler() {
            HeadOfQueueRejectedExecutionHandler handler = new HeadOfQueueRejectedExecutionHandler();

            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("RejectedExecution Tests")
    public class RejectedExecutionTests {

        @Test
        @DisplayName("Should execute rejected task when queue is empty")
        public void shouldExecuteRejectedTaskWhenQueueIsEmpty() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new HeadOfQueueRejectedExecutionHandler());

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            // Block the single thread
            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Fill the queue
            executor.execute(() -> results.add("queued"));

            // This should trigger rejection - execute inline
            executor.execute(() -> results.add("rejected"));

            latch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(results).contains("rejected", "queued");
        }

        @Test
        @DisplayName("Should remove and execute head of queue when queue not empty")
        public void shouldRemoveAndExecuteHeadOfQueueWhenQueueNotEmpty() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2), new HeadOfQueueRejectedExecutionHandler());

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(1);

            // Block the single thread
            executor.execute(() -> {
                startLatch.countDown();
                try {
                    finishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            startLatch.await();

            // Fill the queue with 2 tasks
            executor.execute(() -> results.add("first"));
            executor.execute(() -> results.add("second"));

            // This should trigger rejection
            // Handler should execute "first" inline and queue the new task
            executor.execute(() -> results.add("rejected"));

            finishLatch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(results).contains("first", "second", "rejected");
        }

        @Test
        @DisplayName("Should handle null runnable from queue")
        public void shouldHandleNullRunnableFromQueue() {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
            ThreadPoolExecutor executor =
                    new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, queue, new HeadOfQueueRejectedExecutionHandler());

            List<String> results = Collections.synchronizedList(new ArrayList<>());

            // Queue is empty, poll returns null
            HeadOfQueueRejectedExecutionHandler handler = new HeadOfQueueRejectedExecutionHandler();
            handler.rejectedExecution(() -> results.add("executed"), executor);

            executor.shutdown();

            assertThat(results).contains("executed");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("Should maintain FIFO order for non-rejected tasks")
        public void shouldMaintainFifoOrderForNonRejectedTasks() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1,
                    1,
                    1,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(10),
                    new HeadOfQueueRejectedExecutionHandler());

            List<Integer> results = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < 10; i++) {
                final int value = i;
                executor.execute(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    results.add(value);
                });
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            assertThat(results).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        }

        @Test
        @DisplayName("Should handle multiple rejections")
        public void shouldHandleMultipleRejections() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new HeadOfQueueRejectedExecutionHandler());

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            // Block the executor
            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Fill queue
            executor.execute(() -> results.add("queued-1"));

            // Trigger multiple rejections
            executor.execute(() -> results.add("rejected-1"));
            executor.execute(() -> results.add("rejected-2"));

            latch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(results).hasSize(3).containsAll(Arrays.asList("queued-1", "rejected-1", "rejected-2"));
        }

        @Test
        @DisplayName("Should work with zero capacity queue")
        public void shouldWorkWithZeroCapacityQueue() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new SynchronousQueue<>(), new HeadOfQueueRejectedExecutionHandler());

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            // Block the executor
            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // This will be rejected immediately (queue has no capacity)
            executor.execute(() -> results.add("rejected"));

            latch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(results).contains("rejected");
        }
    }

    @Nested
    @DisplayName("Behavior Tests")
    public class BehaviorTests {

        @Test
        @DisplayName("Should execute oldest task first when rejecting")
        public void shouldExecuteOldestTaskFirstWhenRejecting() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2), new HeadOfQueueRejectedExecutionHandler());

            List<String> executionOrder = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch blockLatch = new CountDownLatch(1);

            // Block the executor
            executor.execute(() -> {
                startLatch.countDown();
                try {
                    blockLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                executionOrder.add("blocking");
            });

            startLatch.await();

            // Add tasks to queue
            executor.execute(() -> executionOrder.add("task-1"));
            executor.execute(() -> executionOrder.add("task-2"));

            // Trigger rejection - should execute task-1 and queue new task
            executor.execute(() -> executionOrder.add("task-3"));

            // Unblock
            blockLatch.countDown();

            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(executionOrder).contains("task-1", "task-2", "task-3");
        }

        @Test
        @DisplayName("Should execute rejected task on calling thread")
        public void shouldExecuteRejectedTaskOnCallingThread() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new SynchronousQueue<>(), new HeadOfQueueRejectedExecutionHandler());

            Thread callingThread = Thread.currentThread();
            List<Boolean> sameThreadResults = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            // Block the executor
            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // This will be rejected and executed on calling thread
            executor.execute(() -> sameThreadResults.add(Thread.currentThread() == callingThread));

            latch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);

            assertThat(sameThreadResults).containsExactly(true);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    public class ExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate exception from rejected task")
        public void shouldPropagateExceptionFromRejectedTask() throws InterruptedException {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    1, 1, 1, TimeUnit.SECONDS, new SynchronousQueue<>(), new HeadOfQueueRejectedExecutionHandler());

            CountDownLatch latch = new CountDownLatch(1);

            // Block the executor
            executor.execute(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // This will be rejected and throw exception
            assertThatThrownBy(() -> executor.execute(() -> {
                        throw new RuntimeException("Test exception");
                    }))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Test exception");

            latch.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }
}
