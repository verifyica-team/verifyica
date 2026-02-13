/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;

public class PooledResourceTest {

    private static final String KEY = "testContext";

    // Hard cap: at most 3 resources concurrently in-use, even if 10 arguments run in parallel.
    private static final Semaphore RESOURCE_SEMAPHORE = new Semaphore(3, true);

    @Verifyica.ArgumentSupplier(parallelism = 10)
    public static Object arguments() {
        return IntStream.range(0, 10).mapToObj(i -> "arg-" + i).collect(Collectors.toList());
    }

    @Verifyica.BeforeAll
    public void beforeAll(ArgumentContext argumentContext) throws InterruptedException {
        // 1) Cap concurrency: blocks when 3 are already checked out
        RESOURCE_SEMAPHORE.acquire();

        // 2) Acquire actual resource from pool
        Resource resource = ResourcePool.getInstance().acquire();

        // 3) Store per-argument invocation context
        argumentContext.getMap().put(KEY, new TestContext(resource));
    }

    @Verifyica.Test
    public void test(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().get(KEY);
        if (context == null || context.getResource() == null) {
            throw new IllegalStateException("Missing TestContext/Resource in ArgumentContext map under key: " + KEY);
        }

        context.getResource().use();
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) {
        TestContext context = (TestContext) argumentContext.getMap().remove(KEY);

        try {
            if (context != null) {
                Resource resource = context.getResource();
                if (resource != null) {
                    ResourcePool.getInstance().release(resource);
                }
            }
        } finally {
            RESOURCE_SEMAPHORE.release();
        }
    }

    /**
     * Per-argument invocation state holder.
     */
    public static final class TestContext {

        private final Resource resource;

        public TestContext(Resource resource) {
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }
    }

    /**
     * Minimal "resource" abstraction used by this test.
     * Replace with your real resource type if you have one.
     */
    public interface Resource {
        void use();

        int id();
    }

    /**
     * A tiny, threadsafe resource pool.
     *
     * - Pre-allocates N resources.
     * - acquire() blocks if none are available.
     * - release() returns a resource to the pool.
     */
    public static final class ResourcePool {

        private static final ResourcePool INSTANCE = new ResourcePool(3);

        private final Object lock = new Object();
        private final Deque<Resource> available;
        private final int capacity;

        private ResourcePool(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be > 0");
            }
            this.capacity = capacity;
            this.available = new ArrayDeque<>(capacity);

            for (int i = 0; i < capacity; i++) {
                this.available.addLast(new PooledResource());
            }
        }

        public static ResourcePool getInstance() {
            return INSTANCE;
        }

        public Resource acquire() throws InterruptedException {
            synchronized (lock) {
                while (available.isEmpty()) {
                    lock.wait();
                }
                return available.removeFirst();
            }
        }

        public void release(Resource resource) {
            if (resource == null) {
                return;
            }
            synchronized (lock) {
                if (available.size() >= capacity) {
                    throw new IllegalStateException("Pool overflow (double release?) size=" + available.size());
                }
                available.addLast(resource);
                lock.notify();
            }
        }

        /**
         * Simple demo Resource implementation.
         */
        private static final class PooledResource implements Resource {

            private static final AtomicInteger IDS = new AtomicInteger();
            private final int id = IDS.incrementAndGet();

            @Override
            public void use() {
                System.out.println("Using resource " + id);
            }

            @Override
            public int id() {
                return id;
            }

            @Override
            public String toString() {
                return "Resource#" + id;
            }
        }
    }
}
