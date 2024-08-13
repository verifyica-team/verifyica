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

public class FifoSemaphore extends Semaphore {

    private final BlockingQueue<Thread> blockingQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();

    public FifoSemaphore(int permits) {
        super(permits, true);
    }

    @Override
    public void acquire() throws InterruptedException {
        final Thread currentThread = Thread.currentThread();
        lock.lock();
        try {
            blockingQueue.put(currentThread);
            while (availablePermits() == 0) {
                notFull.await();
            }
            blockingQueue.remove(currentThread);
            super.acquire();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void release() {
        lock.lock();
        try {
            super.release();
            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
}
