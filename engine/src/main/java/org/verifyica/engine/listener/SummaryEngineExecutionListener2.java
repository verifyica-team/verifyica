/*
 * Copyright (C) Verifyica project authors and contributors
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
import java.util.concurrent.atomic.AtomicLong;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.verifyica.engine.VerifyicaTestEngine;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.AnsiColoredString;
import org.verifyica.engine.common.Counter;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.verifyica.engine.descriptor.ClassTestDescriptor;
import org.verifyica.engine.descriptor.TestDescriptorStatus;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.support.TimestampSupport;

/** Class to implement a SummaryEngineExecutionListener2 */
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedPrivateField", "PMD.EmptyCatchBlock"})
public class SummaryEngineExecutionListener2 implements EngineExecutionListener {

    private static final String SUMMARY_BANNER = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("Verifyica ")
            .append(VerifyicaTestEngine.staticGetVersion())
            .append(" Summary @ ")
            .append(TimestampSupport.now())
            .append(AnsiColor.NONE)
            .build();

    private static final String COMPACT_SUMMARY_BANNER = AnsiColor.TEXT_WHITE_BRIGHT.wrap("Compact Summary");

    private static final String SEPARATOR = AnsiColor.TEXT_WHITE_BRIGHT.wrap(
            "------------------------------------------------------------------------");

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

    private final Map<String, Counter> counterMap;

    private final Stopwatch stopwatch;

    /**
     * Constructor
     */
    public SummaryEngineExecutionListener2() {
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
    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor.isRoot()) {
            stopwatch.reset();
        }

        String type = null;
        String suffix = ".count";

        if (testDescriptor instanceof ClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            type = "argument";
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            type = "method";
        }

        if (!testDescriptor.isRoot() && type != null) {
            counterMap.get(type + suffix).increment();
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        String type = null;
        String suffix = ".skipped";

        if (testDescriptor instanceof ClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            type = "argument";
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            type = "method";
        }

        if (!testDescriptor.isRoot() && type != null) {
            counterMap.get(type + suffix).increment();
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = getDescendantFailureCount(testDescriptor) > 0
                ? TestExecutionResult.Status.FAILED
                : testExecutionResult.getStatus();

        String type = null;
        String suffix = null;

        if (testDescriptor instanceof ClassTestDescriptor) {
            type = "class";
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
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

    /** Method to print the summary */
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
                    ? AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("FAILED")
                    : AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("PASSED");

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

    private static long getDescendantFailureCount(TestDescriptor testDescriptor) {
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

    private static void print(Object object) {
        System.out.print(object);
    }

    /**
     * Method to println an Object
     *
     * @param object object
     */
    private static void println(Object object) {
        System.out.println(object);
    }

    /**
     * Method to get the pad for a list of values
     *
     * @param atomicLongs atomicLongs
     * @return the return pad
     */
    private static int getPad(List<AtomicLong> atomicLongs) {
        return atomicLongs.stream()
                .mapToInt(atomicLong -> String.valueOf(atomicLong.get()).length())
                .max()
                .orElse(0);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Method to get a String that is the value passed to a specific width
     *
     * @param width width
     * @param value value
     * @return the return value
     */
    private static String pad(long width, long value) {
        if (width > 0) {
            return format("%" + width + "d", value);
        } else {
            return String.valueOf(value);
        }
    }
}
