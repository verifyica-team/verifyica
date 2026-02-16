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

package org.verifyica.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class ExtendedMapTest {

    @Test
    public void testCommon() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        extendedMap.put("foo", 1);

        assertThat((Object) extendedMap.getAs("foo")).isNotNull();
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(Integer.class);
        assertThat((Integer) extendedMap.getAs("foo")).isEqualTo(1);

        assertThat(extendedMap.getAs("foo", Integer.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", Integer.class)).isInstanceOf(Integer.class);
        assertThat(extendedMap.getAs("foo", Integer.class)).isEqualTo(1);

        Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Integer.class);
        assertThat((Integer) value).isEqualTo(1);

        assertThat(extendedMap).isEmpty();
    }

    @Test
    public void testInheritance() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        extendedMap.put("foo", new SuperClass("bar"));

        assertThat((Object) extendedMap.getAs("foo")).isNotNull();
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(SuperClass.class);
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(SubClass.class);
        assertThat(((SuperClass) extendedMap.getAs("foo")).getValue()).isEqualTo("bar");
        assertThat(((SubClass) extendedMap.getAs("foo")).getValue()).isEqualTo("bar");

        assertThat(extendedMap.getAs("foo", SubClass.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", SubClass.class)).isInstanceOf(SubClass.class);
        assertThat(extendedMap.getAs("foo", SubClass.class).getValue()).isEqualTo("bar");

        assertThat(extendedMap.getAs("foo", SuperClass.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", SuperClass.class)).isInstanceOf(SuperClass.class);
        assertThat(extendedMap.getAs("foo", SuperClass.class).getValue()).isEqualTo("bar");

        Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(SubClass.class);
        assertThat(value).isInstanceOf(SuperClass.class);
        assertThat(((SubClass) value).getValue()).isEqualTo("bar");
        assertThat(((SuperClass) value).getValue()).isEqualTo("bar");

        assertThat(extendedMap).isEmpty();
    }

    @Test
    public void testGetAsWithNullType() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", "bar");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> extendedMap.getAs("foo", null))
                .withMessage("type is null");
    }

    @Test
    public void testRemoveAsWithNullType() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", "bar");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> extendedMap.removeAs("foo", null))
                .withMessage("type is null");
    }

    @Test
    public void testGetAsNonExistentKey() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        Object result = extendedMap.getAs("nonexistent");
        assertThat(result).isNull();
        assertThat(extendedMap.getAs("nonexistent", String.class)).isNull();
    }

    @Test
    public void testRemoveAsNonExistentKey() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        Object result = extendedMap.removeAs("nonexistent");
        assertThat(result).isNull();
        assertThat(extendedMap.removeAs("nonexistent", String.class)).isNull();
    }

    @Test
    public void testGetAsWrongType() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> extendedMap.getAs("foo", String.class));
    }

    @Test
    public void testRemoveAsWrongType() {
        ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> extendedMap.removeAs("foo", String.class));
    }

    @Test
    public void testConstructors() {
        // Default constructor
        ExtendedMap<String, Object> map1 = new ExtendedMap<>();
        assertThat(map1).isEmpty();

        // Initial capacity constructor
        ExtendedMap<String, Object> map2 = new ExtendedMap<>(16);
        assertThat(map2).isEmpty();

        // Load factor constructor
        ExtendedMap<String, Object> map3 = new ExtendedMap<>(16, 0.75f);
        assertThat(map3).isEmpty();

        // Concurrency level constructor
        ExtendedMap<String, Object> map4 = new ExtendedMap<>(16, 0.75f, 4);
        assertThat(map4).isEmpty();

        // Copy constructor
        ExtendedMap<String, Object> source = new ExtendedMap<>();
        source.put("key1", "value1");
        source.put("key2", 123);

        ExtendedMap<String, Object> copy = new ExtendedMap<>(source);
        assertThat(copy).hasSize(2);
        assertThat(copy.get("key1")).isEqualTo("value1");
        assertThat(copy.get("key2")).isEqualTo(123);
    }

    public static class SubClass {

        private final String value;

        public SubClass(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SuperClass extends SubClass {

        public SuperClass(String value) {
            super(value);
        }
    }
}
