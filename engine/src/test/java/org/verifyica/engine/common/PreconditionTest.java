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

import org.junit.jupiter.api.*;

@DisplayName("Precondition Tests")
class PreconditionTest {

    @Nested
    @DisplayName("NotNull Tests")
    class NotNullTests {

        @Test
        @DisplayName("Should not throw exception for non-null object")
        void shouldNotThrowExceptionForNonNullObject() {
            assertThatCode(() -> Precondition.notNull("test", "message")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for non-null integer")
        void shouldNotThrowExceptionForNonNullInteger() {
            assertThatCode(() -> Precondition.notNull(123, "message")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for empty string")
        void shouldNotThrowExceptionForEmptyString() {
            assertThatCode(() -> Precondition.notNull("", "message")).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            assertThatThrownBy(() -> Precondition.notNull(null, "Object is null"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Object is null");
        }

        @Test
        @DisplayName("Should use provided message in exception")
        void shouldUseProvidedMessageInException() {
            String customMessage = "Custom error message";

            assertThatThrownBy(() -> Precondition.notNull(null, customMessage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(customMessage);
        }
    }

    @Nested
    @DisplayName("NotNullOrBlank Tests")
    class NotNullOrBlankTests {

        @Test
        @DisplayName("Should not throw exception for valid string")
        void shouldNotThrowExceptionForValidString() {
            assertThatCode(() -> Precondition.notNullOrBlank("test", "null message", "blank message"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for string with only non-whitespace")
        void shouldNotThrowExceptionForStringWithOnlyNonWhitespace() {
            assertThatCode(() -> Precondition.notNullOrBlank("abc", "null message", "blank message"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for string with leading/trailing whitespace")
        void shouldNotThrowExceptionForStringWithLeadingTrailingWhitespace() {
            assertThatCode(() -> Precondition.notNullOrBlank("  test  ", "null message", "blank message"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception with null message when string is null")
        void shouldThrowExceptionWithNullMessageWhenStringIsNull() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank(null, "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is null");
        }

        @Test
        @DisplayName("Should throw exception with blank message when string is empty")
        void shouldThrowExceptionWithBlankMessageWhenStringIsEmpty() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank("", "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is blank");
        }

        @Test
        @DisplayName("Should throw exception with blank message when string contains only spaces")
        void shouldThrowExceptionWithBlankMessageWhenStringContainsOnlySpaces() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank("   ", "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is blank");
        }

        @Test
        @DisplayName("Should throw exception with blank message when string contains only tabs")
        void shouldThrowExceptionWithBlankMessageWhenStringContainsOnlyTabs() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank("\t\t", "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is blank");
        }

        @Test
        @DisplayName("Should throw exception with blank message when string contains only newlines")
        void shouldThrowExceptionWithBlankMessageWhenStringContainsOnlyNewlines() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank("\n\n", "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is blank");
        }

        @Test
        @DisplayName("Should throw exception with blank message when string contains mixed whitespace")
        void shouldThrowExceptionWithBlankMessageWhenStringContainsMixedWhitespace() {
            assertThatThrownBy(() -> Precondition.notNullOrBlank(" \t\n ", "String is null", "String is blank"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("String is blank");
        }
    }

    @Nested
    @DisplayName("IsTrue Tests")
    class IsTrueTests {

        @Test
        @DisplayName("Should not throw exception when condition is true")
        void shouldNotThrowExceptionWhenConditionIsTrue() {
            assertThatCode(() -> Precondition.isTrue(true, "Condition is false"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for expression that evaluates to true")
        void shouldNotThrowExceptionForExpressionThatEvaluatesToTrue() {
            assertThatCode(() -> Precondition.isTrue(5 > 3, "Condition is false"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when condition is false")
        void shouldThrowExceptionWhenConditionIsFalse() {
            assertThatThrownBy(() -> Precondition.isTrue(false, "Condition is false"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Condition is false");
        }

        @Test
        @DisplayName("Should throw exception for expression that evaluates to false")
        void shouldThrowExceptionForExpressionThatEvaluatesToFalse() {
            assertThatThrownBy(() -> Precondition.isTrue(5 < 3, "Value must be greater"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Value must be greater");
        }

        @Test
        @DisplayName("Should use provided message in exception")
        void shouldUseProvidedMessageInException() {
            String customMessage = "Custom validation failed";

            assertThatThrownBy(() -> Precondition.isTrue(false, customMessage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(customMessage);
        }
    }

    @Nested
    @DisplayName("IsFalse Tests")
    class IsFalseTests {

        @Test
        @DisplayName("Should not throw exception when condition is false")
        void shouldNotThrowExceptionWhenConditionIsFalse() {
            assertThatCode(() -> Precondition.isFalse(false, "Condition is true"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not throw exception for expression that evaluates to false")
        void shouldNotThrowExceptionForExpressionThatEvaluatesToFalse() {
            assertThatCode(() -> Precondition.isFalse(5 < 3, "Condition is true"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when condition is true")
        void shouldThrowExceptionWhenConditionIsTrue() {
            assertThatThrownBy(() -> Precondition.isFalse(true, "Condition is true"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Condition is true");
        }

        @Test
        @DisplayName("Should throw exception for expression that evaluates to true")
        void shouldThrowExceptionForExpressionThatEvaluatesToTrue() {
            assertThatThrownBy(() -> Precondition.isFalse(5 > 3, "Value must not be greater"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Value must not be greater");
        }

        @Test
        @DisplayName("Should use provided message in exception")
        void shouldUseProvidedMessageInException() {
            String customMessage = "Custom validation failed";

            assertThatThrownBy(() -> Precondition.isFalse(true, customMessage))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(customMessage);
        }
    }

    @Nested
    @DisplayName("Combined Usage Tests")
    class CombinedUsageTests {

        @Test
        @DisplayName("Should validate multiple preconditions in sequence")
        void shouldValidateMultiplePreconditionsInSequence() {
            String value = "test";
            int number = 5;

            assertThatCode(() -> {
                        Precondition.notNull(value, "value is null");
                        Precondition.notNullOrBlank(value, "value is null", "value is blank");
                        Precondition.isTrue(number > 0, "number must be positive");
                        Precondition.isFalse(number < 0, "number must not be negative");
                    })
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should fail on first violation")
        void shouldFailOnFirstViolation() {
            assertThatThrownBy(() -> {
                        Precondition.notNull(null, "First check failed");
                        Precondition.isTrue(false, "Second check failed");
                    })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("First check failed");
        }
    }
}
