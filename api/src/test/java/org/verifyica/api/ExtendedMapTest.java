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

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExtendedMap Tests")
public class ExtendedMapTest {

    @Test
    @DisplayName("Should perform common ExtendedMap operations")
    public void testCommon() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        extendedMap.put("foo", 1);

        assertThat((Object) extendedMap.getAs("foo")).isNotNull();
        assertThat((Object) extendedMap.getAs("foo")).isInstanceOf(Integer.class);
        assertThat((Integer) extendedMap.getAs("foo")).isEqualTo(1);

        assertThat(extendedMap.getAs("foo", Integer.class)).isNotNull();
        assertThat(extendedMap.getAs("foo", Integer.class)).isInstanceOf(Integer.class);
        assertThat(extendedMap.getAs("foo", Integer.class)).isEqualTo(1);

        final Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Integer.class);
        assertThat((Integer) value).isEqualTo(1);

        assertThat(extendedMap.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should handle inheritance in ExtendedMap")
    public void testInheritance() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

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

        final Object value = extendedMap.removeAs("foo");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(SubClass.class);
        assertThat(value).isInstanceOf(SuperClass.class);
        assertThat(((SubClass) value).getValue()).isEqualTo("bar");
        assertThat(((SuperClass) value).getValue()).isEqualTo("bar");

        assertThat(extendedMap.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when getAs type is null")
    public void testGetAsWithNullType() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", "bar");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> extendedMap.getAs("foo", null))
                .withMessage("type is null");
    }

    @Test
    @DisplayName("Should throw exception when removeAs type is null")
    public void testRemoveAsWithNullType() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", "bar");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> extendedMap.removeAs("foo", null))
                .withMessage("type is null");
    }

    @Test
    @DisplayName("Should return null for non-existent key in getAs")
    public void testGetAsNonExistentKey() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        final Object result = extendedMap.getAs("nonexistent");
        assertThat(result).isNull();
        assertThat(extendedMap.getAs("nonexistent", String.class)).isNull();
    }

    @Test
    @DisplayName("Should return null for non-existent key in removeAs")
    public void testRemoveAsNonExistentKey() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        final Object result = extendedMap.removeAs("nonexistent");
        assertThat(result).isNull();
        assertThat(extendedMap.removeAs("nonexistent", String.class)).isNull();
    }

    @Test
    @DisplayName("Should throw ClassCastException for wrong type in getAs")
    public void testGetAsWrongType() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> extendedMap.getAs("foo", String.class));
    }

    @Test
    @DisplayName("Should throw ClassCastException for wrong type in removeAs")
    public void testRemoveAsWrongType() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> extendedMap.removeAs("foo", String.class));
    }

    @Test
    @DisplayName("Should create ExtendedMap with various constructors")
    public void testConstructors() {
        // Default constructor
        final ExtendedMap<String, Object> map1 = new ExtendedMap<>();
        assertThat(map1).isEmpty();

        // Initial capacity constructor
        final ExtendedMap<String, Object> map2 = new ExtendedMap<>(16);
        assertThat(map2).isEmpty();

        // Load factor constructor
        final ExtendedMap<String, Object> map3 = new ExtendedMap<>(16, 0.75f);
        assertThat(map3).isEmpty();

        // Concurrency level constructor
        final ExtendedMap<String, Object> map4 = new ExtendedMap<>(16, 0.75f, 4);
        assertThat(map4).isEmpty();

        // Copy constructor
        final ExtendedMap<String, Object> source = new ExtendedMap<>();
        source.put("key1", "value1");
        source.put("key2", 123);

        final ExtendedMap<String, Object> copy = new ExtendedMap<>(source);
        assertThat(copy).hasSize(2);
        assertThat(copy.get("key1")).isEqualTo("value1");
        assertThat(copy.get("key2")).isEqualTo(123);
    }

    @Test
    @DisplayName("Should throw ClassCastException for unchecked cast in getAs without type parameter")
    public void testGetAsUncheckedCastException() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        // This should throw ClassCastException when assigning to wrong type
        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            final String value = extendedMap.getAs("foo");
        });
    }

    @Test
    @DisplayName("Should throw ClassCastException for unchecked cast in removeAs without type parameter")
    public void testRemoveAsUncheckedCastException() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();
        extendedMap.put("foo", 123);

        // This should throw ClassCastException when assigning to wrong type
        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            final String value = extendedMap.removeAs("foo");
        });
    }

    @Test
    @DisplayName("Should handle different key types")
    public void testDifferentKeyTypes() {
        final ExtendedMap<Integer, Object> intKeyMap = new ExtendedMap<>();
        intKeyMap.put(1, "value1");
        intKeyMap.put(2, 123);

        assertThat((Object) intKeyMap.getAs(1)).isEqualTo("value1");
        assertThat((Object) intKeyMap.getAs(2)).isEqualTo(123);
        assertThat(intKeyMap.getAs(1, String.class)).isEqualTo("value1");
        assertThat(intKeyMap.getAs(2, Integer.class)).isEqualTo(123);

        assertThat((Object) intKeyMap.removeAs(1)).isEqualTo("value1");
        assertThat(intKeyMap.removeAs(2, Integer.class)).isEqualTo(123);
        assertThat(intKeyMap).isEmpty();
    }

    @Test
    @DisplayName("Should copy from regular Map in constructor")
    public void testCopyFromRegularMap() {
        final Map<String, Object> regularMap = new HashMap<>();
        regularMap.put("key1", "value1");
        regularMap.put("key2", 123);
        regularMap.put("key3", true);

        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>(regularMap);

        assertThat(extendedMap).hasSize(3);
        assertThat(extendedMap.getAs("key1", String.class)).isEqualTo("value1");
        assertThat(extendedMap.getAs("key2", Integer.class)).isEqualTo(123);
        assertThat(extendedMap.getAs("key3", Boolean.class)).isEqualTo(Boolean.TRUE);
    }

    @Test
    @DisplayName("Should handle empty map copy constructor")
    public void testEmptyMapCopyConstructor() {
        final Map<String, Object> emptyMap = new HashMap<>();
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>(emptyMap);

        assertThat(extendedMap.isEmpty()).isTrue();
        assertThat((Object) extendedMap.getAs("anykey")).isNull();
    }

    @Test
    @DisplayName("Should handle complex object types")
    public void testComplexObjectTypes() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        final Map<String, Integer> nestedMap = new HashMap<>();
        nestedMap.put("nested", 42);

        extendedMap.put("map", nestedMap);
        extendedMap.put("array", new int[] {1, 2, 3});

        @SuppressWarnings("unchecked")
        final Map<String, Integer> retrievedMap = extendedMap.getAs("map");
        assertThat(retrievedMap).isEqualTo(nestedMap);

        final Map<String, Integer> typedMap = extendedMap.getAs("map", Map.class);
        assertThat(typedMap).isEqualTo(nestedMap);

        final int[] retrievedArray = extendedMap.getAs("array", int[].class);
        assertThat(retrievedArray).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Should handle multiple operations on same key")
    public void testMultipleOperationsOnSameKey() {
        final ExtendedMap<String, Object> extendedMap = new ExtendedMap<>();

        // First put
        extendedMap.put("key", "value1");
        assertThat(extendedMap.getAs("key", String.class)).isEqualTo("value1");

        // Overwrite
        extendedMap.put("key", "value2");
        assertThat(extendedMap.getAs("key", String.class)).isEqualTo("value2");

        // Remove
        final String removed = extendedMap.removeAs("key", String.class);
        assertThat(removed).isEqualTo("value2");
        assertThat(extendedMap.isEmpty()).isTrue();

        // Put again
        extendedMap.put("key", 123);
        assertThat(extendedMap.getAs("key", Integer.class)).isEqualTo(123);
    }

    public static class SubClass {

        private final String value;

        public SubClass(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SuperClass extends SubClass {

        public SuperClass(final String value) {
            super(value);
        }
    }
}
