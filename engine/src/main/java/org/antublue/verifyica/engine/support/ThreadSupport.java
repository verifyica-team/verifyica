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

package org.antublue.verifyica.engine.support;

import io.github.thunkware.vt.bridge.ThreadTool;
import org.antublue.verifyica.engine.common.Precondition;

/** Class to implement ThreadSupport */
public class ThreadSupport {

    /** Constructor */
    private ThreadSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if virtual threads are supported
     *
     * @return true if virtual threads are supported, else false
     */
    public static boolean hasVirtualThreads() {
        return ThreadTool.hasVirtualThreads();
    }

    /**
     * Method to create a new Thread
     *
     * @param task task
     * @return a new Thread
     */
    public static Thread newThread(Runnable task) {
        Precondition.notNull(task, "task is null");

        return ThreadTool.hasVirtualThreads()
                ? ThreadTool.unstartedVirtualThread(task)
                : new Thread(task);
    }
}
