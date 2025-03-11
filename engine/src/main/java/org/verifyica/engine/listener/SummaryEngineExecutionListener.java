/*
 * Copyright (C) 2023-present Verifyica project authors and contributors
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

import static org.verifyica.engine.support.TimestampSupport.convertDurationToMillisAndNanoseconds;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.verifyica.engine.VerifyicaTestEngine;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.AnsiColoredString;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.verifyica.engine.descriptor.ClassTestDescriptor;
import org.verifyica.engine.descriptor.TestDescriptorStatus;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.support.TimestampSupport;

/** Class to implement a SummaryEngineExecutionListener */
public class SummaryEngineExecutionListener implements EngineExecutionListener {

    private static final String SUMMARY_BANNER = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("Verifyica ")
            .append(VerifyicaTestEngine.staticGetVersion())
            .append(" Summary (")
            .append(TimestampSupport.now())
            .append(")")
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

    private static final Map<String, String> counterKeyToMessageDisplayStringMap = new HashMap<>();

    static {
        counterKeyToMessageDisplayStringMap.put("test.class.count", "Test classes   ");
        counterKeyToMessageDisplayStringMap.put("test.class.count.successful", "Passed");
        counterKeyToMessageDisplayStringMap.put("test.class.count.failed", "Failed");
        counterKeyToMessageDisplayStringMap.put("test.class.count.skipped", "Skipped");
        counterKeyToMessageDisplayStringMap.put("test.argument.count", "Test arguments ");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.successful", "Passed");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.failed", "Failed");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.skipped", "Skipped");
        counterKeyToMessageDisplayStringMap.put("test.method.count", "Test methods   ");
        counterKeyToMessageDisplayStringMap.put("test.method.count.successful", "Passed");
        counterKeyToMessageDisplayStringMap.put("test.method.count.failed", "Failed");
        counterKeyToMessageDisplayStringMap.put("test.method.count.skipped", "Skipped");
    }

    private final Map<ClassTestDescriptor, TestExecutionResult.Status> classTestDescriptorTestExecutionResultStatusMap;

    private final Map<ClassTestDescriptor, String> classTestDescriptorSkippedMap;

    private final Map<ArgumentTestDescriptor, TestExecutionResult.Status>
            argumentTestDescriptorTestExecutionResultStatusMap;

    private final Map<ArgumentTestDescriptor, String> argumentTestDescriptorSkippedMap;

    private final Map<TestMethodTestDescriptor, TestExecutionResult.Status>
            testMethodTestDescriptorTestExecutionResultStatusMap;

    private final Map<TestMethodTestDescriptor, String> testMethodTestDescriptorSkippedMap;

    private final AtomicLong failureCount;
    private final Map<String, AtomicLong> counterMap;
    private final Stopwatch stopwatch;

    /** Constructor */
    public SummaryEngineExecutionListener() {
        classTestDescriptorTestExecutionResultStatusMap = new ConcurrentHashMap<>();
        classTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        argumentTestDescriptorTestExecutionResultStatusMap = new ConcurrentHashMap<>();
        argumentTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        testMethodTestDescriptorTestExecutionResultStatusMap = new ConcurrentHashMap<>();
        testMethodTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        failureCount = new AtomicLong();
        counterMap = new ConcurrentHashMap<>();
        stopwatch = new Stopwatch();
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor.isRoot()) {
            stopwatch.reset();
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            classTestDescriptorSkippedMap.put((ClassTestDescriptor) testDescriptor, reason);
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            argumentTestDescriptorSkippedMap.put((ArgumentTestDescriptor) testDescriptor, reason);
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            testMethodTestDescriptorSkippedMap.put(
                    (TestMethodTestDescriptor) testDescriptor, reason != null ? reason : "Skipped");
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = getDescendantFailureCount(testDescriptor) > 0
                ? TestExecutionResult.Status.FAILED
                : testExecutionResult.getStatus();

        if (testDescriptor instanceof ClassTestDescriptor) {
            classTestDescriptorTestExecutionResultStatusMap.put((ClassTestDescriptor) testDescriptor, status);
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            argumentTestDescriptorTestExecutionResultStatusMap.put((ArgumentTestDescriptor) testDescriptor, status);
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            testMethodTestDescriptorTestExecutionResultStatusMap.put((TestMethodTestDescriptor) testDescriptor, status);
        }

        if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
            failureCount.incrementAndGet();
        }

        if (testDescriptor.isRoot()) {
            summary();
        }
    }

    /** Method to print the summary */
    private void summary() {
        try {
            stopwatch.stop();

            for (int i = 0; i < classTestDescriptorSkippedMap.size(); i++) {
                String key = "test.class.count";
                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                        .incrementAndGet();
            }

            for (int i = 0; i < argumentTestDescriptorSkippedMap.size(); i++) {
                String key = "test.argument.count";
                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                        .incrementAndGet();
            }

            for (int i = 0; i < testMethodTestDescriptorSkippedMap.size(); i++) {
                String key = "test.method.count";
                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                        .incrementAndGet();
            }

            for (Map.Entry<ClassTestDescriptor, TestExecutionResult.Status> mapEntry :
                    classTestDescriptorTestExecutionResultStatusMap.entrySet()) {
                TestExecutionResult.Status status = mapEntry.getValue();
                String key = "test.class.count";

                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL: {
                        counterMap
                                .computeIfAbsent(key + ".successful", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        counterMap
                                .computeIfAbsent(key + ".failed", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        counterMap
                                .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    default: {
                        // INTENTIONALLY BLANK
                    }
                }
            }

            for (Map.Entry<ArgumentTestDescriptor, TestExecutionResult.Status> mapEntry :
                    argumentTestDescriptorTestExecutionResultStatusMap.entrySet()) {
                TestExecutionResult.Status status = mapEntry.getValue();
                String key = "test.argument.count";

                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL: {
                        counterMap
                                .computeIfAbsent(key + ".successful", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        counterMap
                                .computeIfAbsent(key + ".failed", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        counterMap
                                .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    default: {
                        // INTENTIONALLY BLANK
                    }
                }
            }

            for (Map.Entry<TestMethodTestDescriptor, TestExecutionResult.Status> mapEntry :
                    testMethodTestDescriptorTestExecutionResultStatusMap.entrySet()) {
                TestExecutionResult.Status status = mapEntry.getValue();
                String key = "test.method.count";

                counterMap.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL: {
                        counterMap
                                .computeIfAbsent(key + ".successful", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case FAILED: {
                        counterMap
                                .computeIfAbsent(key + ".failed", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    case ABORTED: {
                        counterMap
                                .computeIfAbsent(key + ".skipped", k -> new AtomicLong())
                                .incrementAndGet();
                        break;
                    }
                    default: {
                        // INTENTIONALLY BLANK
                    }
                }
            }

            println(INFO + SEPARATOR);
            println(INFO + SUMMARY_BANNER);
            println(INFO + SEPARATOR);

            int countPad = getPad(counterMap.entrySet().stream()
                    .filter(mapEntry -> mapEntry.getKey().endsWith(".count"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));

            int successPad = getPad(counterMap.entrySet().stream()
                    .filter(mapEntry -> mapEntry.getKey().endsWith(".successful"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));

            int failedPad = getPad(counterMap.entrySet().stream()
                    .filter(mapEntry -> mapEntry.getKey().endsWith(".failed"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));

            int skipPad = getPad(counterMap.entrySet().stream()
                    .filter(mapEntry -> mapEntry.getKey().endsWith(".skipped"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));

            StringBuilder compactSummary = new StringBuilder(COMPACT_SUMMARY_BANNER);
            compactSummary.append(AnsiColor.TEXT_WHITE_BRIGHT.wrap(" |"));

            String[] keys = {"test.class", "test.argument", "test.method"};
            for (String key : keys) {
                key += ".count";

                long totalCount =
                        counterMap.computeIfAbsent(key, k -> new AtomicLong()).get();

                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder
                        .append(INFO)
                        .append(AnsiColor.TEXT_WHITE_BRIGHT.wrap(counterKeyToMessageDisplayStringMap.get(key)))
                        .append(": ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT.wrap(pad(countPad, totalCount)));

                compactSummary.append(" ").append(AnsiColor.TEXT_WHITE_BRIGHT.wrap(totalCount));

                String[] subKeys = {key + ".successful", key + ".failed", key + ".skipped"};

                for (String subKey : subKeys) {
                    String messageDisplayString = counterKeyToMessageDisplayStringMap.get(subKey);
                    long count = counterMap
                            .computeIfAbsent(subKey, k -> new AtomicLong())
                            .get();
                    String countDisplayString = "";

                    if (subKey.endsWith(".successful")) {
                        messageDisplayString = AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(pad(successPad, count));
                        compactSummary.append(" ").append(AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(count));
                    } else if (subKey.endsWith(".failed")) {
                        messageDisplayString = AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(pad(failedPad, count));
                        compactSummary.append(" ").append(AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(count));
                    } else if (subKey.endsWith(".skipped")) {
                        messageDisplayString = AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(pad(skipPad, count));
                        compactSummary.append(" ").append(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(count));
                    }

                    if (count == 0) {
                        // Revert display message and count display message to TEST_WRITE_BRIGHT
                        messageDisplayString = AnsiColor.stripAnsiEscapeSequences(messageDisplayString);
                        countDisplayString = AnsiColor.stripAnsiEscapeSequences(countDisplayString);

                        messageDisplayString = AnsiColor.TEXT_WHITE_BRIGHT.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_WHITE_BRIGHT.wrap(countDisplayString);
                    }

                    stringBuilder
                            .append(" ")
                            .append(messageDisplayString)
                            .append(AnsiColor.TEXT_WHITE_BOLD)
                            .append(" : ")
                            .append(countDisplayString);
                }

                compactSummary.append(" ").append(AnsiColor.TEXT_WHITE_BRIGHT.wrap("|"));

                println(stringBuilder);
            }

            println(INFO + SEPARATOR);

            String message = failureCount.get() > 0
                    ? AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("TESTS FAILED")
                    : AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("TESTS PASSED");

            println(INFO + message);

            Duration elapsedTime = stopwatch.elapsedTime();

            if (failureCount.get() > 0) {
                compactSummary.append(" ").append(AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("TESTS FAILED"));
            } else {
                compactSummary.append(" ").append(AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("TESTS PASSED"));
            }

            compactSummary.append(AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                    " | " + convertDurationToMillisAndNanoseconds(elapsedTime) + " ms"));

            println(INFO + SEPARATOR);
            println(INFO + AnsiColor.TEXT_WHITE_BRIGHT.wrap(compactSummary));
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

            if (failureCount.get() == 0) {
                println(INFO + SEPARATOR);
            }
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

    /**
     * Method to get a String that is the value passed to a specific width
     *
     * @param width width
     * @param value value
     * @return the return value
     */
    private static String pad(long width, long value) {
        if (width > 0) {
            return String.format("%" + width + "d", value);
        } else {
            return String.valueOf(value);
        }
    }
}
