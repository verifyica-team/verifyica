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
 * A simple thread-safe counter class that can be used to count occurrences of events.
 */
public class Counter {

    private final String name;
    private final String description;
    private final AtomicLong count;

    /**
     * Constructor
     *
     * @param name the name of the counter
     * @param description the description of the counter
     */
    public Counter(String name, String description) {
        Precondition.notNullOrBlank(name, "name is null", "name is blank");
        Precondition.notNullOrBlank(description, "description is null", "description is blank");

        this.name = name;
        this.description = description;
        this.count = new AtomicLong(0);
    }

    /**
     * Method to get the name of the counter
     *
     * @return the name of the counter
     */
    public String name() {
        return name;
    }

    /**
     * Method to get the description of the counter
     *
     * @return the description of the counter
     */
    public String description() {
        return description;
    }

    /**
     * Method to get the current count
     *
     * @return the current count
     */
    public long count() {
        return count.get();
    }

    /**
     * Method to increment the counter by 1
     */
    public void increment() {
        count.incrementAndGet();
    }

    /**
     * Method to increment the counter by a specified value. The value must be greater than or equal to 0.
     *
     * @param value the value to increment by
     */
    public void increment(long value) {
        Precondition.isTrue(value >= 0, "value must be >= 0");

        count.addAndGet(value);
    }

    @Override
    public String toString() {
        return "Counter {" + "name='"
                + name + '\'' + ", description='"
                + description + '\'' + ", count="
                + count.get() + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Counter)) return false;
        Counter counter = (Counter) o;
        return Objects.equals(name, counter.name) && Objects.equals(description, counter.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    /**
     * Method to register the counter in a map.
     *
     * @param map the map to register the counter in
     */
    public void register(Map<String, Counter> map) {
        Precondition.notNull(map, "map is null");

        map.put(name, this);
    }
}
