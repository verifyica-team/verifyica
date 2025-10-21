/*
 * Copyright (C) Verifyica project authors and contributors
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

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement ExecutorServiceSupport */
public class ExecutorServiceSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceSupport.class);

    /**
     * Constructor
     */
    private ExecutorServiceSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to wait for all Futures to complete
     *
     * @param futures futures
     */
    public static void waitForAllFutures(Collection<Future<?>> futures) {
        Precondition.notNull(futures, "futures is null");

        LOGGER.trace("waitForAllFutures() futures [%d]", futures.size());

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Error waiting for future [%s]", e.getMessage());
            } catch (ExecutionException ee) {
                LOGGER.error("Future error [%s]", ee.getMessage());
            }
        }
    }

    /**
     * Method to shutdown an ExecutorService wait for termination
     *
     * @param executorService executorService
     */
    public static void shutdownAndAwaitTermination(ExecutorService executorService) {
        Precondition.notNull(executorService, "executorService is null");

        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOGGER.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
