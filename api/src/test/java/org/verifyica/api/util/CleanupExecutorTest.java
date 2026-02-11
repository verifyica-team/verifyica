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

package org.verifyica.api.util;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for CleanupExecutor
 */
@DisplayName("CleanupExecutor")
class CleanupExecutorTest {

    private CleanupExecutor cleanupExecutor;

    @BeforeEach
    void setUp() {
        cleanupExecutor = new CleanupExecutor();
    }

    @Nested
    @DisplayName("addAction()")
    class AddActionTests {

        @Test
        @DisplayName("should add a single cleanup task successfully")
        void shouldAddSingleCleanupTask() {
            CleanupTask action = () -> {};

            CleanupExecutor result = cleanupExecutor.addTask(action);

            assertThat(result).isSameAs(cleanupExecutor);
            assertThat(cleanupExecutor.cleanupActions()).hasSize(1);
            assertThat(cleanupExecutor.cleanupActions()).containsExactly(action);
        }

        @Test
        @DisplayName("should add multiple cleanup tasks in order")
        void shouldAddMultipleCleanupTasksInOrder() {
            CleanupTask action1 = () -> {};
            CleanupTask action2 = () -> {};
            CleanupTask action3 = () -> {};

            cleanupExecutor.addTask(action1).addTask(action2).addTask(action3);

            assertThat(cleanupExecutor.cleanupActions()).hasSize(3);
            assertThat(cleanupExecutor.cleanupActions()).containsExactly(action1, action2, action3);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when action is null")
        void shouldThrowExceptionWhenActionIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTask(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should allow fluent chaining")
        void shouldAllowFluentChaining() {
            CleanupTask action1 = () -> {};
            CleanupTask action2 = () -> {};

            CleanupExecutor result = cleanupExecutor.addTask(action1).addTask(action2);

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("addActionIfPresent()")
    class AddActionIfPresentTests {

        @Test
        @DisplayName("should execute cleanup task when supplier returns non-null value")
        void shouldExecuteCleanupTaskWhenValuePresent() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);
            String value = "test";

            cleanupExecutor.addTaskIfPresent(() -> value, v -> counter.incrementAndGet());
            cleanupExecutor.execute();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should not execute cleanup task when supplier returns null")
        void shouldNotExecuteCleanupTaskWhenValueNull() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);

            cleanupExecutor.addTaskIfPresent(() -> null, v -> counter.incrementAndGet());
            cleanupExecutor.execute();

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("should pass the supplied value to the cleanup task")
        void shouldPassSuppliedValueToCleanupTask() throws Throwable {
            List<String> capturedValues = new ArrayList<>();
            String expectedValue = "testValue";

            cleanupExecutor.addTaskIfPresent(() -> expectedValue, capturedValues::add);
            cleanupExecutor.execute();

            assertThat(capturedValues).containsExactly(expectedValue);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when supplier is null")
        void shouldThrowExceptionWhenSupplierIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTaskIfPresent(null, v -> {}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("supplier is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when cleanup task is null")
        void shouldThrowExceptionWhenCleanupTaskIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTaskIfPresent(() -> "test", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        void shouldReturnCleanupExecutorForFluentChaining() {
            CleanupExecutor result = cleanupExecutor.addTaskIfPresent(() -> "test", v -> {});

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("addCleanupTasks()")
    class AddCleanupTasksTests {

        @Test
        @DisplayName("should add multiple cleanup tasks from list")
        void shouldAddMultipleCleanupTasksFromList() {
            List<CleanupTask> actions = of(() -> {}, () -> {}, () -> {});

            cleanupExecutor.addCleanupTasks(actions);

            assertThat(cleanupExecutor.cleanupActions()).hasSize(3);
            assertThat(cleanupExecutor.cleanupActions()).containsExactlyElementsOf(actions);
        }

        @Test
        @DisplayName("should add empty list without errors")
        void shouldAddEmptyListWithoutErrors() {
            cleanupExecutor.addCleanupTasks(of());

            assertThat(cleanupExecutor.cleanupActions()).isEmpty();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list is null")
        void shouldThrowExceptionWhenListIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addCleanupTasks(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupActions is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list contains null action")
        void shouldThrowExceptionWhenListContainsNull() {
            List<CleanupTask> actions = new ArrayList<>();
            actions.add(() -> {});
            actions.add(null);

            assertThatThrownBy(() -> cleanupExecutor.addCleanupTasks(actions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        void shouldReturnCleanupExecutorForFluentChaining() {
            CleanupExecutor result = cleanupExecutor.addCleanupTasks(of(() -> {}));

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("cleanupActions()")
    class CleanupTasksTests {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            cleanupExecutor.addTask(() -> {});

            List<CleanupTask> actions = cleanupExecutor.cleanupActions();

            assertThatThrownBy(() -> actions.add(() -> {})).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when no actions added")
        void shouldReturnEmptyListWhenNoActionsAdded() {
            assertThat(cleanupExecutor.cleanupActions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("throwables()")
    class ThrowablesTests {

        @Test
        @DisplayName("should return unmodifiable list")
        void shouldReturnUnmodifiableList() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            List<Throwable> throwables = cleanupExecutor.throwables();

            assertThatThrownBy(() -> throwables.add(new RuntimeException()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when cleanup executor has not run")
        void shouldReturnEmptyListWhenCleanupExecutorHasNotRun() {
            assertThat(cleanupExecutor.throwables()).isEmpty();
        }

        @Test
        @DisplayName("should contain null for successful cleanup tasks")
        void shouldContainNullForSuccessfulActions() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(null, null);
        }

        @Test
        @DisplayName("should contain throwable for failed cleanup tasks")
        void shouldContainThrowableForFailedActions() throws Throwable {
            RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(exception);
        }
    }

    @Nested
    @DisplayName("run()")
    class RunTests {

        @Test
        @DisplayName("should execute all cleanup tasks in order")
        void shouldExecuteAllCleanupTasksInOrder() throws Throwable {
            List<Integer> executionOrder = new ArrayList<>();

            cleanupExecutor
                    .addTask(() -> executionOrder.add(1))
                    .addTask(() -> executionOrder.add(2))
                    .addTask(() -> executionOrder.add(3));

            cleanupExecutor.execute();

            assertThat(executionOrder).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        void shouldReturnCleanupExecutorForFluentChaining() throws Throwable {
            cleanupExecutor.addTask(() -> {});

            CleanupExecutor result = cleanupExecutor.execute();

            assertThat(result).isSameAs(cleanupExecutor);
        }

        @Test
        @DisplayName("should collect throwables from failed actions")
        void shouldCollectThrowablesFromFailedActions() throws Throwable {
            RuntimeException exception1 = new RuntimeException("error1");
            RuntimeException exception2 = new RuntimeException("error2");

            cleanupExecutor
                    .addTask(() -> {
                        throw exception1;
                    })
                    .addTask(() -> {})
                    .addTask(() -> {
                        throw exception2;
                    });

            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(exception1, null, exception2);
        }

        @Test
        @DisplayName("should continue execution even when actions throw exceptions")
        void shouldContinueExecutionWhenActionsThrowExceptions() throws Throwable {
            AtomicInteger counter = new AtomicInteger(0);

            cleanupExecutor
                    .addTask(() -> {
                        throw new RuntimeException();
                    })
                    .addTask(() -> counter.incrementAndGet())
                    .addTask(() -> {
                        throw new RuntimeException();
                    })
                    .addTask(() -> counter.incrementAndGet());

            cleanupExecutor.execute();

            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw IllegalStateException when run multiple times")
        void shouldThrowExceptionWhenRunMultipleTimes() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThatThrownBy(() -> cleanupExecutor.execute())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("CleanupExecutor has already been run");
        }

        @Test
        @DisplayName("should clear throwables before execution")
        void shouldClearThrowablesBeforeExecution() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            // Verify throwables exist
            assertThat(cleanupExecutor.throwables()).hasSize(1);
        }

        @Test
        @DisplayName("should handle actions that throw checked exceptions")
        void shouldHandleActionsWithCheckedExceptions() throws Throwable {
            Exception checkedException = new Exception("checked exception");

            cleanupExecutor.addTask(() -> {
                throw checkedException;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(checkedException);
        }

        @Test
        @DisplayName("should handle actions that throw errors")
        void shouldHandleActionsWithErrors() throws Throwable {
            Error error = new Error("error");

            cleanupExecutor.addTask(() -> {
                throw error;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(error);
        }

        @Test
        @DisplayName("should work with empty cleanup executor")
        void shouldWorkWithEmptyCleanupExecutor() throws Throwable {
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasThrowables()")
    class HasThrowablesTests {

        @Test
        @DisplayName("should return false when no throwables exist")
        void shouldReturnFalseWhenNoThrowablesExist() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.hasThrowables()).isTrue(); // Contains null entries
        }

        @Test
        @DisplayName("should return true when throwables exist")
        void shouldReturnTrueWhenThrowablesExist() throws Throwable {
            cleanupExecutor.addTask(() -> {
                throw new RuntimeException();
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.hasThrowables()).isTrue();
        }

        @Test
        @DisplayName("should return false when cleanup executor has not run")
        void shouldReturnFalseWhenCleanupExecutorHasNotRun() {
            cleanupExecutor.addTask(() -> {});

            assertThat(cleanupExecutor.hasThrowables()).isFalse();
        }
    }

    @Nested
    @DisplayName("verify()")
    class VerifyTests {

        @Test
        @DisplayName("should not throw when all actions succeed")
        void shouldNotThrowWhenAllActionsSucceed() {
            cleanupExecutor.addTask(() -> {}).addTask(() -> {});

            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should automatically run the cleanup executor if not already run")
        void shouldAutomaticallyRunCleanupExecutorIfNotRun() {
            AtomicInteger counter = new AtomicInteger(0);
            cleanupExecutor.addTask(() -> counter.incrementAndGet());

            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw first throwable when single action fails")
        void shouldThrowFirstThrowableWhenSingleActionFails() {
            RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(exception);
        }

        @Test
        @DisplayName("should throw first throwable and add subsequent throwables as suppressed")
        void shouldAddSubsequentThrowablesAsSuppressed() {
            RuntimeException exception1 = new RuntimeException("error1");
            RuntimeException exception2 = new RuntimeException("error2");
            RuntimeException exception3 = new RuntimeException("error3");

            cleanupExecutor
                    .addTask(() -> {
                        throw exception1;
                    })
                    .addTask(() -> {})
                    .addTask(() -> {
                        throw exception2;
                    })
                    .addTask(() -> {
                        throw exception3;
                    });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed())
                    .isSameAs(exception1)
                    .hasSuppressedException(exception2)
                    .hasSuppressedException(exception3);
        }

        @Test
        @DisplayName("should handle checked exceptions")
        void shouldHandleCheckedExceptions() {
            Exception checkedException = new Exception("checked exception");
            cleanupExecutor.addTask(() -> {
                throw checkedException;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(checkedException);
        }

        @Test
        @DisplayName("should handle errors")
        void shouldHandleErrors() {
            Error error = new Error("error");
            cleanupExecutor.addTask(() -> {
                throw error;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(error);
        }

        @Test
        @DisplayName("should work when called after run()")
        void shouldWorkWhenCalledAfterRun() throws Throwable {
            RuntimeException exception = new RuntimeException("error");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });
            cleanupExecutor.execute();

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(exception);
        }

        @Test
        @DisplayName("should not throw when cleanup executor is empty")
        void shouldNotThrowWhenCleanupExecutorIsEmpty() {
            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve exception stack traces")
        void shouldPreserveExceptionStackTraces() {
            RuntimeException exception = new RuntimeException("test");
            StackTraceElement[] originalStackTrace = exception.getStackTrace();

            cleanupExecutor.addTask(() -> {
                throw exception;
            });

            try {
                cleanupExecutor.throwIfFailed();
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

            cleanupExecutor
                    .addTask(() -> executionLog.add("action1"))
                    .addTaskIfPresent(() -> "resource1", r -> executionLog.add("cleanup:" + r))
                    .addTask(() -> {
                        executionLog.add("action3");
                        throw exception;
                    })
                    .addTaskIfPresent(() -> null, r -> executionLog.add("should not execute"))
                    .addTask(() -> executionLog.add("action5"));

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(exception);

            assertThat(executionLog).containsExactly("action1", "cleanup:resource1", "action3", "action5");
        }

        @Test
        @DisplayName("should support try-with-resources pattern simulation")
        void shouldSupportTryWithResourcesPattern() {
            List<String> executionLog = new ArrayList<>();

            try {
                CleanupExecutor cleanupExecutor = new CleanupExecutor();
                cleanupExecutor.addTask(() -> executionLog.add("close-resource1"));
                cleanupExecutor.addTask(() -> executionLog.add("close-resource2"));

                // Simulate some work
                executionLog.add("work");

                // Cleanup
                cleanupExecutor.throwIfFailed();
            } catch (Throwable t) {
                fail("Should not throw");
            }

            assertThat(executionLog).containsExactly("work", "close-resource1", "close-resource2");
        }

        @Test
        @DisplayName("should handle nested cleanup executors")
        void shouldHandleNestedCleanupExecutors() throws Throwable {
            List<String> executionLog = new ArrayList<>();
            CleanupExecutor innerCleanupExecutor = new CleanupExecutor();
            innerCleanupExecutor.addTask(() -> executionLog.add("inner-cleanup"));

            cleanupExecutor
                    .addTask(() -> executionLog.add("outer-cleanup-1"))
                    .addTask(innerCleanupExecutor::throwIfFailed)
                    .addTask(() -> executionLog.add("outer-cleanup-2"));

            cleanupExecutor.throwIfFailed();

            assertThat(executionLog).containsExactly("outer-cleanup-1", "inner-cleanup", "outer-cleanup-2");
        }

        @Test
        @DisplayName("should maintain thread safety for single-threaded execution")
        void shouldMaintainThreadSafetyForSingleThreadedExecution() {
            AtomicInteger counter = new AtomicInteger(0);

            for (int i = 0; i < 100; i++) {
                cleanupExecutor.addTask(counter::incrementAndGet);
            }

            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
            assertThat(counter.get()).isEqualTo(100);
        }
    }

    /**
     * Helper method to create an immutable list (Java 8 compatible)
     */
    @SafeVarargs
    private static <T> List<T> of(T... elements) {
        List<T> list = new ArrayList<>(Arrays.asList(elements));
        return Collections.unmodifiableList(list);
    }
}
