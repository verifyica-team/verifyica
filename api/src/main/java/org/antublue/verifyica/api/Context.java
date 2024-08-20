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

package org.antublue.verifyica.api;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/** Interface to implement Context */
public interface Context {

    /**
     * Returns the Store
     *
     * @return the Store
     */
    Store getStore();

    /**
     * Returns the Context Lock.
     *
     * <p>Equivalent to getStore().getLock()
     *
     * <p>Equivalent to getStore().getReadWriteLock().writeLock()
     *
     * @return the Context Lock
     */
    default Lock getLock() {
        return getStore().getLock();
    }

    /**
     * Returns the Context ReadWriteLock.
     *
     * <p>Equivalent to getStore().getReadWriteLock()
     *
     * @return the Context ReadWriteLock
     */
    default ReadWriteLock getReadWriteLock() {
        return getStore().getReadWriteLock();
    }
}
