/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

import io.github.thunkware.vt.bridge.ThreadTool;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} implementation that creates virtual threads when available,
 * falling back to platform threads on older JVM versions.
 *
 * <p>This factory is the preferred choice for test execution when the JVM supports
 * virtual threads (Java 21+). If virtual threads are not available, it gracefully
 * falls back to creating platform threads.
 *
 * @see ThreadFactory
 * @see java.lang.Thread
 */
public class VirtualThreadFactory implements ThreadFactory {

    /**
     * Constructs a new VirtualThreadFactory instance.
     */
    public VirtualThreadFactory() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public Thread newThread(Runnable task) {
        return ThreadTool.hasVirtualThreads() ? ThreadTool.unstartedVirtualThread(task) : new Thread(task);
    }
}
