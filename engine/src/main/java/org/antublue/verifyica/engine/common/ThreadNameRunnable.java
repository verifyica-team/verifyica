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

package org.antublue.verifyica.engine.common;

/** Class to implement NamedRunnable */
public class ThreadNameRunnable implements Runnable {

    private final String threadName;
    private final Runnable runnable;

    /**
     * Constructor
     *
     * @param threadName threadName
     * @param runnable runnable
     */
    public ThreadNameRunnable(String threadName, Runnable runnable) {
        Precondition.notNull(threadName, "threadName is null");
        Precondition.notNull(runnable, "runnable is null");

        this.threadName = threadName;
        this.runnable = runnable;
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
}
