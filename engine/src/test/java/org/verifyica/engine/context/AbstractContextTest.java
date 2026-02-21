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

package org.verifyica.engine.context;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.verifyica.api.Configuration;
import org.verifyica.api.ExtendedMap;

@DisplayName("AbstractContext Tests")
public class AbstractContextTest {

    private TestContext context;

    @BeforeEach
    public void setUp() {
        this.context = new TestContext();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should initialize with empty map")
        public void shouldInitializeWithEmptyMap() {
            final ExtendedMap<String, Object> map = context.getMap();

            assertThat(map).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should return same map instance on multiple calls")
        public void shouldReturnSameMapInstanceOnMultipleCalls() {
            final ExtendedMap<String, Object> map1 = context.getMap();
            final ExtendedMap<String, Object> map2 = context.getMap();

            assertThat(map1).isSameAs(map2);
        }

        @Test
        @DisplayName("Should have independent maps for different instances")
        public void shouldHaveIndependentMapsForDifferentInstances() {
            final TestContext context1 = new TestContext();
            final TestContext context2 = new TestContext();

            context1.getMap().put("key", "value1");
            context2.getMap().put("key", "value2");

            assertThat(context1.getMap().get("key")).isEqualTo("value1");
            assertThat(context2.getMap().get("key")).isEqualTo("value2");
            assertThat(context1.getMap()).isNotSameAs(context2.getMap());
        }
    }

    @Nested
    @DisplayName("Map Operations Tests")
    public class MapOperationsTests {

        @Test
        @DisplayName("Should store and retrieve values in map")
        public void shouldStoreAndRetrieveValuesInMap() {
            context.getMap().put("key1", "value1");
            context.getMap().put("key2", 123);

            assertThat(context.getMap()).containsEntry("key1", "value1").containsEntry("key2", 123);
        }

        @Test
        @DisplayName("Should reject null values (ConcurrentHashMap doesn't support nulls)")
        public void shouldRejectNullValues() {
            assertThatThrownBy(() -> context.getMap().put("nullKey", null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should reject null keys (ConcurrentHashMap doesn't support nulls)")
        public void shouldRejectNullKeys() {
            assertThatThrownBy(() -> context.getMap().put(null, "value")).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should support type-safe casting with getAs")
        public void shouldSupportTypeSafeCastingWithGetAs() {
            context.getMap().put("stringKey", "value");
            context.getMap().put("intKey", 42);

            final String stringValue = context.getMap().getAs("stringKey", String.class);
            final Integer intValue = context.getMap().getAs("intKey", Integer.class);

            assertThat(stringValue).isEqualTo("value");
            assertThat(intValue).isEqualTo(42);
        }

        @Test
        @DisplayName("Should return null for non-existent key")
        public void shouldReturnNullForNonExistentKey() {
            final Object value = context.getMap().get("nonExistentKey");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("Should remove entries")
        public void shouldRemoveEntries() {
            context.getMap().put("key", "value");
            context.getMap().remove("key");

            assertThat(context.getMap()).doesNotContainKey("key");
        }

        @Test
        @DisplayName("Should clear all entries")
        public void shouldClearAllEntries() {
            context.getMap().put("key1", "value1");
            context.getMap().put("key2", "value2");

            context.getMap().clear();

            assertThat(context.getMap()).isEmpty();
        }

        @Test
        @DisplayName("Should update existing value")
        public void shouldUpdateExistingValue() {
            context.getMap().put("key", "original");
            context.getMap().put("key", "updated");

            assertThat(context.getMap().get("key")).isEqualTo("updated");
        }

        @Test
        @DisplayName("Should handle multiple data types")
        public void shouldHandleMultipleDataTypes() {
            context.getMap().put("string", "value");
            context.getMap().put("integer", 42);
            context.getMap().put("long", 42L);
            context.getMap().put("double", 3.14);
            context.getMap().put("boolean", true);

            assertThat(context.getMap()).hasSize(5);
            assertThat(context.getMap().get("string")).isInstanceOf(String.class);
            assertThat(context.getMap().get("integer")).isInstanceOf(Integer.class);
            assertThat(context.getMap().get("long")).isInstanceOf(Long.class);
            assertThat(context.getMap().get("double")).isInstanceOf(Double.class);
            assertThat(context.getMap().get("boolean")).isInstanceOf(Boolean.class);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent writes")
        public void shouldBeThreadSafeForConcurrentWrites() throws InterruptedException {
            final int threadCount = 10;
            final int operationsPerThread = 100;
            final Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        final String key = "thread-" + threadIndex + "-key-" + j;
                        context.getMap().put(key, j);
                    }
                });
                threads[i].start();
            }

            for (final Thread thread : threads) {
                thread.join();
            }

            assertThat(context.getMap()).hasSize(threadCount * operationsPerThread);
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent reads and writes")
        public void shouldBeThreadSafeForConcurrentReadsAndWrites() throws InterruptedException {
            final int writerCount = 5;
            final int readerCount = 5;
            final int operationsPerThread = 50;
            final Thread[] threads = new Thread[writerCount + readerCount];

            // Writer threads
            for (int i = 0; i < writerCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        final String key = "writer-" + threadIndex + "-key-" + j;
                        context.getMap().put(key, j);
                    }
                });
            }

            // Reader threads
            for (int i = 0; i < readerCount; i++) {
                threads[writerCount + i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        context.getMap().size();
                        context.getMap().isEmpty();
                    }
                });
            }

            for (final Thread thread : threads) {
                thread.start();
            }

            for (final Thread thread : threads) {
                thread.join();
            }

            assertThat(context.getMap()).hasSize(writerCount * operationsPerThread);
        }
    }

    // Test implementation of AbstractContext for testing purposes
    private static class TestContext extends AbstractContext {

        @Override
        public Configuration getConfiguration() {
            return null; // Not needed for AbstractContext tests
        }
    }
}
