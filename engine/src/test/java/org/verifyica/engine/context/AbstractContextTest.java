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
        context = new TestContext();
    }

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should initialize with empty map")
        public void shouldInitializeWithEmptyMap() {
            ExtendedMap<String, Object> map = context.getMap();

            assertThat(map).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should return same map instance on multiple calls")
        public void shouldReturnSameMapInstanceOnMultipleCalls() {
            ExtendedMap<String, Object> map1 = context.getMap();
            ExtendedMap<String, Object> map2 = context.getMap();

            assertThat(map1).isSameAs(map2);
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
        @DisplayName("Should support type-safe casting with getAs")
        public void shouldSupportTypeSafeCastingWithGetAs() {
            context.getMap().put("stringKey", "value");
            context.getMap().put("intKey", 42);

            String stringValue = context.getMap().getAs("stringKey", String.class);
            Integer intValue = context.getMap().getAs("intKey", Integer.class);

            assertThat(stringValue).isEqualTo("value");
            assertThat(intValue).isEqualTo(42);
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
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent access")
        public void shouldBeThreadSafeForConcurrentAccess() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread-" + threadIndex + "-key-" + j;
                        context.getMap().put(key, j);
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(context.getMap()).hasSize(threadCount * operationsPerThread);
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
