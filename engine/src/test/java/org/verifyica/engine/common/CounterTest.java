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
public class CounterTest {

    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {

        @Test
        @DisplayName("Should create counter with valid name and description")
        public void shouldCreateCounterWithValidNameAndDescription() {
            Counter counter = new Counter("test-counter", "Test counter description");

            assertThat(counter.name()).isEqualTo("test-counter");
            assertThat(counter.description()).isEqualTo("Test counter description");
            assertThat(counter.count()).isZero();
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        public void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> new Counter(null, "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is null");
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        public void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> new Counter("   ", "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is blank");
        }

        @Test
        @DisplayName("Should throw exception when name is empty")
        public void shouldThrowExceptionWhenNameIsEmpty() {
            assertThatThrownBy(() -> new Counter("", "description"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name is blank");
        }

        @Test
        @DisplayName("Should throw exception when description is null")
        public void shouldThrowExceptionWhenDescriptionIsNull() {
            assertThatThrownBy(() -> new Counter("name", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is null");
        }

        @Test
        @DisplayName("Should throw exception when description is blank")
        public void shouldThrowExceptionWhenDescriptionIsBlank() {
            assertThatThrownBy(() -> new Counter("name", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is blank");
        }

        @Test
        @DisplayName("Should throw exception when description is empty")
        public void shouldThrowExceptionWhenDescriptionIsEmpty() {
            assertThatThrownBy(() -> new Counter("name", ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("description is blank");
        }
    }

    @Nested
    @DisplayName("Increment Tests")
    public class IncrementTests {

        @Test
        @DisplayName("Should increment counter by 1")
        public void shouldIncrementCounterByOne() {
            Counter counter = new Counter("test", "description");

            counter.increment();

            assertThat(counter.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should increment counter multiple times")
        public void shouldIncrementCounterMultipleTimes() {
            Counter counter = new Counter("test", "description");

            counter.increment();
            counter.increment();
            counter.increment();

            assertThat(counter.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should increment counter by specific value")
        public void shouldIncrementCounterBySpecificValue() {
            Counter counter = new Counter("test", "description");

            counter.increment(5);

            assertThat(counter.count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should increment counter by zero")
        public void shouldIncrementCounterByZero() {
            Counter counter = new Counter("test", "description");

            counter.increment(0);

            assertThat(counter.count()).isZero();
        }

        @Test
        @DisplayName("Should increment counter by large value")
        public void shouldIncrementCounterByLargeValue() {
            Counter counter = new Counter("test", "description");

            counter.increment(Long.MAX_VALUE / 2);
            counter.increment(100);

            assertThat(counter.count()).isEqualTo((Long.MAX_VALUE / 2) + 100);
        }

        @Test
        @DisplayName("Should throw exception when increment value is negative")
        public void shouldThrowExceptionWhenIncrementValueIsNegative() {
            Counter counter = new Counter("test", "description");

            assertThatThrownBy(() -> counter.increment(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("value must be >= 0");
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    public class ThreadSafetyTests {

        @Test
        @DisplayName("Should be thread-safe for concurrent increments")
        public void shouldBeThreadSafeForConcurrentIncrements() throws InterruptedException {
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
        public void shouldBeThreadSafeForConcurrentIncrementsWithValues() throws InterruptedException {
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
    public class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when name and description match")
        public void shouldBeEqualWhenNameAndDescriptionMatch() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            assertThat(counter1).isEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal when name differs")
        public void shouldNotBeEqualWhenNameDiffers() {
            Counter counter1 = new Counter("test1", "description");
            Counter counter2 = new Counter("test2", "description");

            assertThat(counter1).isNotEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal when description differs")
        public void shouldNotBeEqualWhenDescriptionDiffers() {
            Counter counter1 = new Counter("test", "description1");
            Counter counter2 = new Counter("test", "description2");

            assertThat(counter1).isNotEqualTo(counter2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        public void shouldNotBeEqualToNull() {
            Counter counter = new Counter("test", "description");

            assertThat(counter).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        public void shouldNotBeEqualToDifferentType() {
            Counter counter = new Counter("test", "description");

            assertThat(counter).isNotEqualTo("test");
        }

        @Test
        @DisplayName("Should have same hashCode when equal")
        public void shouldHaveSameHashCodeWhenEqual() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            assertThat(counter1.hashCode()).isEqualTo(counter2.hashCode());
        }

        @Test
        @DisplayName("Should be equal regardless of count")
        public void shouldBeEqualRegardlessOfCount() {
            Counter counter1 = new Counter("test", "description");
            Counter counter2 = new Counter("test", "description");

            counter1.increment(5);
            counter2.increment(10);

            assertThat(counter1).isEqualTo(counter2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    public class ToStringTests {

        @Test
        @DisplayName("Should contain name, description and count in string representation")
        public void shouldContainNameDescriptionAndCountInStringRepresentation() {
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
        public void shouldShowZeroCountInitially() {
            Counter counter = new Counter("test", "description");

            String result = counter.toString();

            assertThat(result).contains("count=0");
        }
    }

    @Nested
    @DisplayName("Register Tests")
    public class RegisterTests {

        @Test
        @DisplayName("Should register counter in map")
        public void shouldRegisterCounterInMap() {
            Counter counter = new Counter("test", "description");
            Map<String, Counter> map = new HashMap<>();

            counter.register(map);

            assertThat(map).containsKey("test").containsValue(counter);
        }

        @Test
        @DisplayName("Should throw exception when map is null")
        public void shouldThrowExceptionWhenMapIsNull() {
            Counter counter = new Counter("test", "description");

            assertThatThrownBy(() -> counter.register(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("map is null");
        }

        @Test
        @DisplayName("Should replace existing counter with same name")
        public void shouldReplaceExistingCounterWithSameName() {
            Counter counter1 = new Counter("test", "description1");
            Counter counter2 = new Counter("test", "description2");
            Map<String, Counter> map = new HashMap<>();

            counter1.register(map);
            counter2.register(map);

            assertThat(map).hasSize(1).containsValue(counter2);
        }
    }
}
