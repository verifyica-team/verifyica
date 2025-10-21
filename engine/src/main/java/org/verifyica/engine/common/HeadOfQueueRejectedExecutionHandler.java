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

package org.verifyica.engine.common;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class to implement HeadOfQueueRejectedExecutionHandler
 */
public class HeadOfQueueRejectedExecutionHandler implements RejectedExecutionHandler {

    /**
     * Constructor
     */
    public HeadOfQueueRejectedExecutionHandler() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        Runnable oldestRunnable = threadPoolExecutor.getQueue().poll();
        if (oldestRunnable != null) {
            oldestRunnable.run();
            threadPoolExecutor.execute(runnable);
        } else {
            runnable.run();
        }
    }
}
