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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A {@link RejectedExecutionHandler} implementation that executes the oldest queued task
 * when a new task is rejected, then submits the new task for execution.
 *
 * <p>This handler ensures that no tasks are lost when the thread pool is saturated.
 * Instead of simply rejecting the new task, it removes the oldest waiting task from
 * the queue, executes it immediately, and then attempts to queue the new task.
 * This approach provides better throughput under high load conditions.
 *
 * @see RejectedExecutionHandler
 * @see ThreadPoolExecutor
 */
public class HeadOfQueueRejectedExecutionHandler implements RejectedExecutionHandler {

    /**
     * Constructs a new HeadOfQueueRejectedExecutionHandler instance.
     */
    public HeadOfQueueRejectedExecutionHandler() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        Precondition.notNull(runnable, "runnable is null");
        Precondition.notNull(threadPoolExecutor, "threadPoolExecutor is null");

        Runnable oldestRunnable = threadPoolExecutor.getQueue().poll();
        if (oldestRunnable != null) {
            oldestRunnable.run();
            threadPoolExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }
}
