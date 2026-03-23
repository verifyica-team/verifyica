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

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} implementation that creates platform threads (non-virtual threads).
 *
 * <p>This factory is used when the engine is configured to use platform threads rather than
 * virtual threads for test execution. Each call to {@link #newThread(Runnable)} creates
 * a new platform thread to execute the provided task.
 *
 * @see ThreadFactory
 * @see java.lang.Thread
 */
public class PlatformThreadFactory implements ThreadFactory {

    /**
     * Constructs a new PlatformThreadFactory instance.
     */
    public PlatformThreadFactory() {
        // INTENTIONALLY EMPTY
    }

    @Override
    public Thread newThread(Runnable task) {
        return new Thread(task);
    }
}
