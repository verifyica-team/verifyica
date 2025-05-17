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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/** Class to implement DirectExecutorService */
public class DirectExecutorService extends AbstractExecutorService {

    private static final List<Runnable> EMPTY_LIST = new ArrayList<>();

    private volatile boolean isShutdown = false;

    /** Constructor */
    public DirectExecutorService() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        return EMPTY_LIST;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) {
        return isShutdown;
    }

    @Override
    public void execute(Runnable runnable) {
        if (!isShutdown) {
            runnable.run();
        }
    }
}
