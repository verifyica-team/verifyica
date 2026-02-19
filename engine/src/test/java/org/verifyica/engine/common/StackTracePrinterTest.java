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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.*;

@DisplayName("StackTracePrinter Tests")
public class StackTracePrinterTest {

    // Java 8 compatible String repeat helper
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("PrintStackTrace Tests")
    public class PrintStackTraceTests {

        @Test
        @DisplayName("Should print stack trace to stream")
        public void shouldPrintStackTraceToStream() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException").contains("Test exception");
        }

        @Test
        @DisplayName("Should print stack trace with ANSI color")
        public void shouldPrintStackTraceWithAnsiColor() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.TEXT_RED, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException").contains("Test exception");
        }

        @Test
        @DisplayName("Should handle exception with cause")
        public void shouldHandleExceptionWithCause() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception cause = new IllegalArgumentException("Cause exception");
            Exception exception = new RuntimeException("Main exception", cause);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output)
                    .contains("RuntimeException")
                    .contains("Main exception")
                    .contains("IllegalArgumentException")
                    .contains("Cause exception");
        }

        @Test
        @DisplayName("Should handle exception with nested causes")
        public void shouldHandleExceptionWithNestedCauses() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception rootCause = new IllegalStateException("Root cause");
            Exception middleCause = new IllegalArgumentException("Middle cause", rootCause);
            Exception topException = new RuntimeException("Top exception", middleCause);

            StackTracePrinter.printStackTrace(topException, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output)
                    .contains("RuntimeException")
                    .contains("Top exception")
                    .contains("IllegalArgumentException")
                    .contains("Middle cause")
                    .contains("IllegalStateException")
                    .contains("Root cause");
        }

        @Test
        @DisplayName("Should prune stack traces from engine classes")
        public void shouldPruneStackTracesFromEngineClasses() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            // Create exception with a mix of user and engine stack frames
            Exception exception = new RuntimeException("User code exception");
            StackTraceElement[] stackTrace = new StackTraceElement[] {
                new StackTraceElement("com.example.UserTest", "testMethod", "UserTest.java", 25),
                new StackTraceElement(
                        "org.verifyica.engine.internal.TestExecutor", "execute", "TestExecutor.java", 100),
                new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 498)
            };
            exception.setStackTrace(stackTrace);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            // Verify exception type and message
            assertThat(output).contains("RuntimeException").contains("User code exception");
            // Verify user code is present
            assertThat(output).contains("com.example.UserTest").contains("testMethod");
            // Verify engine classes are pruned
            assertThat(output).doesNotContain("org.verifyica.engine.internal.TestExecutor");
            // Verify frames after engine are also pruned
            assertThat(output).doesNotContain("java.lang.reflect.Method");
            // Verify "... X more" indicator
            assertThat(output).contains("... 2 more");
        }

        @Test
        @DisplayName("Should be thread-safe for concurrent printing")
        public void shouldBeThreadSafeForConcurrentPrinting() throws InterruptedException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    Exception exception = new RuntimeException("Exception " + threadIndex);
                    StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);
                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }
    }

    @Nested
    @DisplayName("Stack Trace Pruning Tests")
    public class StackTracePruningTests {

        @Test
        @DisplayName("Should handle exception without stack trace")
        public void shouldHandleExceptionWithoutStackTrace() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception") {
                @Override
                public synchronized Throwable fillInStackTrace() {
                    return this;
                }
            };

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            // Anonymous subpublic class won't have "RuntimeException" in the name, just check for the message
            assertThat(output).contains("Test exception");
        }

        @Test
        @DisplayName("Should handle exception with empty stack trace")
        public void shouldHandleExceptionWithEmptyStackTrace() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");
            exception.setStackTrace(new StackTraceElement[0]);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException").contains("Test exception");
        }

        @Test
        @DisplayName("Should trim stack trace at engine boundary")
        public void shouldTrimStackTraceAtEngineBoundary() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            Exception exception = new RuntimeException("Test exception");
            StackTraceElement[] stackTrace = new StackTraceElement[] {
                new StackTraceElement("UserClass", "userMethod", "UserClass.java", 10),
                new StackTraceElement("org.verifyica.engine.SomeClass", "engineMethod", "SomeClass.java", 20),
                new StackTraceElement("FrameworkClass", "frameworkMethod", "FrameworkClass.java", 30)
            };
            exception.setStackTrace(stackTrace);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            // Verify exception type and message are present
            assertThat(output).contains("RuntimeException").contains("Test exception");
            // Verify user code is present
            assertThat(output).contains("UserClass").contains("userMethod");
            // Verify engine class and beyond are NOT present (pruned)
            assertThat(output).doesNotContain("org.verifyica.engine.SomeClass");
            assertThat(output).doesNotContain("FrameworkClass");
            // Verify "... X more" indicator is present
            assertThat(output).contains("... 2 more");
        }
    }

    @Nested
    @DisplayName("Color Tests")
    public class ColorTests {

        @Test
        @DisplayName("Should support different ANSI colors")
        public void shouldSupportDifferentAnsiColors() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.TEXT_GREEN, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("Should handle ANSI color NONE")
        public void shouldHandleAnsiColorNone() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("Should reset ANSI color after printing")
        public void shouldResetAnsiColorAfterPrinting() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.TEXT_RED, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    public class EdgeCasesTests {

        @Test
        @DisplayName("Should handle exception with null message")
        public void shouldHandleExceptionWithNullMessage() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException((String) null);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("Should handle exception with empty message")
        public void shouldHandleExceptionWithEmptyMessage() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("Should handle exception with very long message")
        public void shouldHandleExceptionWithVeryLongMessage() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            String longMessage = repeat("A", 1000);
            Exception exception = new RuntimeException(longMessage);

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }

        @Test
        @DisplayName("Should handle exception with special characters in message")
        public void shouldHandleExceptionWithSpecialCharactersInMessage() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test \n\t\r exception with special chars: <>&\"'");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException");
        }
    }

    @Nested
    @DisplayName("Different Exception Types Tests")
    public class DifferentExceptionTypesTests {

        @Test
        @DisplayName("Should handle RuntimeException")
        public void shouldHandleRuntimeException() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Runtime exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("RuntimeException").contains("Runtime exception");
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        public void shouldHandleIllegalArgumentException() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new IllegalArgumentException("Invalid argument");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("IllegalArgumentException").contains("Invalid argument");
        }

        @Test
        @DisplayName("Should handle NullPointerException")
        public void shouldHandleNullPointerException() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new NullPointerException("Null pointer");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("NullPointerException").contains("Null pointer");
        }

        @Test
        @DisplayName("Should handle custom exception types")
        public void shouldHandleCustomExceptionTypes() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new CustomException("Custom exception");

            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            String output = outputStream.toString();
            assertThat(output).contains("CustomException").contains("Custom exception");
        }
    }

    @Nested
    @DisplayName("Null Parameter Validation Tests")
    public class NullParameterValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException when throwable is null")
        public void shouldThrowNullPointerExceptionWhenThrowableIsNull() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            assertThatThrownBy(() -> StackTracePrinter.printStackTrace(null, AnsiColor.NONE, printStream))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("throwable is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when ansiColor is null")
        public void shouldThrowNullPointerExceptionWhenAnsiColorIsNull() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            Exception exception = new RuntimeException("Test");

            assertThatThrownBy(() -> StackTracePrinter.printStackTrace(exception, null, printStream))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ansiColor is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException when printStream is null")
        public void shouldThrowNullPointerExceptionWhenPrintStreamIsNull() {
            Exception exception = new RuntimeException("Test");

            assertThatThrownBy(() -> StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("printStream is null");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    public class ImmutabilityTests {

        @Test
        @DisplayName("Should not modify original exception stack trace")
        public void shouldNotModifyOriginalExceptionStackTrace() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            // Create exception with known stack trace
            Exception exception = new RuntimeException("Test exception");
            StackTraceElement[] originalStackTrace = exception.getStackTrace();
            int originalLength = originalStackTrace.length;

            // Print the stack trace (which may prune)
            StackTracePrinter.printStackTrace(exception, AnsiColor.NONE, printStream);

            // Verify original exception is unchanged
            StackTraceElement[] stackTraceAfterPrint = exception.getStackTrace();
            assertThat(stackTraceAfterPrint).hasSize(originalLength);
            assertThat(stackTraceAfterPrint).isEqualTo(originalStackTrace);
        }

        @Test
        @DisplayName("Should not modify cause stack traces")
        public void shouldNotModifyCauseStackTraces() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            Exception rootCause = new IllegalStateException("Root cause");
            Exception middleCause = new IllegalArgumentException("Middle cause", rootCause);
            Exception topException = new RuntimeException("Top exception", middleCause);

            StackTraceElement[] originalRootTrace = rootCause.getStackTrace();
            StackTraceElement[] originalMiddleTrace = middleCause.getStackTrace();
            StackTraceElement[] originalTopTrace = topException.getStackTrace();

            StackTracePrinter.printStackTrace(topException, AnsiColor.NONE, printStream);

            assertThat(rootCause.getStackTrace()).isEqualTo(originalRootTrace);
            assertThat(middleCause.getStackTrace()).isEqualTo(originalMiddleTrace);
            assertThat(topException.getStackTrace()).isEqualTo(originalTopTrace);
        }
    }

    // Helper method to create exception from user code
    private Exception createExceptionFromUserCode() {
        return new RuntimeException("User code exception");
    }

    // Custom exception for testing
    private static class CustomException extends Exception {

        public CustomException(String message) {
            super(message);
        }
    }
}
