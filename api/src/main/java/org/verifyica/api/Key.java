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

package org.verifyica.api;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Class to implement a Key */
public class Key {

    private final List<String> segments;

    /**
     * Constructor
     *
     * @param segments segments
     */
    private Key(List<String> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    /**
     * Method to get an unmodifiable List of segments that make up the Key
     *
     * @return a List of segments
     */
    public List<String> segments() {
        return segments;
    }

    /**
     * Method to append a String to the Key, returning a new Key.
     *
     * <p>The String must not be null
     *
     * @param segment segment
     * @return a new Key with the appended segment
     */
    public Key append(String segment) {
        notBlank(segment, "segment is null", "segment is blank");

        List<String> segments = new ArrayList<>(this.segments);
        segments.add(segment);
        return new Key(segments);
    }

    /**
     * Method to an array of Strings to the Key, returning a new Key.
     *
     * @param segments segments
     * @return a new Key with the appended segment
     */
    public Key append(String... segments) {
        notNull(segments, "segments is null");
        isTrue(segments.length > 0, "segments is empty");

        List<String> tempSegments = new ArrayList<>(segments.length);

        for (int i = 0; i < segments.length; i++) {
            notBlank(segments[i], format("segments[%d] is null", i), format("segments[%d] is blank", i));
            tempSegments.add(segments[i]);
        }

        List<String> rootSegments = new ArrayList<>(this.segments.size() + tempSegments.size());
        rootSegments.addAll(this.segments);
        rootSegments.addAll(tempSegments);

        return new Key(rootSegments);
    }

    /**
     * Method to an array of Strings to the Key, returning a new Key.
     *
     * @param segments segments
     * @return a new Key with the appended segment
     */
    public Key append(List<String> segments) {
        notNull(segments, "segments is null");
        isTrue(!segments.isEmpty(), "segments is empty");

        List<String> rootSegments = new ArrayList<>(this.segments.size() + segments.size());
        rootSegments.addAll(this.segments);

        int i = 0;
        for (String segment : segments) {
            notBlank(segment, format("segments[%d] is null", i), format("segments[%d] is blank", i));
            rootSegments.add(segment);
            i++;
        }

        return new Key(rootSegments);
    }

    /**
     * Method to append a Key to the Key, returning a new Key.
     *
     * <p>The String must not be null
     *
     * @param key key
     * @return a new Key with the appended segment
     */
    public Key append(Key key) {
        notNull(key, "key is null");

        return append(key.segments());
    }

    /**
     * Method to remove the last segment from the Key, returning a new Key
     *
     * @return a new Key with the last appended segment removed
     * @throws IllegalStateException if there is only one segement in the key
     */
    public Key remove() {
        if (segments.size() <= 1) {
            throw new IllegalStateException("can't remove root segment");
        }

        List<String> segments = new ArrayList<>(this.segments);
        segments.remove(segments.size() - 1);
        return new Key(segments);
    }

    /**
     * Method to create a Key from an array of Strings
     *
     * @param segments segments
     * @return a Key
     */
    public static Key of(String... segments) {
        notNull(segments, "segments is null");
        isTrue(segments.length > 0, "segments is empty");

        List<String> tempSegments = new ArrayList<>(segments.length);
        for (int i = 0; i < segments.length; i++) {
            notBlank(segments[i], format("segments[%d] is null", i), format("segments[%d] is blank", i));
            tempSegments.add(segments[i]);
        }

        return new Key(tempSegments);
    }

    /**
     * Method to create a Key from a List of Strings
     *
     * @param segments segments
     * @return a Key
     */
    public static Key of(List<String> segments) {
        notNull(segments, "segments is null");
        isFalse(segments.isEmpty(), "segments is empty");

        List<String> tempSegments = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            String string = segments.get(i);
            notBlank(string, format("segments[%d] is null", i), format("segments[%d] is blank", i));
            tempSegments.add(string);
        }

        return new Key(tempSegments);
    }

    /**
     * Method to flatten a key, starting with a '/' character and adding a '/' character between
     * segments
     *
     * @return a flattened key
     */
    public String flatten() {
        return "/" + String.join("/", segments);
    }

    @Override
    public String toString() {
        return flatten();
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

    /**
     * Method to validate an Object is not null, throwing an IllegalArgumentException if it is null
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param blankMessage blankMessage
     */
    private static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /**
     * Method to validate a condition is true, throwing an IllegalArgumentException if it is false
     *
     * @param condition condition
     * @param message message
     */
    private static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a condition is false, throwing an IllegalArgumentException if it is true
     *
     * @param condition condition
     * @param message message
     */
    private static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
