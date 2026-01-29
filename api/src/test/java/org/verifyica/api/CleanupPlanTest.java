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

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for CleanupPlan
 */
@DisplayName("CleanupPlan")
class CleanupPlanTest {

    private CleanupPlan cleanupPlan;

    @BeforeEach
    void setUp() {
        cleanupPlan = new CleanupPlan();
    }

    @Nested
    @DisplayName("addAction()")
    class AddActionTests {

        @Test
        @DisplayName("should add a single cleanup action successfully")
        void shouldAddSingleCleanupAction() {
            CleanupAction action = () -> {};

            CleanupPlan result = cleanupPlan.addAction(action);

            assertThat(result).isSameAs(cleanupPlan);
            assertThat(cleanupPlan.cleanupActions()).hasSize(1);
            assertThat(cleanupPlan.cleanupActions()).containsExactly(action);
        }

        @Test
        @DisplayName("should add multiple cleanup actions in order")
        void shouldAddMultipleCleanupActionsInOrder() {
            CleanupAction action1 = () -> {};
            CleanupAction action2 = () -> {};
            CleanupAction action3 = () -> {};

            cleanupPlan.addAction(action1).addAction(action2).addAction(action3);

            assertThat(cleanupPlan.cleanupActions()).hasSize(3);
            assertThat(cleanupPlan.cleanupActions()).containsExactly(action1, action2, action3);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when action is null")
        void shouldThrowExceptionWhenActionIsNull() {
            assertThatThrownBy(() -> cleanupPlan.addAction(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should allow fluent chaining")
        void shouldAllowFluentChaining() {
            CleanupAction action1 = () -> {};
            CleanupAction action2 = () -> {};

            CleanupPlan result = cleanupPlan.addAction(action1).addAction(action2);

            assertThat(result).isSameAs(cleanupPlan);
        }
    }

    @Nested
    @DisplayName("addActionIfPresent()")
    class AddActionIfPresentTests {

        @Test
        @DisplayName("should execute cleanup action when supplier returns non-null value")
        void shouldExecuteCleanupActionWhenValuePresent() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);
            String value = "test";

            cleanupPlan.addActionIfPresent(() -> value, v -> counter.incrementAndGet());
            cleanupPlan.run();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should not execute cleanup action when supplier returns null")
        void shouldNotExecuteCleanupActionWhenValueNull() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);

            cleanupPlan.addActionIfPresent(() -> null, v -> counter.incrementAndGet());
            cleanupPlan.run();

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("should pass the supplied value to the cleanup action")
        void shouldPassSuppliedValueToCleanupAction() throws Throwable {
            List<String> capturedValues = new ArrayList<>();
            String expectedValue = "testValue";

            cleanupPlan.addActionIfPresent(() -> expectedValue, capturedValues::add);
            cleanupPlan.run();

            assertThat(capturedValues).containsExactly(expectedValue);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when supplier is null")
        void shouldThrowExceptionWhenSupplierIsNull() {
            assertThatThrownBy(() -> cleanupPlan.addActionIfPresent(null, v -> {}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("supplier is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when cleanup action is null")
        void shouldThrowExceptionWhenCleanupActionIsNull() {
            assertThatThrownBy(() -> cleanupPlan.addActionIfPresent(() -> "test", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupPlan for fluent chaining")
        void shouldReturnCleanupPlanForFluentChaining() {
            CleanupPlan result = cleanupPlan.addActionIfPresent(() -> "test", v -> {});

            assertThat(result).isSameAs(cleanupPlan);
        }
    }

    @Nested
    @DisplayName("addCleanupActions()")
    class AddCleanupActionsTests {

        @Test
        @DisplayName("should add multiple cleanup actions from list")
        void shouldAddMultipleCleanupActionsFromList() {
            List<CleanupAction> actions = of(() -> {}, () -> {}, () -> {});

            cleanupPlan.addCleanupActions(actions);

            assertThat(cleanupPlan.cleanupActions()).hasSize(3);
            assertThat(cleanupPlan.cleanupActions()).containsExactlyElementsOf(actions);
        }

        @Test
        @DisplayName("should add empty list without errors")
        void shouldAddEmptyListWithoutErrors() {
            cleanupPlan.addCleanupActions(of());

            assertThat(cleanupPlan.cleanupActions()).isEmpty();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list is null")
        void shouldThrowExceptionWhenListIsNull() {
            assertThatThrownBy(() -> cleanupPlan.addCleanupActions(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupActions is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list contains null action")
        void shouldThrowExceptionWhenListContainsNull() {
            List<CleanupAction> actions = new ArrayList<>();
            actions.add(() -> {});
            actions.add(null);

            assertThatThrownBy(() -> cleanupPlan.addCleanupActions(actions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupPlan for fluent chaining")
        void shouldReturnCleanupPlanForFluentChaining() {
            CleanupPlan result = cleanupPlan.addCleanupActions(of(() -> {}));

            assertThat(result).isSameAs(cleanupPlan);
        }
    }

    @Nested
    @DisplayName("cleanupActions()")
    class CleanupActionsTests {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            cleanupPlan.addAction(() -> {});

            List<CleanupAction> actions = cleanupPlan.cleanupActions();

            assertThatThrownBy(() -> actions.add(() -> {})).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when no actions added")
        void shouldReturnEmptyListWhenNoActionsAdded() {
            assertThat(cleanupPlan.cleanupActions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("throwables()")
    class ThrowablesTests {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() throws Throwable {
            cleanupPlan.addAction(() -> {});
            cleanupPlan.run();

            List<Throwable> throwables = cleanupPlan.throwables();

            assertThatThrownBy(() -> throwables.add(new RuntimeException()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when plan has not run")
        void shouldReturnEmptyListWhenPlanHasNotRun() {
            assertThat(cleanupPlan.throwables()).isEmpty();
        }

        @Test
        @DisplayName("should contain null for successful cleanup actions")
        void shouldContainNullForSuccessfulActions() throws Throwable {
            cleanupPlan.addAction(() -> {});
            cleanupPlan.addAction(() -> {});
            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).containsExactly(null, null);
        }

        @Test
        @DisplayName("should contain throwable for failed cleanup actions")
        void shouldContainThrowableForFailedActions() throws Throwable {
            RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupPlan.addAction(() -> {
                throw exception;
            });
            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).containsExactly(exception);
        }
    }

    @Nested
    @DisplayName("run()")
    class RunTests {

        @Test
        @DisplayName("should execute all cleanup actions in order")
        void shouldExecuteAllCleanupActionsInOrder() throws Throwable {
            List<Integer> executionOrder = new ArrayList<>();

            cleanupPlan
                    .addAction(() -> executionOrder.add(1))
                    .addAction(() -> executionOrder.add(2))
                    .addAction(() -> executionOrder.add(3));

            cleanupPlan.run();

            assertThat(executionOrder).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should return the CleanupPlan for fluent chaining")
        void shouldReturnCleanupPlanForFluentChaining() throws Throwable {
            cleanupPlan.addAction(() -> {});

            CleanupPlan result = cleanupPlan.run();

            assertThat(result).isSameAs(cleanupPlan);
        }

        @Test
        @DisplayName("should collect throwables from failed actions")
        void shouldCollectThrowablesFromFailedActions() throws Throwable {
            RuntimeException exception1 = new RuntimeException("error1");
            RuntimeException exception2 = new RuntimeException("error2");

            cleanupPlan
                    .addAction(() -> {
                        throw exception1;
                    })
                    .addAction(() -> {})
                    .addAction(() -> {
                        throw exception2;
                    });

            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).containsExactly(exception1, null, exception2);
        }

        @Test
        @DisplayName("should continue execution even when actions throw exceptions")
        void shouldContinueExecutionWhenActionsThrowExceptions() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);

            cleanupPlan
                    .addAction(() -> {
                        throw new RuntimeException();
                    })
                    .addAction(() -> counter.incrementAndGet())
                    .addAction(() -> {
                        throw new RuntimeException();
                    })
                    .addAction(() -> counter.incrementAndGet());

            cleanupPlan.run();

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw IllegalStateException when run multiple times")
        void shouldThrowExceptionWhenRunMultipleTimes() throws Throwable {
            cleanupPlan.addAction(() -> {});
            cleanupPlan.run();

            assertThatThrownBy(() -> cleanupPlan.run())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("CleanupPlan has already been run");
        }

        @Test
        @DisplayName("should clear throwables before execution")
        void shouldClearThrowablesBeforeExecution() throws Throwable {
            cleanupPlan.addAction(() -> {});
            cleanupPlan.run();

            // Verify throwables exist
            assertThat(cleanupPlan.throwables()).hasSize(1);
        }

        @Test
        @DisplayName("should handle actions that throw checked exceptions")
        void shouldHandleActionsWithCheckedExceptions() throws Throwable {
            Exception checkedException = new Exception("checked exception");

            cleanupPlan.addAction(() -> {
                throw checkedException;
            });
            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).containsExactly(checkedException);
        }

        @Test
        @DisplayName("should handle actions that throw errors")
        void shouldHandleActionsWithErrors() throws Throwable {
            Error error = new Error("error");

            cleanupPlan.addAction(() -> {
                throw error;
            });
            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).containsExactly(error);
        }

        @Test
        @DisplayName("should work with empty cleanup plan")
        void shouldWorkWithEmptyCleanupPlan() throws Throwable {
            cleanupPlan.run();

            assertThat(cleanupPlan.throwables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasThrowables()")
    class HasThrowablesTests {

        @Test
        @DisplayName("should return false when no throwables exist")
        void shouldReturnFalseWhenNoThrowablesExist() throws Throwable {
            cleanupPlan.addAction(() -> {});
            cleanupPlan.run();

            assertThat(cleanupPlan.hasThrowables()).isTrue(); // Contains null entries
        }

        @Test
        @DisplayName("should return true when throwables exist")
        void shouldReturnTrueWhenThrowablesExist() throws Throwable {
            cleanupPlan.addAction(() -> {
                throw new RuntimeException();
            });
            cleanupPlan.run();

            assertThat(cleanupPlan.hasThrowables()).isTrue();
        }

        @Test
        @DisplayName("should return false when plan has not run")
        void shouldReturnFalseWhenPlanHasNotRun() {
            cleanupPlan.addAction(() -> {});

            assertThat(cleanupPlan.hasThrowables()).isFalse();
        }
    }

    @Nested
    @DisplayName("verify()")
    class VerifyTests {

        @Test
        @DisplayName("should not throw when all actions succeed")
        void shouldNotThrowWhenAllActionsSucceed() {
            cleanupPlan.addAction(() -> {}).addAction(() -> {});

            assertThatCode(() -> cleanupPlan.verify()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should automatically run the plan if not already run")
        void shouldAutomaticallyRunPlanIfNotRun() {
            AtomicInteger counter = new AtomicInteger(0);
            cleanupPlan.addAction(() -> counter.incrementAndGet());

            assertThatCode(() -> cleanupPlan.verify()).doesNotThrowAnyException();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw first throwable when single action fails")
        void shouldThrowFirstThrowableWhenSingleActionFails() {
            RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupPlan.addAction(() -> {
                throw exception;
            });

            assertThatThrownBy(() -> cleanupPlan.verify()).isSameAs(exception);
        }

        @Test
        @DisplayName("should throw first throwable and add subsequent throwables as suppressed")
        void shouldAddSubsequentThrowablesAsSuppressed() {
            RuntimeException exception1 = new RuntimeException("error1");
            RuntimeException exception2 = new RuntimeException("error2");
            RuntimeException exception3 = new RuntimeException("error3");

            cleanupPlan
                    .addAction(() -> {
                        throw exception1;
                    })
                    .addAction(() -> {})
                    .addAction(() -> {
                        throw exception2;
                    })
                    .addAction(() -> {
                        throw exception3;
                    });

            assertThatThrownBy(() -> cleanupPlan.verify())
                    .isSameAs(exception1)
                    .hasSuppressedException(exception2)
                    .hasSuppressedException(exception3);
        }

        @Test
        @DisplayName("should handle checked exceptions")
        void shouldHandleCheckedExceptions() {
            Exception checkedException = new Exception("checked exception");
            cleanupPlan.addAction(() -> {
                throw checkedException;
            });

            assertThatThrownBy(() -> cleanupPlan.verify()).isSameAs(checkedException);
        }

        @Test
        @DisplayName("should handle errors")
        void shouldHandleErrors() {
            Error error = new Error("error");
            cleanupPlan.addAction(() -> {
                throw error;
            });

            assertThatThrownBy(() -> cleanupPlan.verify()).isSameAs(error);
        }

        @Test
        @DisplayName("should work when called after run()")
        void shouldWorkWhenCalledAfterRun() throws Throwable {
            RuntimeException exception = new RuntimeException("error");
            cleanupPlan.addAction(() -> {
                throw exception;
            });
            cleanupPlan.run();

            assertThatThrownBy(() -> cleanupPlan.verify()).isSameAs(exception);
        }

        @Test
        @DisplayName("should not throw when plan is empty")
        void shouldNotThrowWhenPlanIsEmpty() {
            assertThatCode(() -> cleanupPlan.verify()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve exception stack traces")
        void shouldPreserveExceptionStackTraces() {
            RuntimeException exception = new RuntimeException("test");
            StackTraceElement[] originalStackTrace = exception.getStackTrace();

            cleanupPlan.addAction(() -> {
                throw exception;
            });

            try {
                cleanupPlan.verify();
                fail("Expected exception to be thrown");
            } catch (Throwable t) {
                assertThat(t.getStackTrace()).isEqualTo(originalStackTrace);
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("should handle complex cleanup scenario with mixed results")
        void shouldHandleComplexCleanupScenario() {
            List<String> executionLog = new ArrayList<>();
            RuntimeException exception = new RuntimeException("cleanup error");

            cleanupPlan
                    .addAction(() -> executionLog.add("action1"))
                    .addActionIfPresent(() -> "resource1", r -> executionLog.add("cleanup:" + r))
                    .addAction(() -> {
                        executionLog.add("action3");
                        throw exception;
                    })
                    .addActionIfPresent(() -> null, r -> executionLog.add("should not execute"))
                    .addAction(() -> executionLog.add("action5"));

            assertThatThrownBy(() -> cleanupPlan.verify()).isSameAs(exception);

            assertThat(executionLog).containsExactly("action1", "cleanup:resource1", "action3", "action5");
        }

        @Test
        @DisplayName("should support try-with-resources pattern simulation")
        void shouldSupportTryWithResourcesPattern() {
            List<String> executionLog = new ArrayList<>();

            try {
                CleanupPlan plan = new CleanupPlan();
                plan.addAction(() -> executionLog.add("close-resource1"));
                plan.addAction(() -> executionLog.add("close-resource2"));

                // Simulate some work
                executionLog.add("work");

                // Cleanup
                plan.verify();
            } catch (Throwable t) {
                fail("Should not throw");
            }

            assertThat(executionLog).containsExactly("work", "close-resource1", "close-resource2");
        }

        @Test
        @DisplayName("should handle nested cleanup plans")
        void shouldHandleNestedCleanupPlans() throws Throwable {
            List<String> executionLog = new ArrayList<>();
            CleanupPlan innerPlan = new CleanupPlan();
            innerPlan.addAction(() -> executionLog.add("inner-cleanup"));

            cleanupPlan
                    .addAction(() -> executionLog.add("outer-cleanup-1"))
                    .addAction(() -> innerPlan.verify())
                    .addAction(() -> executionLog.add("outer-cleanup-2"));

            cleanupPlan.verify();

            assertThat(executionLog).containsExactly("outer-cleanup-1", "inner-cleanup", "outer-cleanup-2");
        }

        @Test
        @DisplayName("should maintain thread safety for single-threaded execution")
        void shouldMaintainThreadSafetyForSingleThreadedExecution() {
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < 100; i++) {
                cleanupPlan.addAction(() -> counter.incrementAndGet());
            }

            assertThatCode(() -> cleanupPlan.verify()).doesNotThrowAnyException();
            assertThat(counter.get()).isEqualTo(100);
        }
    }

    /**
     * Helper method to create an immutable list (Java 8 compatible)
     */
    @SafeVarargs
    private static <T> List<T> of(T... elements) {
        List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return Collections.unmodifiableList(list);
    }
}
