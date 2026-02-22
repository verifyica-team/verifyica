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

package org.verifyica.engine.listener;

import static java.lang.String.format;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.verifyica.engine.VerifyicaTestEngine;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.AnsiColoredString;
import org.verifyica.engine.common.Counter;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.descriptor.TestArgumentTestDescriptor;
import org.verifyica.engine.descriptor.TestClassTestDescriptor;
import org.verifyica.engine.descriptor.TestDescriptorStatus;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.support.TimestampSupport;

/**
 * EngineExecutionListener implementation that provides a summary of test execution results,
 * including counts of passed, failed, and skipped tests, as well as total execution time.
 * The summary is printed to the console after all tests have finished executing.
 */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedPrivateField", "PMD.EmptyCatchBlock"})
public class SummaryEngineExecutionListener implements EngineExecutionListener {

    /**
     * Summary banner displayed after execution.
     */
    private static final String SUMMARY_BANNER = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("Verifyica ")
            .append(VerifyicaTestEngine.staticGetVersion())
            .append(" Finished @ ")
            .append(TimestampSupport.now())
            .append(AnsiColor.NONE)
            .build();

    /**
     * Separator line for console output.
     */
    private static final String SEPARATOR = AnsiColor.TEXT_WHITE_BRIGHT.wrap(
            "------------------------------------------------------------------------");

    /**
     * INFO-level console prefix.
     */
    private static final String INFO = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE)
            .append("[")
            .append(AnsiColor.TEXT_BLUE_BOLD)
            .append("INFO")
            .append(AnsiColor.TEXT_WHITE)
            .append("]")
            .append(AnsiColor.NONE)
            .append(" ")
            .build();

    /**
     * Counter registry for execution statistics.
     */
    private final Map<String, Counter> counterMap;

    /**
     * Stopwatch for tracking total elapsed time.
     */
    private final Stopwatch stopwatch;

    /**
     * Creates a new SummaryEngineExecutionListener and initializes counters and stopwatch.
     */
    public SummaryEngineExecutionListener() {
        counterMap = Collections.synchronizedMap(new LinkedHashMap<>());

        new Counter("class.count", "Test Class Count").register(counterMap);
        new Counter("class.passed", "Test Class Passed Count").register(counterMap);
        new Counter("class.failed", "Test Class Failed Count").register(counterMap);
        new Counter("class.skipped", "Test Class Skipped Count").register(counterMap);
        new Counter("argument.count", "Test Argument Count").register(counterMap);
        new Counter("argument.passed", "Test Argument Passed Count").register(counterMap);
        new Counter("argument.failed", "Test Argument Failed Count").register(counterMap);
        new Counter("argument.skipped", "Test Argument Skipped Count").register(counterMap);
        new Counter("method.count", "Test Method Count").register(counterMap);
        new Counter("method.passed", "Test Method Passed Count").register(counterMap);
        new Counter("method.failed", "Test Method Failed Count").register(counterMap);
        new Counter("method.skipped", "Test Method Skipped Count").register(counterMap);

        stopwatch = new Stopwatch();
    }

    @Override
    public void executionStarted(final TestDescriptor testDescriptor) {
        if (testDescriptor.isRoot()) {
            stopwatch.reset();
        }

        String type = null;
        String suffix = ".count";

        if (testDescriptor instanceof TestClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof TestArgumentTestDescriptor) {
            type = "argument";
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            type = "method";
        }

        if (!testDescriptor.isRoot() && type != null) {
            counterMap.get(type + suffix).increment();
        }
    }

    @Override
    public void executionSkipped(final TestDescriptor testDescriptor, final String reason) {
        String type = null;
        String suffix = ".skipped";

        if (testDescriptor instanceof TestClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof TestArgumentTestDescriptor) {
            type = "argument";
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            type = "method";
        }

        if (!testDescriptor.isRoot() && type != null) {
            counterMap.get(type + suffix).increment();
        }
    }

    @Override
    public void executionFinished(final TestDescriptor testDescriptor, final TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = getDescendantFailureCount(testDescriptor) > 0
                ? TestExecutionResult.Status.FAILED
                : testExecutionResult.getStatus();

        String type = null;
        String suffix = null;

        if (testDescriptor instanceof TestClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof TestArgumentTestDescriptor) {
            type = "argument";
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            type = "method";
        }

        if (status == TestExecutionResult.Status.ABORTED) {
            suffix = ".skipped";
        } else if (status == TestExecutionResult.Status.FAILED) {
            suffix = ".failed";
        } else if (status == TestExecutionResult.Status.SUCCESSFUL) {
            suffix = ".passed";
        }

        if (type != null && suffix != null) {
            counterMap.get(type + suffix).increment();
        }

        if (testDescriptor.isRoot()) {
            summary();
        }
    }

    /**
     * Prints a summary of test execution results, including counts of passed, failed, and skipped tests,
     * as well as total execution time. The summary is printed to the console in a formatted
     * table format, with color-coded status messages. If any failures are detected, the summary will indicate
     * a failed status; otherwise, it will indicate a passed status.
     */
    private void summary() {
        try {
            stopwatch.stop();

            Duration elapsedTime = stopwatch.elapsed();

            long failureCount = 0;

            println(INFO + SEPARATOR);
            println(INFO + SUMMARY_BANNER);
            println(INFO + SEPARATOR);

            List<String> groups = Arrays.asList("class", "argument", "method");
            List<String> statuses = Arrays.asList("count", "passed", "skipped", "failed");

            println(INFO
                    + AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                            format("%-16s | %15s | %15s | %15s |", "Status", "Classes", "Arguments", "Methods")));
            println(INFO
                    + AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                            "-----------------+-----------------+-----------------+-----------------+"));

            NumberFormat numberFormat = NumberFormat.getIntegerInstance();

            for (String status : statuses) {
                print(INFO + AnsiColor.TEXT_WHITE_BRIGHT.wrap(format("%-16s |", capitalize(status))));

                for (String group : groups) {
                    String key = format("%s.%s", group, status);
                    Counter counter = counterMap.get(key);
                    String count = (counter != null) ? numberFormat.format(counter.count()) : "-";
                    print(AnsiColor.TEXT_WHITE_BRIGHT.wrap(format(" %15s |", count)));

                    if ("failed".equals(status) && counter != null) {
                        failureCount += counter.count();
                    }
                }

                println("");
            }

            println(INFO + SEPARATOR);

            String message = failureCount > 0
                    ? AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("STATUS FAILED")
                    : AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("STATUS PASSED");

            println(INFO + message);

            println(INFO + SEPARATOR);

            println(new AnsiColoredString()
                    .append(INFO)
                    .append(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Total time  : ")
                    .append(TimestampSupport.toHumanReadable(TimestampSupport.Format.SHORT, elapsedTime.toNanos()))
                    .append(" (")
                    .append(elapsedTime.toNanos() / 1e+6D)
                    .append(" ms)")
                    .append(AnsiColor.NONE));

            println(new AnsiColoredString()
                    .append(INFO)
                    .append(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Finished at : ")
                    .append(TimestampSupport.now())
                    .append(AnsiColor.NONE));
        } catch (Throwable t) {
            StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
        }
    }

    /**
     * Counts failed descendant test descriptors.
     *
     * @param testDescriptor root descriptor to inspect
     * @return number of failed descendants
     */
    private static long getDescendantFailureCount(final TestDescriptor testDescriptor) {
        Set<? extends TestDescriptor> descendants = testDescriptor.getDescendants();

        return descendants.stream()
                .filter(descendant -> descendant instanceof TestableTestDescriptor)
                .map(descendant -> (TestableTestDescriptor) descendant)
                .filter(testableTestDescriptor -> {
                    TestDescriptorStatus testDescriptorStatus = testableTestDescriptor.getTestDescriptorStatus();
                    return testDescriptorStatus != null && testDescriptorStatus.isFailure();
                })
                .count();
    }

    /**
     * Prints an Object to the console without a newline.
     *
     * @param object object
     */
    private static void print(final Object object) {
        System.out.print(object);
    }

    /**
     * Prints an Object to the console with a newline.
     *
     * @param object object
     */
    private static void println(final Object object) {
        System.out.println(object);
    }

    /**
     * Capitalizes the first character of a string.
     *
     * @param s string to capitalize
     * @return capitalized string
     */
    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
