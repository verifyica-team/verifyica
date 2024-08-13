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

package org.antublue.verifyica.engine.concurrency;

import java.util.concurrent.Semaphore;
import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement SemaphoreControlledRunnable */
public class SemaphoreRunnable implements Runnable {

    private final Semaphore semaphore;
    private final Runnable runnable;

    /**
     * Constructor
     *
     * @param semaphore semaphore
     * @param runnable runnable
     */
    private SemaphoreRunnable(Semaphore semaphore, Runnable runnable) {
        this.semaphore = semaphore;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            try {
                runnable.run();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Method to wrap a Runnable
     *
     * @param semaphore semaphore
     * @param runnable runnable
     * @return a SemaphoreControlledRunnable
     */
    public static SemaphoreRunnable newSemaphoreRunnable(Semaphore semaphore, Runnable runnable) {
        ArgumentSupport.notNull(semaphore, "semaphore is null");
        ArgumentSupport.notNull(runnable, "runnable is null");

        return new SemaphoreRunnable(semaphore, runnable);
    }
}
