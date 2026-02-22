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

package org.verifyica.engine.common;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe counter implementation for tracking occurrences of events.
 *
 * <p>This class provides a simple, thread-safe way to count occurrences of events.
 * It uses an {@link AtomicLong} internally to ensure accurate counting across
 * multiple threads without explicit synchronization.
 *
 * <p>Each counter has a name and description for identification purposes, making
 * it useful for tracking and reporting statistics in multi-threaded environments.
 *
 * @see AtomicLong
 */
public class Counter {

    private final String name;
    private final String description;
    private final AtomicLong count;

    /**
     * Creates a new Counter with the specified name and description.
     *
     * @param name the name of the counter (must not be null or blank)
     * @param description the description of the counter (must not be null or blank)
     * @throws IllegalArgumentException if name or description is null or blank
     */
    public Counter(final String name, final String description) {
        Precondition.notNullOrBlank(name, "name is null", "name is blank");
        Precondition.notNullOrBlank(description, "description is null", "description is blank");

        this.name = name;
        this.description = description;
        this.count = new AtomicLong();
    }

    /**
     * Returns the name of the counter.
     *
     * @return the name of the counter
     */
    public String name() {
        return name;
    }

    /**
     * Returns the description of the counter.
     *
     * @return the description of the counter
     */
    public String description() {
        return description;
    }

    /**
     * Returns the current count value.
     *
     * @return the current count value
     */
    public long count() {
        return count.get();
    }

    /**
     * Atomically increments the counter by one.
     */
    public void increment() {
        count.incrementAndGet();
    }

    /**
     * Atomically increments the counter by the specified value.
     *
     * @param value the value to increment by (must be >= 0)
     * @throws IllegalArgumentException if value is negative
     */
    public void increment(final long value) {
        Precondition.isTrue(value >= 0, "value must be >= 0");

        count.addAndGet(value);
    }

    @Override
    public String toString() {
        // Estimate capacity: "Counter {" (9) + name + "'', description=''" (19) + count + " }" (3)
        // Plus estimate for count as string (20 chars for long)
        final int estimatedLength = 9 + name.length() + description.length() + 19 + 20 + 3;
        return new StringBuilder(estimatedLength)
                .append("Counter {")
                .append("name='")
                .append(name)
                .append('\'')
                .append(", description='")
                .append(description)
                .append('\'')
                .append(", count=")
                .append(count.get())
                .append(" }")
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Counter)) return false;
        final Counter counter = (Counter) o;
        return Objects.equals(name, counter.name) && Objects.equals(description, counter.description);
    }

    @Override
    public int hashCode() {
        // Manual hash computation to avoid Objects.hash() array allocation
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    /**
     * Registers this counter in the specified map using its name as the key.
     *
     * @param map the map to register the counter in (must not be null)
     * @throws IllegalArgumentException if map is null
     */
    public void register(final Map<String, Counter> map) {
        Precondition.notNull(map, "map is null");

        map.put(name, this);
    }
}
