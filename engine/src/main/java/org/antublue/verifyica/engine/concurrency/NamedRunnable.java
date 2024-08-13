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

import org.antublue.verifyica.engine.support.ArgumentSupport;

/** Class to implement NamedRunnable */
public class NamedRunnable implements Runnable {

    private final Runnable runnable;
    private final String threadName;

    /**
     * Constructor
     *
     * @param runnable runnable
     * @param threadName threadName
     */
    private NamedRunnable(Runnable runnable, String threadName) {
        this.runnable = runnable;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String originalThreadName = currentThread.getName();

        try {
            currentThread.setName(threadName);
            runnable.run();
        } finally {
            currentThread.setName(originalThreadName);
        }
    }

    /**
     * Method to wrap a Runnable
     *
     * @param runnable runnable
     * @param threadName threadName
     * @return a NamedRunnable
     */
    public static NamedRunnable newNamedRunnable(Runnable runnable, String threadName) {
        ArgumentSupport.notNull(runnable, "runnable is null");
        ArgumentSupport.notNullOrEmpty(threadName, "threadName is null", "threadName is empty");

        return new NamedRunnable(runnable, threadName.trim());
    }
}