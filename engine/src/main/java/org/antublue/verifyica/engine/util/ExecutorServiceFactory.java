/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.util;

import io.github.thunkware.vt.bridge.ExecutorTool;
import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement ExecutorServiceFactory */
public class ExecutorServiceFactory {

    /** Constructor */
    private ExecutorServiceFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create a new ExecutorService. If virtual threads are supported, threadCount is
     * ignored
     *
     * @param threadCount threadCount
     * @return an ExecutorService
     */
    public ExecutorService newExecutorService(int threadCount) {
        ArgumentSupport.isTrue(threadCount > 0, "thread count is less than 1");

        ExecutorService executorService;

        if (ThreadTool.hasVirtualThreads()) {
            executorService = ExecutorTool.newVirtualThreadPerTaskExecutor();
        } else {
            executorService =
                    new ThreadPoolExecutor(
                            threadCount,
                            threadCount,
                            60L,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(threadCount * 10),
                            new BlockingRejectedExecutionHandler());
        }

        return executorService;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static ExecutorServiceFactory getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to return if using virtual threads
     *
     * @return true if using virtual threads, else false
     */
    public static boolean usingVirtualThreads() {
        return ThreadTool.hasVirtualThreads();
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final ExecutorServiceFactory SINGLETON = new ExecutorServiceFactory();
    }
}
