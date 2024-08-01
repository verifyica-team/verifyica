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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Class to implement a Key */
public class Key {

    private final Object[] objects;

    /** Constructor */
    private Key(Object[] objects) {
        this.objects = objects;
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

        return new Key(objects);
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

        return of(objects.toArray());
    }

    @Override
    public String toString() {
        return "Key{" + "objects=" + Arrays.toString(objects) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return Objects.deepEquals(objects, key.objects);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(objects);
    }
}
