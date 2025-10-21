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

import java.util.concurrent.Semaphore;

/**
 * Class to implement SemaphoreControlledRunnable
 */
public class SemaphoreRunnable implements Runnable {

    private final Semaphore semaphore;
    private final Runnable runnable;

    /**
     * Constructor
     *
     * @param semaphore semaphore
     * @param runnable runnable
     */
    public SemaphoreRunnable(Semaphore semaphore, Runnable runnable) {
        Precondition.notNull(semaphore, "semaphore is null");
        Precondition.notNull(runnable, "runnable is null");

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
}
