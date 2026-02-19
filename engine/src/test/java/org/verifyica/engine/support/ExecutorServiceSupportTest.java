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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExecutorServiceSupport Tests")
public class ExecutorServiceSupportTest {

    @Test
    @DisplayName("Should wait for all futures to complete")
    public void shouldWaitForAllFuturesToComplete() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(2);

        futures.add(executorService.submit(() -> {
            latch.countDown();
            return null;
        }));
        futures.add(executorService.submit(() -> {
            latch.countDown();
            return null;
        }));

        assertThatCode(() -> ExecutorServiceSupport.waitForAllFutures(futures)).doesNotThrowAnyException();

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();

        executorService.shutdown();
    }

    @Test
    @DisplayName("Should throw exception for null futures collection")
    public void shouldThrowExceptionForNullFuturesCollection() {
        assertThatThrownBy(() -> ExecutorServiceSupport.waitForAllFutures(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futures is null");
    }

    @Test
    @DisplayName("Should handle empty futures collection")
    public void shouldHandleEmptyFuturesCollection() {
        assertThatCode(() -> ExecutorServiceSupport.waitForAllFutures(Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle futures with exceptions")
    public void shouldHandleFuturesWithExceptions() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<?> future = executorService.submit((Callable<Void>) () -> {
            throw new RuntimeException("Test exception");
        });

        List<Future<?>> futures = Collections.singletonList(future);

        // Should not throw, just log the error
        assertThatCode(() -> ExecutorServiceSupport.waitForAllFutures(futures)).doesNotThrowAnyException();

        executorService.shutdown();
    }

    @Test
    @DisplayName("Should shutdown and await termination")
    public void shouldShutdownAndAwaitTermination() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        assertThatCode(() -> ExecutorServiceSupport.shutdownAndAwaitTermination(executorService))
                .doesNotThrowAnyException();

        assertThat(executorService.isShutdown()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for null executor service")
    public void shouldThrowExceptionForNullExecutorService() {
        assertThatThrownBy(() -> ExecutorServiceSupport.shutdownAndAwaitTermination(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("executorService is null");
    }

    @Test
    @DisplayName("Should handle already shutdown executor service")
    public void shouldHandleAlreadyShutdownExecutorService() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.shutdown();

        assertThatCode(() -> ExecutorServiceSupport.shutdownAndAwaitTermination(executorService))
                .doesNotThrowAnyException();
    }
}
