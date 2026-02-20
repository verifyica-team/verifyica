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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;

@DisplayName("OrderedProperties Tests")
public class OrderedPropertiesTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create empty properties")
        public void shouldCreateEmptyProperties() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should initialize with empty ordered map")
        public void shouldInitializeWithEmptyOrderedMap() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties.keySet()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Put and Get Tests")
    public class PutAndGetTests {

        @Test
        @DisplayName("Should store and retrieve string property")
        public void shouldStoreAndRetrieveStringProperty() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("key1", "value1");

            assertThat(properties.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should store multiple properties")
        public void shouldStoreMultipleProperties() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("key1", "value1");
            properties.put("key2", "value2");
            properties.put("key3", "value3");

            assertThat(properties.get("key1")).isEqualTo("value1");
            assertThat(properties.get("key2")).isEqualTo("value2");
            assertThat(properties.get("key3")).isEqualTo("value3");
        }

        @Test
        @DisplayName("Should replace existing property value")
        public void shouldReplaceExistingPropertyValue() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("key1", "value1");
            properties.put("key1", "value2");

            assertThat(properties.get("key1")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should return previous value when replacing")
        public void shouldReturnPreviousValueWhenReplacing() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("key1", "value1");
            Object oldValue = properties.put("key1", "value2");

            assertThat(oldValue).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should return null for non-existent key")
        public void shouldReturnNullForNonExistentKey() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties.get("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("Ordering Tests")
    public class OrderingTests {

        @Test
        @DisplayName("Should maintain sorted order by keys")
        public void shouldMaintainSortedOrderByKeys() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("z", "26");
            properties.put("a", "1");
            properties.put("m", "13");
            properties.put("b", "2");

            List<Object> keys = new ArrayList<>(properties.keySet());

            assertThat(keys).containsExactly("a", "b", "m", "z");
        }

        @Test
        @DisplayName("Should maintain order when iterating over keys")
        public void shouldMaintainOrderWhenIteratingOverKeys() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("3", "three");
            properties.put("1", "one");
            properties.put("2", "two");

            List<Object> keys = new ArrayList<>();
            Enumeration<Object> enumeration = properties.keys();
            while (enumeration.hasMoreElements()) {
                keys.add(enumeration.nextElement());
            }

            assertThat(keys).containsExactly("1", "2", "3");
        }

        @Test
        @DisplayName("Should maintain order in entrySet")
        public void shouldMaintainOrderInEntrySet() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("c", "3");
            properties.put("a", "1");
            properties.put("b", "2");

            List<String> keys =
                    properties.entrySet().stream().map(e -> (String) e.getKey()).collect(Collectors.toList());

            assertThat(keys).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should maintain order in values")
        public void shouldMaintainOrderInValues() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("c", "value-c");
            properties.put("a", "value-a");
            properties.put("b", "value-b");

            List<Object> values = new ArrayList<>(properties.values());

            assertThat(values).containsExactly("value-a", "value-b", "value-c");
        }

        @Test
        @DisplayName("Should maintain order with numeric string keys")
        public void shouldMaintainOrderWithNumericStringKeys() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("10", "ten");
            properties.put("2", "two");
            properties.put("1", "one");

            List<Object> keys = new ArrayList<>(properties.keySet());

            // String comparison: "1", "10", "2"
            assertThat(keys).containsExactly("1", "10", "2");
        }
    }

    @Nested
    @DisplayName("Remove Tests")
    public class RemoveTests {

        @Test
        @DisplayName("Should remove existing property")
        public void shouldRemoveExistingProperty() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            properties.remove("key1");

            assertThat(properties.get("key1")).isNull();
            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should return removed value")
        public void shouldReturnRemovedValue() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            Object removedValue = properties.remove("key1");

            assertThat(removedValue).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should return null when removing non-existent key")
        public void shouldReturnNullWhenRemovingNonExistentKey() {
            OrderedProperties properties = new OrderedProperties();

            Object removedValue = properties.remove("nonexistent");

            assertThat(removedValue).isNull();
        }

        @Test
        @DisplayName("Should maintain order after removal")
        public void shouldMaintainOrderAfterRemoval() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("a", "1");
            properties.put("b", "2");
            properties.put("c", "3");

            properties.remove("b");

            List<Object> keys = new ArrayList<>(properties.keySet());
            assertThat(keys).containsExactly("a", "c");
        }
    }

    @Nested
    @DisplayName("Clear Tests")
    public class ClearTests {

        @Test
        @DisplayName("Should clear all properties")
        public void shouldClearAllProperties() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");
            properties.put("key2", "value2");

            properties.clear();

            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should clear ordered map")
        public void shouldClearOrderedMap() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            properties.clear();

            assertThat(properties.keySet()).isEmpty();
        }

        @Test
        @DisplayName("Should allow adding properties after clear")
        public void shouldAllowAddingPropertiesAfterClear() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");
            properties.clear();

            properties.put("key2", "value2");

            assertThat(properties.get("key2")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("Load Tests")
    public class LoadTests {

        @Test
        @DisplayName("Should load properties from InputStream")
        public void shouldLoadPropertiesFromInputStream() throws IOException {
            String propertiesContent = "key1=value1\nkey2=value2\n";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(propertiesContent.getBytes());
            OrderedProperties properties = new OrderedProperties();

            properties.load(inputStream);

            assertThat(properties.get("key1")).isEqualTo("value1");
            assertThat(properties.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should load properties from Reader")
        public void shouldLoadPropertiesFromReader() throws IOException {
            String propertiesContent = "key1=value1\nkey2=value2\n";
            StringReader reader = new StringReader(propertiesContent);
            OrderedProperties properties = new OrderedProperties();

            properties.load(reader);

            assertThat(properties.get("key1")).isEqualTo("value1");
            assertThat(properties.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("Should maintain order after loading")
        public void shouldMaintainOrderAfterLoading() throws IOException {
            String propertiesContent = "z=26\na=1\nm=13\n";
            StringReader reader = new StringReader(propertiesContent);
            OrderedProperties properties = new OrderedProperties();

            properties.load(reader);

            List<Object> keys = new ArrayList<>(properties.keySet());
            assertThat(keys).containsExactly("a", "m", "z");
        }

        @Test
        @DisplayName("Should replace existing properties when loading")
        public void shouldReplaceExistingPropertiesWhenLoading() throws IOException {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "oldValue");

            String propertiesContent = "key1=newValue\n";
            StringReader reader = new StringReader(propertiesContent);

            properties.load(reader);

            assertThat(properties.get("key1")).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("Store Tests")
    public class StoreTests {

        @Test
        @DisplayName("Should store properties to OutputStream")
        public void shouldStorePropertiesToOutputStream() throws IOException {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");
            properties.put("key2", "value2");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            properties.store(outputStream, "Test comment");

            String output = outputStream.toString();
            assertThat(output)
                    .contains("key1")
                    .contains("value1")
                    .contains("key2")
                    .contains("value2");
        }

        @Test
        @DisplayName("Should store properties to Writer")
        public void shouldStorePropertiesToWriter() throws IOException {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");
            properties.put("key2", "value2");

            StringWriter writer = new StringWriter();
            properties.store(writer, "Test comment");

            String output = writer.toString();
            assertThat(output)
                    .contains("key1")
                    .contains("value1")
                    .contains("key2")
                    .contains("value2");
        }

        @Test
        @DisplayName("Should include comment in output")
        public void shouldIncludeCommentInOutput() throws IOException {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            StringWriter writer = new StringWriter();
            properties.store(writer, "Custom comment");

            String output = writer.toString();
            assertThat(output).contains("Custom comment");
        }
    }

    @Nested
    @DisplayName("Elements Tests")
    public class ElementsTests {

        @Test
        @DisplayName("Should return empty enumeration for empty properties")
        public void shouldReturnEmptyEnumerationForEmptyProperties() {
            OrderedProperties properties = new OrderedProperties();

            Enumeration<Object> elements = properties.elements();

            assertThat(elements.hasMoreElements()).isFalse();
        }

        @Test
        @DisplayName("Should return values in sorted order")
        public void shouldReturnValuesInSortedOrder() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("c", "value-c");
            properties.put("a", "value-a");
            properties.put("b", "value-b");

            List<Object> values = new ArrayList<>();
            Enumeration<Object> enumeration = properties.elements();
            while (enumeration.hasMoreElements()) {
                values.add(enumeration.nextElement());
            }

            assertThat(values).containsExactly("value-a", "value-b", "value-c");
        }

        @Test
        @DisplayName("Should return values after removal")
        public void shouldReturnValuesAfterRemoval() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("a", "value-a");
            properties.put("b", "value-b");
            properties.put("c", "value-c");

            properties.remove("b");

            List<Object> values = new ArrayList<>();
            Enumeration<Object> enumeration = properties.elements();
            while (enumeration.hasMoreElements()) {
                values.add(enumeration.nextElement());
            }

            assertThat(values).containsExactly("value-a", "value-c");
        }
    }

    @Nested
    @DisplayName("Inherited Methods Tests")
    public class InheritedMethodsTests {

        @Test
        @DisplayName("Should return correct size")
        public void shouldReturnCorrectSize() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties.size()).isEqualTo(0);

            properties.put("key1", "value1");
            assertThat(properties.size()).isEqualTo(1);

            properties.put("key2", "value2");
            assertThat(properties.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct isEmpty state")
        public void shouldReturnCorrectIsEmptyState() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties.isEmpty()).isTrue();

            properties.put("key1", "value1");
            assertThat(properties.isEmpty()).isFalse();

            properties.remove("key1");
            assertThat(properties.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should check containsKey correctly")
        public void shouldCheckContainsKeyCorrectly() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            assertThat(properties.containsKey("key1")).isTrue();
            assertThat(properties.containsKey("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check containsValue correctly")
        public void shouldCheckContainsValueCorrectly() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            assertThat(properties.containsValue("value1")).isTrue();
            assertThat(properties.containsValue("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should return null when putting new key")
        public void shouldReturnNullWhenPuttingNewKey() {
            OrderedProperties properties = new OrderedProperties();

            Object result = properties.put("key1", "value1");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should use getProperty for string values")
        public void shouldUseGetPropertyForStringValues() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            assertThat(properties.getProperty("key1")).isEqualTo("value1");
            assertThat(properties.getProperty("nonexistent")).isNull();
        }

        @Test
        @DisplayName("Should use getProperty with default value")
        public void shouldUseGetPropertyWithDefaultValue() {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");

            assertThat(properties.getProperty("key1", "default")).isEqualTo("value1");
            assertThat(properties.getProperty("nonexistent", "default")).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("Null Handling Tests")
    public class NullHandlingTests {

        @Test
        @DisplayName("Should return null for get with non-existent key")
        public void shouldReturnNullForGetWithNonExistentKey() {
            OrderedProperties properties = new OrderedProperties();

            assertThat(properties.get("nonexistent")).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    public class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty load from InputStream")
        public void shouldHandleEmptyLoadFromInputStream() throws IOException {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
            OrderedProperties properties = new OrderedProperties();

            properties.load(inputStream);

            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty load from Reader")
        public void shouldHandleEmptyLoadFromReader() throws IOException {
            StringReader reader = new StringReader("");
            OrderedProperties properties = new OrderedProperties();

            properties.load(reader);

            assertThat(properties).isEmpty();
        }

        @Test
        @DisplayName("Should handle single property")
        public void shouldHandleSingleProperty() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("onlyKey", "onlyValue");

            assertThat(properties.size()).isEqualTo(1);
            assertThat(properties.get("onlyKey")).isEqualTo("onlyValue");
            assertThat(new ArrayList<>(properties.keySet())).containsExactly("onlyKey");
        }

        @Test
        @DisplayName("Should handle special characters in keys and values")
        public void shouldHandleSpecialCharactersInKeysAndValues() {
            OrderedProperties properties = new OrderedProperties();

            properties.put("key with spaces", "value with spaces");
            properties.put("key=with=equals", "value=with=equals");
            properties.put("key:with:colons", "value:with:colons");

            assertThat(properties.get("key with spaces")).isEqualTo("value with spaces");
            assertThat(properties.get("key=with=equals")).isEqualTo("value=with=equals");
            assertThat(properties.get("key:with:colons")).isEqualTo("value:with:colons");
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent puts")
        public void shouldBeThreadSafeForConcurrentPuts() throws InterruptedException {
            OrderedProperties properties = new OrderedProperties();
            int threadCount = 10;
            int operationsPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        properties.put("key-" + threadIndex + "-" + j, "value-" + j);
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(properties).hasSize(threadCount * operationsPerThread);
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent reads")
        public void shouldBeThreadSafeForConcurrentReads() throws InterruptedException {
            OrderedProperties properties = new OrderedProperties();
            properties.put("key1", "value1");
            properties.put("key2", "value2");

            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        results.add(properties.get("key1").equals("value1"));
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(results).hasSize(threadCount * 100).allMatch(result -> result);
        }
    }
}
