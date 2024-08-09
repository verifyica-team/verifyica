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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Class to implement a Key */
public class Key {

    private final List<Segment> segments;

    /** Constructor */
    private Key(List<Segment> segments) {
        this.segments = segments;
    }

    /**
     * Method to get a copy of the list of Segments
     *
     * @return a List of Segments
     */
    public List<Segment> segments() {
        return new ArrayList<>(segments);
    }

    /**
     * Method to append an Object to the Key, returning a new Key
     *
     * @param object object
     * @return a new Key with the appended Object
     */
    public Key append(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object is null");
        }

        Key key = new Key(Collections.synchronizedList(segments));
        key.segments.add(new Segment(object));
        return key;
    }

    /**
     * Method to remove the last Object from the key, returning a new Key
     *
     * @return a new Key with the last appended Object removed
     */
    public Key remove() {
        List<Segment> segments = Collections.synchronizedList(this.segments);

        if (segments.size() <= 1) {
            throw new IllegalStateException("can't remove root segment");
        }

        segments.remove(segments.size() - 1);
        return new Key(segments);
    }

    /**
     * Method to create a Key from an array of Objects
     *
     * @param objects objects
     * @return a Key
     */
    public static Key of(Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects is null");
        }

        if (objects.length == 0) {
            throw new IllegalArgumentException("objects is empty");
        }

        List<Segment> segments = Collections.synchronizedList(new ArrayList<>(objects.length));
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object == null) {
                throw new IllegalArgumentException(format("objects[%d] is null", i));
            }
            segments.add(new Segment(object));
        }

        return new Key(segments);
    }

    /**
     * Method to create a Key from a List of Objects
     *
     * @param objects objects
     * @return a Key
     */
    public static Object of(List<Object> objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects is null");
        }

        if (objects.isEmpty()) {
            throw new IllegalArgumentException("objects is empty");
        }

        List<Segment> segments = Collections.synchronizedList(new ArrayList<>(objects.size()));
        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);
            if (object == null) {
                throw new IllegalArgumentException(format("object[%d] is null", i));
            }
            segments.add(new Segment(object));
        }

        return new Key(segments);
    }

    @Override
    public String toString() {
        return "Key{" + "segments=" + segments + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return Objects.equals(segments, key.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
    }

    /** Class to implement Segment */
    public static class Segment {

        private final Object value;

        /**
         * Contructor
         *
         * @param value value
         */
        private Segment(Object value) {
            this.value = value;
        }

        /**
         * Method to get the value
         *
         * @return the value
         */
        public Object value() {
            return value;
        }
    }
}
