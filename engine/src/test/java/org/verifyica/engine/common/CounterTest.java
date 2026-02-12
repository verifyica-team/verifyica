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

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;

@DisplayName("Counter Tests")
class CounterTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create counter with valid name and description")
        void shouldCreateCounterWithValidNameAndDescription() {
            Counter counter = new Counter("test-counter", "Test counter description");

            assertThat(counter.name()).isEqualTo("test-counter");
            assertThat(counter.description()).isEqualTo("Test counter description");
            assertThat(counter.count()).isZero();
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> new Counter(null, "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is null");
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> new Counter("   ", "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is blank");
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            assertThatThrownBy(() -> new Counter("", "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is blank");
        }

        @Test
        @DisplayName("Should throw exception when description is null")
        void shouldThrowExceptionWhenDescriptionIsNull() {
            assertThatThrownBy(() -> new Counter("name", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is null");
        }

        @Test
        @DisplayName("Should throw exception when description is blank")
        void shouldThrowExceptionWhenDescriptionIsBlank() {
            assertThatThrownBy(() -> new Counter("name", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is blank");
        }

        @Test
        @DisplayName("Should throw exception when description is empty")
        void shouldThrowExceptionWhenDescriptionIsEmpty() {
            assertThatThrownBy(() -> new Counter("name", ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is blank");
        }
    }

    @Nested
    @DisplayName("Increment Tests")
    class IncrementTests {

        @Test
        @DisplayName("Should increment counter by 1")
        void shouldIncrementCounterByOne() {
            Counter counter = new Counter("test", "description");

            counter.increment();

            assertThat(counter.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should increment counter multiple times")
        void shouldIncrementCounterMultipleTimes() {
            Counter counter = new Counter("test", "description");

            counter.increment();
            counter.increment();
            counter.increment();

            assertThat(counter.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should increment counter by specific value")
        void shouldIncrementCounterBySpecificValue() {
            Counter counter = new Counter("test", "description");

            counter.increment(5);

            assertThat(counter.count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should increment counter by zero")
        void shouldIncrementCounterByZero() {
            Counter counter = new Counter("test", "description");

            counter.increment(0);

            assertThat(counter.count()).isZero();
        }

        @Test
        @DisplayName("Should increment counter by large value")
        void shouldIncrementCounterByLargeValue() {
            Counter counter = new Counter("test", "description");

            counter.increment(Long.MAX_VALUE / 2);
            counter.increment(100);

            assertThat(counter.count()).isEqualTo((Long.MAX_VALUE / 2) + 100);
        }

        @Test
        @DisplayName("Should throw exception when increment value is negative")
        void shouldThrowExceptionWhenIncrementValueIsNegative() {
            Counter counter = new Counter("test", "description");

            assertThatThrownBy(() -> counter.increment(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be >= 0");
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent increments")
        void shouldBeThreadSafeForConcurrentIncrements() throws InterruptedException {
            Counter counter = new Counter("test", "description");
            int threadCount = 10;
            int incrementsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(counter.count()).isEqualTo(threadCount * incrementsPerThread);
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent increments with values")
        void shouldBeThreadSafeForConcurrentIncrementsWithValues() throws InterruptedException {
            Counter counter = new Counter("test", "description");
            int threadCount = 10;
            int incrementsPerThread = 100;
            int incrementValue = 5;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment(incrementValue);
                    }
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(counter.count()).isEqualTo(threadCount * incrementsPerThread * incrementValue);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when name and description match")
        void shouldBeEqualWhenNameAndDescriptionMatch() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            assertThat(counter1).isEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal when name differs")
        void shouldNotBeEqualWhenNameDiffers() {
            Counter counter1 = new Counter("test1", "description");
            Counter counter2 = new Counter("test2", "description");

            assertThat(counter1).isNotEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal when description differs")
        void shouldNotBeEqualWhenDescriptionDiffers() {
            Counter counter1 = new Counter("test", "description1");
            Counter counter2 = new Counter("test", "description2");

            assertThat(counter1).isNotEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Counter counter = new Counter("test", "description");

            assertThat(counter).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Counter counter = new Counter("test", "description");

            assertThat(counter).isNotEqualTo("test");
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        void shouldHaveSameHashCodeWhenEqual() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            assertThat(counter1.hashCode()).isEqualTo(counter2.hashCode());
        }

        @Test
        @DisplayName("Should be equal regardless of count")
        void shouldBeEqualRegardlessOfCount() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            counter1.increment(5);
            counter2.increment(10);

            assertThat(counter1).isEqualTo(counter2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain name, description and count in string representation")
        void shouldContainNameDescriptionAndCountInStringRepresentation() {
            Counter counter = new Counter("test-counter", "Test description");
            counter.increment(5);

            String result = counter.toString();

            assertThat(result)
                    .contains("name='test-counter'")
                    .contains("description='Test description'")
                    .contains("count=5");
        }

        @Test
        @DisplayName("Should show zero count initially")
        void shouldShowZeroCountInitially() {
            Counter counter = new Counter("test", "description");

            String result = counter.toString();

            assertThat(result).contains("count=0");
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register counter in map")
        void shouldRegisterCounterInMap() {
            Counter counter = new Counter("test", "description");
            Map<String, Counter> map = new HashMap<>();

            counter.register(map);

            assertThat(map).containsKey("test").containsValue(counter);
        }

        @Test
        @DisplayName("Should throw exception when map is null")
        void shouldThrowExceptionWhenMapIsNull() {
            Counter counter = new Counter("test", "description");

            assertThatThrownBy(() -> counter.register(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("map is null");
        }

        @Test
        @DisplayName("Should replace existing counter with same name")
        void shouldReplaceExistingCounterWithSameName() {
            Counter counter1 = new Counter("test", "description1");
            Counter counter2 = new Counter("test", "description2");
            Map<String, Counter> map = new HashMap<>();

            counter1.register(map);
            counter2.register(map);

            assertThat(map).hasSize(1).containsValue(counter2);
        }
    }
}
