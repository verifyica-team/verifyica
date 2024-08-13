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

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Class to implement FifoSemaphore */
public class FifoSemaphore extends Semaphore {

    /** BlockingQueue */
    private final BlockingQueue<Thread> blockingQueue;

    /** ReentrantLock */
    private final ReentrantLock reentrantLock;

    /** Condition */
    private final Condition notFullCondition;

    /**
     * Constructor
     *
     * @param permits permits
     */
    public FifoSemaphore(int permits) {
        super(permits, true);

        blockingQueue = new LinkedBlockingQueue<>();
        reentrantLock = new ReentrantLock(true);
        notFullCondition = reentrantLock.newCondition();
    }

    @Override
    public void acquire() throws InterruptedException {
        Thread currentThread = Thread.currentThread();

        reentrantLock.lock();
        try {
            blockingQueue.put(currentThread);
            while (availablePermits() == 0) {
                notFullCondition.await();
            }
            blockingQueue.remove(currentThread);
            super.acquire();
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void release() {
        reentrantLock.lock();
        try {
            super.release();
            notFullCondition.signal();
        } finally {
            reentrantLock.unlock();
        }
    }
}
