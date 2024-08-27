/*
 * Copyright (C) 2023 The Verifyica project authors
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

package org.antublue.verifyica.engine.listener;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.antublue.verifyica.engine.VerifyicaTestEngine;
import org.antublue.verifyica.engine.common.AnsiColor;
import org.antublue.verifyica.engine.common.StopWatch;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.support.HumanReadableTimeSupport;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a SummaryEngineExecutionListener */
public class SummaryEngineExecutionListener implements EngineExecutionListener {

    private static final String BANNER =
            new AnsiColor.StringBuilder()
                    .append(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Verifyica ")
                    .append(VerifyicaTestEngine.staticGetVersion())
                    .append(AnsiColor.NONE)
                    .build();

    private static final String SUMMARY_BANNER =
            BANNER + AnsiColor.TEXT_WHITE_BRIGHT.wrap(" Summary");

    private static final String SEPARATOR =
            AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                    "------------------------------------------------------------------------");

    private static final String INFO =
            new AnsiColor.StringBuilder()
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
        counterKeyToMessageDisplayStringMap.put("test.class.count.aborted", "Aborted");
        counterKeyToMessageDisplayStringMap.put("test.class.count.skipped", "Skipped");
        counterKeyToMessageDisplayStringMap.put("test.argument.count", "Test arguments ");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.successful", "Passed");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.failed", "Failed");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.aborted", "Aborted");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.skipped", "Skipped");
        counterKeyToMessageDisplayStringMap.put("test.method.count", "Test methods   ");
        counterKeyToMessageDisplayStringMap.put("test.method.count.successful", "Passed");
        counterKeyToMessageDisplayStringMap.put("test.method.count.failed", "Failed");
        counterKeyToMessageDisplayStringMap.put("test.method.count.aborted", "Aborted");
        counterKeyToMessageDisplayStringMap.put("test.method.count.skipped", "Skipped");
    }

    private boolean hasTests;
    private AtomicLong failureCount;

    private final Map<ClassTestDescriptor, TestExecutionResult>
            classTestDescriptorTestExecutionResultMap;

    private final Map<ClassTestDescriptor, String> classTestDescriptorSkippedMap;

    private final Map<ArgumentTestDescriptor, TestExecutionResult>
            argumentTestDescriptorTestExecutionResultMap;

    private final Map<ArgumentTestDescriptor, String> argumentTestDescriptorSkippedMap;

    private final Map<TestMethodTestDescriptor, TestExecutionResult>
            methodTestDescriptorTestExecutionResultMap;

    private final Map<TestMethodTestDescriptor, String> methodTestDescriptorSkippedMap;

    private final Map<String, AtomicLong> counterMap;
    private final StopWatch stopWatch;

    /** Constructor */
    public SummaryEngineExecutionListener() {
        failureCount = new AtomicLong();

        classTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        classTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        argumentTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        argumentTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        methodTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        methodTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        counterMap = new ConcurrentHashMap<>();
        stopWatch = new StopWatch();

        println(INFO + SEPARATOR);
        println(INFO + BANNER);
        println(INFO + SEPARATOR);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor.isRoot()) {
            stopWatch.reset();
        }

        if (!testDescriptor.isRoot()) {
            hasTests = true;
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            classTestDescriptorSkippedMap.put((ClassTestDescriptor) testDescriptor, reason);
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            argumentTestDescriptorSkippedMap.put((ArgumentTestDescriptor) testDescriptor, reason);
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            methodTestDescriptorSkippedMap.put((TestMethodTestDescriptor) testDescriptor, reason);
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            classTestDescriptorTestExecutionResultMap.put(
                    (ClassTestDescriptor) testDescriptor, testExecutionResult);
        } else if (testDescriptor instanceof ArgumentTestDescriptor) {
            argumentTestDescriptorTestExecutionResultMap.put(
                    (ArgumentTestDescriptor) testDescriptor, testExecutionResult);
        } else if (testDescriptor instanceof TestMethodTestDescriptor) {
            methodTestDescriptorTestExecutionResultMap.put(
                    (TestMethodTestDescriptor) testDescriptor, testExecutionResult);
        }

        if (!testDescriptor.isRoot()
                && testExecutionResult.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
            failureCount.incrementAndGet();
        }

        if (testDescriptor instanceof StatusEngineDescriptor) {
            ((StatusEngineDescriptor) testDescriptor).setFailureCount(failureCount.get());
            summary();
        }
    }

    /** Method to print the summary */
    private void summary() {
        try {
            stopWatch.stop();

            for (int i = 0; i < classTestDescriptorSkippedMap.size(); i++) {
                String key = "test.class.count";
                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", o -> new AtomicLong())
                        .incrementAndGet();
            }

            for (int i = 0; i < argumentTestDescriptorSkippedMap.size(); i++) {
                String key = "test.argument.count";
                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", o -> new AtomicLong())
                        .incrementAndGet();
            }

            for (int i = 0; i < methodTestDescriptorSkippedMap.size(); i++) {
                String key = "test.method.count";
                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                counterMap
                        .computeIfAbsent(key + ".skipped", o -> new AtomicLong())
                        .incrementAndGet();
            }

            for (Map.Entry<ClassTestDescriptor, TestExecutionResult> mapEntry :
                    classTestDescriptorTestExecutionResultMap.entrySet()) {
                TestExecutionResult testExecutionResult = mapEntry.getValue();
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                String key = "test.class.count";

                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".successful", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case FAILED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".failed", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case ABORTED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".aborted", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    default:
                        {
                            // INTENTIONALLY BLANK
                        }
                }
            }

            for (Map.Entry<ArgumentTestDescriptor, TestExecutionResult> mapEntry :
                    argumentTestDescriptorTestExecutionResultMap.entrySet()) {
                TestExecutionResult testExecutionResult = mapEntry.getValue();
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                String key = "test.argument.count";

                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".successful", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case FAILED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".failed", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case ABORTED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".aborted", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    default:
                        {
                            // INTENTIONALLY BLANK
                        }
                }
            }

            for (Map.Entry<TestMethodTestDescriptor, TestExecutionResult> mapEntry :
                    methodTestDescriptorTestExecutionResultMap.entrySet()) {
                TestExecutionResult testExecutionResult = mapEntry.getValue();
                TestExecutionResult.Status status = testExecutionResult.getStatus();
                String key = "test.method.count";

                counterMap.computeIfAbsent(key, o -> new AtomicLong()).incrementAndGet();

                switch (status) {
                    case SUCCESSFUL:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".successful", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case FAILED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".failed", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    case ABORTED:
                        {
                            counterMap
                                    .computeIfAbsent(key + ".aborted", o -> new AtomicLong())
                                    .incrementAndGet();
                            break;
                        }
                    default:
                        {
                            // INTENTIONALLY BLANK
                        }
                }
            }

            if (hasTests) {
                println(INFO + SEPARATOR);
                println(INFO + SUMMARY_BANNER);
                println(INFO + SEPARATOR);
            }

            int countPad =
                    getPad(
                            counterMap.entrySet().stream()
                                    .filter(mapEntry -> mapEntry.getKey().endsWith(".count"))
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList()));

            int successPad =
                    getPad(
                            counterMap.entrySet().stream()
                                    .filter(mapEntry -> mapEntry.getKey().endsWith(".successful"))
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList()));

            int failedPad =
                    getPad(
                            counterMap.entrySet().stream()
                                    .filter(mapEntry -> mapEntry.getKey().endsWith(".failed"))
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList()));

            int abortedPad =
                    getPad(
                            counterMap.entrySet().stream()
                                    .filter(mapEntry -> mapEntry.getKey().endsWith(".aborted"))
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList()));

            int skipPad =
                    getPad(
                            counterMap.entrySet().stream()
                                    .filter(mapEntry -> mapEntry.getKey().endsWith(".skipped"))
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList()));

            String[] keys = {"test.class", "test.argument", "test.method"};
            for (String key : keys) {
                StringBuilder stringBuilder = new StringBuilder();

                key += ".count";
                stringBuilder
                        .append(INFO)
                        .append(
                                AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                                        counterKeyToMessageDisplayStringMap.get(key)))
                        .append(": ")
                        .append(
                                AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                                        pad(
                                                counterMap
                                                        .computeIfAbsent(key, o -> new AtomicLong())
                                                        .get(),
                                                countPad)));

                String[] subKeys = {
                    key + ".successful", key + ".failed", key + ".aborted", key + ".skipped"
                };

                for (String subKey : subKeys) {
                    String messageDisplayString = counterKeyToMessageDisplayStringMap.get(subKey);
                    long count = counterMap.computeIfAbsent(subKey, o -> new AtomicLong()).get();
                    String countDisplayString;

                    if (subKey.endsWith(".successful")) {
                        messageDisplayString =
                                AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString =
                                AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(pad(count, successPad));
                    } else if (subKey.endsWith(".failed")) {
                        messageDisplayString =
                                AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString =
                                AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(pad(count, failedPad));
                    } else if (subKey.endsWith(".aborted")) {
                        messageDisplayString =
                                AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString =
                                AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(pad(count, abortedPad));
                    } else {
                        messageDisplayString = AnsiColor.TEXT_WHITE_BOLD.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_WHITE_BOLD.wrap(pad(count, skipPad));
                    }

                    if (count == 0) {
                        // Revert display message and count display message to TEST_WRITE_BRIGHT
                        messageDisplayString = AnsiColor.stripAnsiCodes(messageDisplayString);
                        countDisplayString = AnsiColor.stripAnsiCodes(countDisplayString);

                        messageDisplayString =
                                AnsiColor.TEXT_WHITE_BRIGHT.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_WHITE_BRIGHT.wrap(countDisplayString);
                    }

                    stringBuilder
                            .append(" ")
                            .append(messageDisplayString)
                            .append(": ")
                            .append(countDisplayString);
                }

                println(stringBuilder);
            }

            println(INFO + SEPARATOR);

            String message =
                    failureCount.get() > 0
                            ? AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("TESTS FAILED")
                            : AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("TESTS PASSED");

            println(INFO + message);
            println(INFO + SEPARATOR);

            Duration elapsedTime = stopWatch.elapsedTime();

            println(
                    new AnsiColor.StringBuilder()
                            .append(INFO)
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Total time  : ")
                            .append(
                                    HumanReadableTimeSupport.toHumanReadable(
                                            elapsedTime.toNanos(),
                                            HumanReadableTimeSupport.Format.SHORT))
                            .append(" (")
                            .append(elapsedTime.toNanos() / 1e+6D)
                            .append(" ms)")
                            .append(AnsiColor.NONE));

            println(
                    new AnsiColor.StringBuilder()
                            .append(INFO)
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Finished at : ")
                            .append(HumanReadableTimeSupport.now())
                            .append(AnsiColor.NONE));

            if (failureCount.get() == 0) {
                println(INFO + SEPARATOR);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
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
        int pad = 0;

        for (AtomicLong atomicLong : atomicLongs) {
            pad = Math.max(pad, String.valueOf(atomicLong.get()).length());
        }

        return pad;
    }

    /**
     * Method to get a String that is the value passed to a specific width
     *
     * @param value value
     * @param width width
     * @return the return value
     */
    private static String pad(long value, long width) {
        String string = String.valueOf(value);
        StringBuilder stringBuilder = new StringBuilder();

        while ((stringBuilder.length() + string.length()) < width) {
            stringBuilder.append(" ");
        }

        return stringBuilder.append(string).toString();
    }
}
