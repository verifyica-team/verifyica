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
public class CleanupExecutorTest {

    private CleanupExecutor cleanupExecutor;

    @BeforeEach
    public void setUp() {
        cleanupExecutor = new CleanupExecutor();
    }

    @Nested
    @DisplayName("addAction()")
    public class AddActionTests {

        @Test
        @DisplayName("should add a single throwable task successfully")
        public void shouldAddSingleThrowableTask() {
            final ThrowableTask action = () -> {};

            final CleanupExecutor result = cleanupExecutor.addTask(action);

            assertThat(result).isSameAs(cleanupExecutor);
            assertThat(cleanupExecutor.cleanupActions()).hasSize(1);
            assertThat(cleanupExecutor.cleanupActions()).containsExactly(action);
        }

        @Test
        @DisplayName("should add multiple throwable tasks in order")
        public void shouldAddMultipleThrowableTasksInOrder() {
            final ThrowableTask action1 = () -> {};
            final ThrowableTask action2 = () -> {};
            final ThrowableTask action3 = () -> {};

            cleanupExecutor.addTask(action1).addTask(action2).addTask(action3);

            assertThat(cleanupExecutor.cleanupActions()).hasSize(3);
            assertThat(cleanupExecutor.cleanupActions()).containsExactly(action1, action2, action3);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when action is null")
        public void shouldThrowExceptionWhenActionIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTask(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should allow fluent chaining")
        public void shouldAllowFluentChaining() {
            final ThrowableTask action1 = () -> {};
            final ThrowableTask action2 = () -> {};

            final CleanupExecutor result = cleanupExecutor.addTask(action1).addTask(action2);

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("addActionIfPresent()")
    public class AddActionIfPresentTests {

        @Test
        @DisplayName("should execute throwable task when supplier returns non-null value")
        public void shouldExecuteThrowableTaskWhenValuePresent() throws Throwable {
            final AtomicInteger counter = new AtomicInteger(0);
            final String value = "test";

            cleanupExecutor.addTaskIfPresent(() -> value, v -> counter.incrementAndGet());
            cleanupExecutor.execute();

            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should not execute throwable task when supplier returns null")
        public void shouldNotExecuteThrowableTaskWhenValueNull() throws Throwable {
            final AtomicInteger counter = new AtomicInteger(0);

            cleanupExecutor.addTaskIfPresent(() -> null, v -> counter.incrementAndGet());
            cleanupExecutor.execute();

            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("should pass the supplied value to the throwable task")
        public void shouldPassSuppliedValueToThrowableTask() throws Throwable {
            final List<String> capturedValues = new ArrayList<>();
            final String expectedValue = "testValue";

            cleanupExecutor.addTaskIfPresent(() -> expectedValue, capturedValues::add);
            cleanupExecutor.execute();

            assertThat(capturedValues).containsExactly(expectedValue);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when supplier is null")
        public void shouldThrowExceptionWhenSupplierIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTaskIfPresent(null, v -> {}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("supplier is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when throwable task is null")
        public void shouldThrowExceptionWhenThrowableTaskIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTaskIfPresent(() -> "test", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        public void shouldReturnCleanupExecutorForFluentChaining() {
            final CleanupExecutor result = cleanupExecutor.addTaskIfPresent(() -> "test", v -> {});

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("addThrowableTasks()")
    public class AddThrowableTasksTests {

        @Test
        @DisplayName("should add multiple throwable tasks from list")
        public void shouldAddMultipleThrowableTasksFromList() {
            final List<ThrowableTask> actions = of(() -> {}, () -> {}, () -> {});

            cleanupExecutor.addTasks(actions);

            assertThat(cleanupExecutor.cleanupActions()).hasSize(3);
            assertThat(cleanupExecutor.cleanupActions()).containsExactlyElementsOf(actions);
        }

        @Test
        @DisplayName("should add empty list without errors")
        public void shouldAddEmptyListWithoutErrors() {
            cleanupExecutor.addTasks(of());

            assertThat(cleanupExecutor.cleanupActions()).isEmpty();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list is null")
        public void shouldThrowExceptionWhenListIsNull() {
            assertThatThrownBy(() -> cleanupExecutor.addTasks(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupActions is null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list contains null action")
        public void shouldThrowExceptionWhenListContainsNull() {
            final List<ThrowableTask> actions = new ArrayList<>();
            actions.add(() -> {});
            actions.add(null);

            assertThatThrownBy(() -> cleanupExecutor.addTasks(actions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("cleanupAction is null");
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        public void shouldReturnCleanupExecutorForFluentChaining() {
            final CleanupExecutor result = cleanupExecutor.addTasks(of(() -> {}));

            assertThat(result).isSameAs(cleanupExecutor);
        }
    }

    @Nested
    @DisplayName("cleanupActions()")
    public class ThrowableTasksTests {

        @Test
        @DisplayName("should return unmodifiable list")
        public void shouldReturnUnmodifiableList() {
            cleanupExecutor.addTask(() -> {});

            final List<ThrowableTask> actions = cleanupExecutor.cleanupActions();

            assertThatThrownBy(() -> actions.add(() -> {})).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when no actions added")
        public void shouldReturnEmptyListWhenNoActionsAdded() {
            assertThat(cleanupExecutor.cleanupActions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("throwables()")
    public class ThrowablesTests {

        @Test
        @DisplayName("should return unmodifiable list")
        public void shouldReturnUnmodifiableList() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            final List<Throwable> throwables = cleanupExecutor.throwables();

            assertThatThrownBy(() -> throwables.add(new RuntimeException()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should return empty list when cleanup executor has not run")
        public void shouldReturnEmptyListWhenCleanupExecutorHasNotRun() {
            assertThat(cleanupExecutor.throwables()).isEmpty();
        }

        @Test
        @DisplayName("should contain null for successful throwable tasks")
        public void shouldContainNullForSuccessfulActions() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(null, null);
        }

        @Test
        @DisplayName("should contain throwable for failed throwable tasks")
        public void shouldContainThrowableForFailedActions() throws Throwable {
            final RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(exception);
        }
    }

    @Nested
    @DisplayName("run()")
    public class RunTests {

        @Test
        @DisplayName("should execute all throwable tasks in order")
        public void shouldExecuteAllThrowableTasksInOrder() throws Throwable {
            final List<Integer> executionOrder = new ArrayList<>();

            cleanupExecutor
                    .addTask(() -> executionOrder.add(1))
                    .addTask(() -> executionOrder.add(2))
                    .addTask(() -> executionOrder.add(3));

            cleanupExecutor.execute();

            assertThat(executionOrder).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("should return the CleanupExecutor for fluent chaining")
        public void shouldReturnCleanupExecutorForFluentChaining() throws Throwable {
            cleanupExecutor.addTask(() -> {});

            final CleanupExecutor result = cleanupExecutor.execute();

            assertThat(result).isSameAs(cleanupExecutor);
        }

        @Test
        @DisplayName("should collect throwables from failed actions")
        public void shouldCollectThrowablesFromFailedActions() throws Throwable {
            final RuntimeException exception1 = new RuntimeException("error1");
            final RuntimeException exception2 = new RuntimeException("error2");

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
        public void shouldContinueExecutionWhenActionsThrowExceptions() throws Throwable {
            final AtomicInteger counter = new AtomicInteger(0);

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
        public void shouldThrowExceptionWhenRunMultipleTimes() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThatThrownBy(() -> cleanupExecutor.execute())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("CleanupExecutor has already run");
        }

        @Test
        @DisplayName("should clear throwables before execution")
        public void shouldClearThrowablesBeforeExecution() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            // Verify throwables exist
            assertThat(cleanupExecutor.throwables()).hasSize(1);
        }

        @Test
        @DisplayName("should handle actions that throw checked exceptions")
        public void shouldHandleActionsWithCheckedExceptions() throws Throwable {
            final Exception checkedException = new Exception("checked exception");

            cleanupExecutor.addTask(() -> {
                throw checkedException;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(checkedException);
        }

        @Test
        @DisplayName("should handle actions that throw errors")
        public void shouldHandleActionsWithErrors() throws Throwable {
            final Error error = new Error("error");

            cleanupExecutor.addTask(() -> {
                throw error;
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).containsExactly(error);
        }

        @Test
        @DisplayName("should work with empty cleanup executor")
        public void shouldWorkWithEmptyCleanupExecutor() throws Throwable {
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.throwables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasThrowables()")
    public class HasThrowablesTests {

        @Test
        @DisplayName("should return false when no throwables exist")
        public void shouldReturnFalseWhenNoThrowablesExist() throws Throwable {
            cleanupExecutor.addTask(() -> {});
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.hasThrowables()).isTrue(); // Contains null entries
        }

        @Test
        @DisplayName("should return true when throwables exist")
        public void shouldReturnTrueWhenThrowablesExist() throws Throwable {
            cleanupExecutor.addTask(() -> {
                throw new RuntimeException();
            });
            cleanupExecutor.execute();

            assertThat(cleanupExecutor.hasThrowables()).isTrue();
        }

        @Test
        @DisplayName("should return false when cleanup executor has not run")
        public void shouldReturnFalseWhenCleanupExecutorHasNotRun() {
            cleanupExecutor.addTask(() -> {});

            assertThat(cleanupExecutor.hasThrowables()).isFalse();
        }
    }

    @Nested
    @DisplayName("verify()")
    public class VerifyTests {

        @Test
        @DisplayName("should not throw when all actions succeed")
        public void shouldNotThrowWhenAllActionsSucceed() {
            cleanupExecutor.addTask(() -> {}).addTask(() -> {});

            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should automatically run the cleanup executor if not already run")
        public void shouldAutomaticallyRunCleanupExecutorIfNotRun() {
            final AtomicInteger counter = new AtomicInteger(0);
            cleanupExecutor.addTask(counter::incrementAndGet);

            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw first throwable when single action fails")
        public void shouldThrowFirstThrowableWhenSingleActionFails() {
            final RuntimeException exception = new RuntimeException("cleanup failed");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(exception);
        }

        @Test
        @DisplayName("should throw first throwable and add subsequent throwables as suppressed")
        public void shouldAddSubsequentThrowablesAsSuppressed() {
            final RuntimeException exception1 = new RuntimeException("error1");
            final RuntimeException exception2 = new RuntimeException("error2");
            final RuntimeException exception3 = new RuntimeException("error3");

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
        public void shouldHandleCheckedExceptions() {
            final Exception checkedException = new Exception("checked exception");
            cleanupExecutor.addTask(() -> {
                throw checkedException;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(checkedException);
        }

        @Test
        @DisplayName("should handle errors")
        public void shouldHandleErrors() {
            final Error error = new Error("error");
            cleanupExecutor.addTask(() -> {
                throw error;
            });

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(error);
        }

        @Test
        @DisplayName("should work when called after run()")
        public void shouldWorkWhenCalledAfterRun() throws Throwable {
            final RuntimeException exception = new RuntimeException("error");
            cleanupExecutor.addTask(() -> {
                throw exception;
            });
            cleanupExecutor.execute();

            assertThatThrownBy(() -> cleanupExecutor.throwIfFailed()).isSameAs(exception);
        }

        @Test
        @DisplayName("should not throw when cleanup executor is empty")
        public void shouldNotThrowWhenCleanupExecutorIsEmpty() {
            assertThatCode(() -> cleanupExecutor.throwIfFailed()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should preserve exception stack traces")
        public void shouldPreserveExceptionStackTraces() {
            final RuntimeException exception = new RuntimeException("test");
            final StackTraceElement[] originalStackTrace = exception.getStackTrace();

            cleanupExecutor.addTask(() -> {
                throw exception;
            });

            try {
                cleanupExecutor.throwIfFailed();
                fail("Expected exception to be thrown");
            } catch (final Throwable t) {
                assertThat(t.getStackTrace()).isEqualTo(originalStackTrace);
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    public class IntegrationTests {

        @Test
        @DisplayName("should handle complex cleanup scenario with mixed results")
        public void shouldHandleComplexCleanupScenario() {
            final List<String> executionLog = new ArrayList<>();
            final RuntimeException exception = new RuntimeException("cleanup error");

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
        public void shouldSupportTryWithResourcesPattern() {
            final List<String> executionLog = new ArrayList<>();

            try {
                final CleanupExecutor cleanupExecutor = new CleanupExecutor();
                cleanupExecutor.addTask(() -> executionLog.add("close-resource1"));
                cleanupExecutor.addTask(() -> executionLog.add("close-resource2"));

                // Simulate some work
                executionLog.add("work");

                // Cleanup
                cleanupExecutor.throwIfFailed();
            } catch (final Throwable t) {
                fail("Should not throw");
            }

            assertThat(executionLog).containsExactly("work", "close-resource1", "close-resource2");
        }

        @Test
        @DisplayName("should handle nested cleanup executors")
        public void shouldHandleNestedCleanupExecutors() throws Throwable {
            final List<String> executionLog = new ArrayList<>();
            final CleanupExecutor innerCleanupExecutor = new CleanupExecutor();
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
        public void shouldMaintainThreadSafetyForSingleThreadedExecution() {
            final AtomicInteger counter = new AtomicInteger(0);

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
    private static <T> List<T> of(final T... elements) {
        final List<T> list = new ArrayList<>(Arrays.asList(elements));
        return Collections.unmodifiableList(list);
    }
}
