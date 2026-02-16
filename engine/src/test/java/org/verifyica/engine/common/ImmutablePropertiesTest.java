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

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import org.junit.jupiter.api.*;

@DisplayName("ImmutableProperties Tests")
class ImmutablePropertiesTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create immutable properties from existing properties")
        void shouldCreateImmutablePropertiesFromExistingProperties() {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");

            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable.get("key1")).isEqualTo("value1");
            assertThat(immutable.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should create immutable properties from empty properties")
        void shouldCreateImmutablePropertiesFromEmptyProperties() {
            Properties source = new Properties();

            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable).isEmpty();
        }

        @Test
        @DisplayName("Should not be affected by changes to source properties")
        void shouldNotBeAffectedByChangesToSourceProperties() {
            Properties source = new Properties();
            source.put("key1", "value1");

            ImmutableProperties immutable = new ImmutableProperties(source);
            source.put("key2", "value2");

            assertThat(immutable.get("key1")).isEqualTo("value1");
            assertThat(immutable.get("key2")).isNull();
        }
    }

    @Nested
    @DisplayName("Get Tests")
    class GetTests {

        @Test
        @DisplayName("Should get property value")
        void shouldGetPropertyValue() {
            Properties source = new Properties();
            source.put("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should return null for non-existent key")
        void shouldReturnNullForNonExistentKey() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should get property using getProperty")
        void shouldGetPropertyUsingGetProperty() {
            Properties source = new Properties();
            source.setProperty("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable.getProperty("key1")).isEqualTo("value1");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should throw exception on put")
        void shouldThrowExceptionOnPut() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThatThrownBy(() -> immutable.put("key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on setProperty")
        void shouldThrowExceptionOnSetProperty() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThatThrownBy(() -> immutable.setProperty("key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on remove")
        void shouldThrowExceptionOnRemove() {
            Properties source = new Properties();
            source.put("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThatThrownBy(() -> immutable.remove("key1"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on clear")
        void shouldThrowExceptionOnClear() {
            Properties source = new Properties();
            source.put("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThatThrownBy(() -> immutable.clear())
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on putAll")
        void shouldThrowExceptionOnPutAll() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);
            Map<String, String> map = new HashMap<>();
            map.put("key", "value");

            assertThatThrownBy(() -> immutable.putAll(map))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on load from InputStream")
        void shouldThrowExceptionOnLoadFromInputStream() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);
            ByteArrayInputStream inputStream = new ByteArrayInputStream("key=value".getBytes());

            assertThatThrownBy(() -> immutable.load(inputStream))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on load from Reader")
        void shouldThrowExceptionOnLoadFromReader() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);
            StringReader reader = new StringReader("key=value");

            assertThatThrownBy(() -> immutable.load(reader))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on store to OutputStream")
        void shouldThrowExceptionOnStoreToOutputStream() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            assertThatThrownBy(() -> immutable.store(outputStream, "comment"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }

        @Test
        @DisplayName("Should throw exception on store to Writer")
        void shouldThrowExceptionOnStoreToWriter() {
            Properties source = new Properties();
            ImmutableProperties immutable = new ImmutableProperties(source);
            StringWriter writer = new StringWriter();

            assertThatThrownBy(() -> immutable.store(writer, "comment"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Cannot modify an immutable properties instance.");
        }
    }

    @Nested
    @DisplayName("Collection Views Tests")
    class CollectionViewsTests {

        @Test
        @DisplayName("Should return unmodifiable entrySet")
        void shouldReturnUnmodifiableEntrySet() {
            Properties source = new Properties();
            source.put("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            Set<Map.Entry<Object, Object>> entrySet = immutable.entrySet();

            assertThat(entrySet).hasSize(1);
            assertThatThrownBy(() -> entrySet.clear()).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return unmodifiable stringPropertyNames")
        void shouldReturnUnmodifiableStringPropertyNames() {
            Properties source = new Properties();
            source.setProperty("key1", "value1");
            ImmutableProperties immutable = new ImmutableProperties(source);

            Set<String> propertyNames = immutable.stringPropertyNames();

            assertThat(propertyNames).hasSize(1);
            assertThatThrownBy(() -> propertyNames.clear()).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return propertyNames enumeration")
        void shouldReturnPropertyNamesEnumeration() {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");
            ImmutableProperties immutable = new ImmutableProperties(source);

            @SuppressWarnings("unchecked")
            Enumeration<Object> propertyNames = (Enumeration<Object>) immutable.propertyNames();
            List<Object> names = Collections.list(propertyNames);

            assertThat(names).hasSize(2).contains("key1", "key2");
        }

        @Test
        @DisplayName("Should return elements enumeration")
        void shouldReturnElementsEnumeration() {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");
            ImmutableProperties immutable = new ImmutableProperties(source);

            Enumeration<Object> elements = immutable.elements();
            List<Object> values = Collections.list(elements);

            assertThat(values).hasSize(2).contains("value1", "value2");
        }
    }

    @Nested
    @DisplayName("Multiple Properties Tests")
    class MultiplePropertiesTests {

        @Test
        @DisplayName("Should contain all properties from source")
        void shouldContainAllPropertiesFromSource() {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");
            source.put("key3", "value3");

            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable).hasSize(3);
            assertThat(immutable.get("key1")).isEqualTo("value1");
            assertThat(immutable.get("key2")).isEqualTo("value2");
            assertThat(immutable.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("Should iterate over all properties")
        void shouldIterateOverAllProperties() {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");

            ImmutableProperties immutable = new ImmutableProperties(source);

            int count = 0;
            for (Map.Entry<Object, Object> entry : immutable.entrySet()) {
                count++;
                assertThat(entry.getKey()).isIn("key1", "key2");
                assertThat(entry.getValue()).isIn("value1", "value2");
            }

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should be safe for concurrent reads")
        void shouldBeSafeForConcurrentReads() throws InterruptedException {
            Properties source = new Properties();
            source.put("key1", "value1");
            source.put("key2", "value2");
            ImmutableProperties immutable = new ImmutableProperties(source);

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 1000; j++) {
                        results.add(immutable.get("key1").equals("value1"));
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(results).hasSize(threadCount * 1000).allMatch(result -> result);
        }
    }

    @Nested
    @DisplayName("Special Cases Tests")
    class SpecialCasesTests {

        @Test
        @DisplayName("Should reject null values (Properties backed by Hashtable doesn't support nulls)")
        void shouldRejectNullValues() {
            Properties source = new Properties();

            assertThatThrownBy(() -> source.put("key", null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle properties with empty string values")
        void shouldHandlePropertiesWithEmptyStringValues() {
            Properties source = new Properties();
            source.put("key1", "");

            ImmutableProperties immutable = new ImmutableProperties(source);

            assertThat(immutable.get("key1")).isEqualTo("");
        }
    }
}
