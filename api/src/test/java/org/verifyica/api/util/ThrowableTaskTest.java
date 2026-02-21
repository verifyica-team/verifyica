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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive test suite for {@link ThrowableTask}.
 *
 * <p>Tests validate the functional interface contract, exception handling,
 * and usage patterns including lambda expressions and method references.
 */
@DisplayName("ThrowableTask")
public class ThrowableTaskTest {

    @Test
    @DisplayName("Should execute lambda expression successfully")
    public void shouldExecuteLambdaExpressionSuccessfully() throws Throwable {
        final AtomicBoolean executed = new AtomicBoolean(false);
        final ThrowableTask task = () -> executed.set(true);

        task.run();

        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("Should execute method reference successfully")
    public void shouldExecuteMethodReferenceSuccessfully() throws Throwable {
        final TaskExecutor executor = new TaskExecutor();
        final ThrowableTask task = executor::execute;

        task.run();

        assertThat(executor.wasExecuted()).isTrue();
    }

    @Test
    @DisplayName("Should execute anonymous inner class successfully")
    public void shouldExecuteAnonymousInnerClassSuccessfully() throws Throwable {
        final AtomicBoolean executed = new AtomicBoolean(false);
        final ThrowableTask task = new ThrowableTask() {
            @Override
            public void run() throws Throwable {
                executed.set(true);
            }
        };

        task.run();

        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("Should throw runtime exception")
    public void shouldThrowRuntimeException() {
        final RuntimeException expectedException = new RuntimeException("runtime error");
        final ThrowableTask task = () -> {
            throw expectedException;
        };

        assertThatThrownBy(() -> task.run()).isSameAs(expectedException);
    }

    @Test
    @DisplayName("Should throw checked exception")
    public void shouldThrowCheckedException() {
        final IOException expectedException = new IOException("io error");
        final ThrowableTask task = () -> {
            throw expectedException;
        };

        assertThatThrownBy(() -> task.run()).isSameAs(expectedException);
    }

    @Test
    @DisplayName("Should throw error")
    public void shouldThrowError() {
        final Error expectedError = new Error("fatal error");
        final ThrowableTask task = () -> {
            throw expectedError;
        };

        assertThatThrownBy(() -> task.run()).isSameAs(expectedError);
    }

    @Test
    @DisplayName("Should throw null pointer exception")
    public void shouldThrowNullPointerException() {
        final ThrowableTask task = () -> {
            final String nullString = null;
            nullString.length(); // This will throw NPE
        };

        assertThatThrownBy(() -> task.run()).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should throw custom throwable subclass")
    public void shouldThrowCustomThrowableSubclass() {
        final CustomThrowable expectedException = new CustomThrowable("custom error");
        final ThrowableTask task = () -> {
            throw expectedException;
        };

        assertThatThrownBy(() -> task.run()).isSameAs(expectedException);
    }

    /* ============================================================
     * Functional Interface Usage Tests
     * ============================================================ */

    @Test
    @DisplayName("Should work with functional utility method")
    public void shouldWorkWithFunctionalUtilityMethod() throws Throwable {
        final AtomicReference<String> capturedMessage = new AtomicReference<>();
        final ThrowableTask task = createLoggingTask(capturedMessage, "test message");

        task.run();

        assertThat(capturedMessage.get()).isEqualTo("test message");
    }

    @Test
    @DisplayName("Should support chaining through wrapper")
    public void shouldSupportChainingThroughWrapper() throws Throwable {
        final StringBuilder executionOrder = new StringBuilder();

        final ThrowableTask task1 = () -> executionOrder.append("A");
        final ThrowableTask task2 = () -> executionOrder.append("B");
        final ThrowableTask task3 = () -> executionOrder.append("C");

        final ThrowableTask chained = () -> {
            task1.run();
            task2.run();
            task3.run();
        };

        chained.run();

        assertThat(executionOrder.toString()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("Should capture exception in chaining")
    public void shouldCaptureExceptionInChaining() {
        final RuntimeException expectedException = new RuntimeException("error in chain");
        final AtomicBoolean firstTaskExecuted = new AtomicBoolean(false);

        final ThrowableTask task1 = () -> firstTaskExecuted.set(true);
        final ThrowableTask task2 = () -> {
            throw expectedException;
        };

        final ThrowableTask chained = () -> {
            task1.run();
            task2.run();
        };

        assertThatThrownBy(() -> chained.run()).isSameAs(expectedException);
        assertThat(firstTaskExecuted.get()).isTrue();
    }

    /* ============================================================
     * Edge Case Tests
     * ============================================================ */

    @Test
    @DisplayName("Should handle empty lambda body")
    public void shouldHandleEmptyLambdaBody() throws Throwable {
        final ThrowableTask emptyTask = () -> {};

        assertThatCode(() -> emptyTask.run()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle lambda with complex logic")
    public void shouldHandleLambdaWithComplexLogic() throws Throwable {
        final int[] result = new int[1];
        final ThrowableTask complexTask = () -> {
            int sum = 0;
            for (int i = 1; i <= 10; i++) {
                sum += i;
            }
            result[0] = sum;
        };

        complexTask.run();

        assertThat(result[0]).isEqualTo(55);
    }

    @Test
    @DisplayName("Should handle recursive lambda")
    public void shouldHandleRecursiveLambda() throws Throwable {
        final int[] result = new int[1];
        final AtomicReference<ThrowableTask> recursiveTaskRef = new AtomicReference<>();

        final ThrowableTask recursiveTask = () -> {
            if (result[0] < 5) {
                result[0]++;
                recursiveTaskRef.get().run();
            }
        };
        recursiveTaskRef.set(recursiveTask);

        recursiveTask.run();

        assertThat(result[0]).isEqualTo(5);
    }

    @Test
    @DisplayName("Should preserve stack trace when throwing")
    public void shouldPreserveStackTraceWhenThrowing() {
        final ThrowableTask task = () -> {
            throw new RuntimeException("error with stack trace");
        };

        try {
            task.run();
            fail("Expected exception to be thrown");
        } catch (final Throwable t) {
            assertThat(t.getStackTrace()).isNotEmpty();
            assertThat(t.getMessage()).isEqualTo("error with stack trace");
        }
    }

    /* ============================================================
     * Functional Interface Contract Tests
     * ============================================================ */

    @Test
    @DisplayName("Should be assignable from lambda")
    public void shouldBeAssignableFromLambda() {
        final ThrowableTask task = () -> {};
        assertThat(task).isNotNull();
    }

    @Test
    @DisplayName("Should be assignable from method reference")
    public void shouldBeAssignableFromMethodReference() {
        final TaskExecutor executor = new TaskExecutor();
        final ThrowableTask task = executor::execute;
        assertThat(task).isNotNull();
    }

    @Test
    @DisplayName("Should have functional interface annotation")
    public void shouldHaveFunctionalInterfaceAnnotation() {
        assertThat(ThrowableTask.class.isAnnotationPresent(FunctionalInterface.class))
                .isTrue();
    }

    @Test
    @DisplayName("Should have exactly one abstract method")
    public void shouldHaveExactlyOneAbstractMethod() {
        final long abstractMethodCount = java.util.Arrays.stream(ThrowableTask.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        assertThat(abstractMethodCount).isEqualTo(1);
    }

    /* ============================================================
     * Integration Tests
     * ============================================================ */

    @Test
    @DisplayName("Should work in collection of tasks")
    public void shouldWorkInCollectionOfTasks() throws Throwable {
        final java.util.List<ThrowableTask> tasks = new java.util.ArrayList<>();
        final java.util.List<String> executionLog = new java.util.ArrayList<>();

        tasks.add(() -> executionLog.add("task1"));
        tasks.add(() -> executionLog.add("task2"));
        tasks.add(() -> executionLog.add("task3"));

        for (final ThrowableTask task : tasks) {
            task.run();
        }

        assertThat(executionLog).containsExactly("task1", "task2", "task3");
    }

    @Test
    @DisplayName("Should work with try-catch block inside lambda")
    public void shouldWorkWithTryCatchBlockInsideLambda() throws Throwable {
        final java.util.List<String> log = new java.util.ArrayList<>();

        final ThrowableTask task = () -> {
            try {
                throw new IOException("inner exception");
            } catch (final IOException e) {
                log.add("caught: " + e.getMessage());
            }
        };

        task.run();

        assertThat(log).containsExactly("caught: inner exception");
    }

    @Test
    @DisplayName("Should work with finally block inside lambda")
    public void shouldWorkWithFinallyBlockInsideLambda() throws Throwable {
        final java.util.List<String> log = new java.util.ArrayList<>();

        final ThrowableTask task = () -> {
            try {
                log.add("try");
            } finally {
                log.add("finally");
            }
        };

        task.run();

        assertThat(log).containsExactly("try", "finally");
    }

    /* ============================================================
     * Helper Methods and Classes
     * ============================================================ */

    private static ThrowableTask createLoggingTask(final AtomicReference<String> ref, final String message) {
        return () -> ref.set(message);
    }

    /**
     * Helper class for testing method references
     */
    private static class TaskExecutor {
        private final AtomicBoolean executed = new AtomicBoolean(false);

        public void execute() throws Throwable {
            executed.set(true);
        }

        public boolean wasExecuted() {
            return executed.get();
        }
    }

    /**
     * Custom throwable class for testing
     */
    private static class CustomThrowable extends Throwable {
        private static final long serialVersionUID = 1L;

        CustomThrowable(final String message) {
            super(message);
        }
    }
}
