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

import static org.antublue.verifyica.api.Fail.failIfTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class SemaphoreRunnableTest {

    @Test
    public void test() throws InterruptedException {
        AtomicReference<String> failed = new AtomicReference<>();
        AtomicInteger counter = new AtomicInteger();

        Runnable runnable =
                () -> {
                    try {
                        int value = counter.incrementAndGet();
                        if (value != 1) {
                            failed.set("expected [1] but was [" + value + "]");
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // INTENTIONALLY BLANK
                    } finally {
                        int value = counter.decrementAndGet();
                        if (value != 0) {
                            failed.set("expected [0] but was [" + value + "]");
                        }
                    }
                };

        Semaphore semaphore = new Semaphore(1, true);
        int threadCount = 5;
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new SemaphoreRunnable(semaphore, runnable));
            thread.setDaemon(true);
            threads.add(thread);
        }

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            thread.join();
        }

        failIfTrue(failed.get() != null, failed.get());
    }
}
