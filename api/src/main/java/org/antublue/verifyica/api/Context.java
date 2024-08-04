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
import org.antublue.verifyica.api.concurrency.lock.LockProvider;
import org.antublue.verifyica.api.concurrency.lock.ReadWriteLockProvider;

/** Interface to implement Context */
public interface Context extends ReadWriteLockProvider, LockProvider {

    /**
     * Returns the Store
     *
     * @return the Store
     */
    Store getStore();

    @Override
    default Lock getLock() {
        return getReadWriteLock().writeLock();
    }

    @Override
    default ReadWriteLock getReadWriteLock() {
        return getStore().getReadWriteLock();
    }
}
