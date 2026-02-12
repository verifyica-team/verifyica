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

package org.verifyica.engine.descriptor;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;

@DisplayName("TestDescriptorStatus Tests")
class TestDescriptorStatusTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create passed status with singleton")
        void shouldCreatePassedStatusWithSingleton() {
            TestDescriptorStatus status1 = TestDescriptorStatus.passed();
            TestDescriptorStatus status2 = TestDescriptorStatus.passed();

            assertThat(status1).satisfies(s -> {
                assertThat(s.getType()).isEqualTo(TestDescriptorStatus.Type.PASSED);
                assertThat(s.isSuccess()).isTrue();
                assertThat(s.isFailure()).isFalse();
                assertThat(s.isSkipped()).isFalse();
                assertThat(s.getThrowable()).isNull();
            });

            assertThat(status1).isSameAs(status2);
        }

        @Test
        @DisplayName("Should create skipped status with singleton")
        void shouldCreateSkippedStatusWithSingleton() {
            TestDescriptorStatus status1 = TestDescriptorStatus.skipped();
            TestDescriptorStatus status2 = TestDescriptorStatus.skipped();

            assertThat(status1).satisfies(s -> {
                assertThat(s.getType()).isEqualTo(TestDescriptorStatus.Type.SKIPPED);
                assertThat(s.isSuccess()).isFalse();
                assertThat(s.isFailure()).isFalse();
                assertThat(s.isSkipped()).isTrue();
                assertThat(s.isSkippedWithThrowable()).isFalse();
                assertThat(s.getThrowable()).isNull();
            });

            assertThat(status1).isSameAs(status2);
        }

        @Test
        @DisplayName("Should create skipped status with throwable")
        void shouldCreateSkippedStatusWithThrowable() {
            Throwable throwable = new RuntimeException("Test skip reason");
            TestDescriptorStatus status = TestDescriptorStatus.skipped(throwable);

            assertThat(status).satisfies(s -> {
                assertThat(s.getType()).isEqualTo(TestDescriptorStatus.Type.SKIPPED);
                assertThat(s.isSuccess()).isFalse();
                assertThat(s.isFailure()).isFalse();
                assertThat(s.isSkipped()).isTrue();
                assertThat(s.isSkippedWithThrowable()).isTrue();
                assertThat(s.getThrowable()).isSameAs(throwable);
            });
        }

        @Test
        @DisplayName("Should create failed status with throwable")
        void shouldCreateFailedStatusWithThrowable() {
            Throwable throwable = new AssertionError("Test failure");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status).satisfies(s -> {
                assertThat(s.getType()).isEqualTo(TestDescriptorStatus.Type.FAILED);
                assertThat(s.isSuccess()).isFalse();
                assertThat(s.isFailure()).isTrue();
                assertThat(s.isSkipped()).isFalse();
                assertThat(s.isSkippedWithThrowable()).isFalse();
                assertThat(s.getThrowable()).isSameAs(throwable);
            });
        }

        @Test
        @DisplayName("Should create different instances for skipped with throwable")
        void shouldCreateDifferentInstancesForSkippedWithThrowable() {
            Throwable throwable1 = new RuntimeException("Reason 1");
            Throwable throwable2 = new RuntimeException("Reason 2");

            TestDescriptorStatus status1 = TestDescriptorStatus.skipped(throwable1);
            TestDescriptorStatus status2 = TestDescriptorStatus.skipped(throwable2);

            assertThat(status1).isNotSameAs(status2);
            assertThat(status1.getThrowable()).isSameAs(throwable1);
            assertThat(status2.getThrowable()).isSameAs(throwable2);
        }

        @Test
        @DisplayName("Should create different instances for failed status")
        void shouldCreateDifferentInstancesForFailedStatus() {
            Throwable throwable1 = new AssertionError("Failure 1");
            Throwable throwable2 = new AssertionError("Failure 2");

            TestDescriptorStatus status1 = TestDescriptorStatus.failed(throwable1);
            TestDescriptorStatus status2 = TestDescriptorStatus.failed(throwable2);

            assertThat(status1).isNotSameAs(status2);
            assertThat(status1.getThrowable()).isSameAs(throwable1);
            assertThat(status2.getThrowable()).isSameAs(throwable2);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("Should return correct type for passed status")
        void shouldReturnCorrectTypeForPassedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.getType()).isEqualTo(TestDescriptorStatus.Type.PASSED);
        }

        @Test
        @DisplayName("Should return correct type for skipped status")
        void shouldReturnCorrectTypeForSkippedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped();

            assertThat(status.getType()).isEqualTo(TestDescriptorStatus.Type.SKIPPED);
        }

        @Test
        @DisplayName("Should return correct type for failed status")
        void shouldReturnCorrectTypeForFailedStatus() {
            Throwable throwable = new RuntimeException("Test failure");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.getType()).isEqualTo(TestDescriptorStatus.Type.FAILED);
        }

        @Test
        @DisplayName("Should have three type enum values")
        void shouldHaveThreeTypeEnumValues() {
            TestDescriptorStatus.Type[] types = TestDescriptorStatus.Type.values();

            assertThat(types)
                    .hasSize(3)
                    .contains(
                            TestDescriptorStatus.Type.PASSED,
                            TestDescriptorStatus.Type.FAILED,
                            TestDescriptorStatus.Type.SKIPPED);
        }
    }

    @Nested
    @DisplayName("Predicate Tests")
    class PredicateTests {

        @Test
        @DisplayName("Should return true for isSuccess on passed status")
        void shouldReturnTrueForIsSuccessOnPassedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.isSuccess()).isTrue();
            assertThat(status.isFailure()).isFalse();
            assertThat(status.isSkipped()).isFalse();
        }

        @Test
        @DisplayName("Should return true for isFailure on failed status")
        void shouldReturnTrueForIsFailureOnFailedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.failed(new RuntimeException());

            assertThat(status.isSuccess()).isFalse();
            assertThat(status.isFailure()).isTrue();
            assertThat(status.isSkipped()).isFalse();
        }

        @Test
        @DisplayName("Should return true for isSkipped on skipped status")
        void shouldReturnTrueForIsSkippedOnSkippedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped();

            assertThat(status.isSuccess()).isFalse();
            assertThat(status.isFailure()).isFalse();
            assertThat(status.isSkipped()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isSkippedWithThrowable when no throwable")
        void shouldReturnFalseForIsSkippedWithThrowableWhenNoThrowable() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped();

            assertThat(status.isSkippedWithThrowable()).isFalse();
        }

        @Test
        @DisplayName("Should return true for isSkippedWithThrowable when throwable present")
        void shouldReturnTrueForIsSkippedWithThrowableWhenThrowablePresent() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped(new RuntimeException("Skip reason"));

            assertThat(status.isSkippedWithThrowable()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isSkippedWithThrowable on passed status")
        void shouldReturnFalseForIsSkippedWithThrowableOnPassedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.isSkippedWithThrowable()).isFalse();
        }

        @Test
        @DisplayName("Should return false for isSkippedWithThrowable on failed status")
        void shouldReturnFalseForIsSkippedWithThrowableOnFailedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.failed(new RuntimeException());

            assertThat(status.isSkippedWithThrowable()).isFalse();
        }
    }

    @Nested
    @DisplayName("Throwable Tests")
    class ThrowableTests {

        @Test
        @DisplayName("Should return null throwable for passed status")
        void shouldReturnNullThrowableForPassedStatus() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.getThrowable()).isNull();
        }

        @Test
        @DisplayName("Should return null throwable for skipped status without throwable")
        void shouldReturnNullThrowableForSkippedStatusWithoutThrowable() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped();

            assertThat(status.getThrowable()).isNull();
        }

        @Test
        @DisplayName("Should return throwable for skipped status with throwable")
        void shouldReturnThrowableForSkippedStatusWithThrowable() {
            Throwable throwable = new RuntimeException("Skip reason");
            TestDescriptorStatus status = TestDescriptorStatus.skipped(throwable);

            assertThat(status.getThrowable()).isSameAs(throwable);
        }

        @Test
        @DisplayName("Should return throwable for failed status")
        void shouldReturnThrowableForFailedStatus() {
            Throwable throwable = new AssertionError("Test failed");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.getThrowable()).isSameAs(throwable);
        }

        @Test
        @DisplayName("Should preserve throwable message and stack trace")
        void shouldPreserveThrowableMessageAndStackTrace() {
            Throwable throwable = new RuntimeException("Original message");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.getThrowable())
                    .hasMessage("Original message")
                    .hasStackTraceContaining("shouldPreserveThrowableMessageAndStackTrace");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status).isEqualTo(status);
        }

        @Test
        @DisplayName("Should be equal to passed singleton")
        void shouldBeEqualToPassedSingleton() {
            TestDescriptorStatus status1 = TestDescriptorStatus.passed();
            TestDescriptorStatus status2 = TestDescriptorStatus.passed();

            assertThat(status1).isEqualTo(status2);
            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to skipped singleton")
        void shouldBeEqualToSkippedSingleton() {
            TestDescriptorStatus status1 = TestDescriptorStatus.skipped();
            TestDescriptorStatus status2 = TestDescriptorStatus.skipped();

            assertThat(status1).isEqualTo(status2);
            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status).isNotEqualTo("not a status");
        }

        @Test
        @DisplayName("Should not be equal when types differ")
        void shouldNotBeEqualWhenTypesDiffer() {
            TestDescriptorStatus passed = TestDescriptorStatus.passed();
            TestDescriptorStatus skipped = TestDescriptorStatus.skipped();

            assertThat(passed).isNotEqualTo(skipped);
        }

        @Test
        @DisplayName("Should not be equal when throwables differ")
        void shouldNotBeEqualWhenThrowablesDiffer() {
            Throwable throwable1 = new RuntimeException("Error 1");
            Throwable throwable2 = new RuntimeException("Error 2");

            TestDescriptorStatus status1 = TestDescriptorStatus.failed(throwable1);
            TestDescriptorStatus status2 = TestDescriptorStatus.failed(throwable2);

            assertThat(status1).isNotEqualTo(status2);
        }

        @Test
        @DisplayName("Should be equal when same throwable")
        void shouldBeEqualWhenSameThrowable() {
            Throwable throwable = new RuntimeException("Same error");

            TestDescriptorStatus status1 = TestDescriptorStatus.failed(throwable);
            TestDescriptorStatus status2 = TestDescriptorStatus.failed(throwable);

            assertThat(status1).isEqualTo(status2);
            assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            int hashCode1 = status.hashCode();
            int hashCode2 = status.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Should have consistent hashCode with throwable")
        void shouldHaveConsistentHashCodeWithThrowable() {
            Throwable throwable = new RuntimeException("Error");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            int hashCode1 = status.hashCode();
            int hashCode2 = status.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain type in toString for passed")
        void shouldContainTypeInToStringForPassed() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.toString()).contains("type=PASSED").contains("throwable=null");
        }

        @Test
        @DisplayName("Should contain type in toString for skipped")
        void shouldContainTypeInToStringForSkipped() {
            TestDescriptorStatus status = TestDescriptorStatus.skipped();

            assertThat(status.toString()).contains("type=SKIPPED").contains("throwable=null");
        }

        @Test
        @DisplayName("Should contain type and throwable in toString for failed")
        void shouldContainTypeAndThrowableInToStringForFailed() {
            Throwable throwable = new RuntimeException("Test error");
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.toString())
                    .contains("type=FAILED")
                    .contains("throwable=")
                    .contains("RuntimeException");
        }

        @Test
        @DisplayName("Should contain throwable info for skipped with throwable")
        void shouldContainThrowableInfoForSkippedWithThrowable() {
            Throwable throwable = new RuntimeException("Skip reason");
            TestDescriptorStatus status = TestDescriptorStatus.skipped(throwable);

            assertThat(status.toString())
                    .contains("type=SKIPPED")
                    .contains("throwable=")
                    .contains("RuntimeException");
        }

        @Test
        @DisplayName("Should contain InvocationResult in toString")
        void shouldContainInvocationResultInToString() {
            TestDescriptorStatus status = TestDescriptorStatus.passed();

            assertThat(status.toString()).contains("InvocationResult");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null message in throwable")
        void shouldHandleNullMessageInThrowable() {
            Throwable throwable = new RuntimeException();
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.getThrowable()).isSameAs(throwable);
            assertThat(status.isFailure()).isTrue();
        }

        @Test
        @DisplayName("Should handle nested throwable causes")
        void shouldHandleNestedThrowableCauses() {
            Throwable cause = new IllegalArgumentException("Root cause");
            Throwable throwable = new RuntimeException("Wrapper", cause);
            TestDescriptorStatus status = TestDescriptorStatus.failed(throwable);

            assertThat(status.getThrowable()).isSameAs(throwable).hasCause(cause);
        }

        @Test
        @DisplayName("Should handle different exception types")
        void shouldHandleDifferentExceptionTypes() {
            Throwable runtime = new RuntimeException("Runtime");
            Throwable assertion = new AssertionError("Assertion");
            Throwable illegal = new IllegalStateException("Illegal");

            TestDescriptorStatus status1 = TestDescriptorStatus.failed(runtime);
            TestDescriptorStatus status2 = TestDescriptorStatus.failed(assertion);
            TestDescriptorStatus status3 = TestDescriptorStatus.failed(illegal);

            assertThat(status1.getThrowable()).isInstanceOf(RuntimeException.class);
            assertThat(status2.getThrowable()).isInstanceOf(AssertionError.class);
            assertThat(status3.getThrowable()).isInstanceOf(IllegalStateException.class);
        }
    }
}
