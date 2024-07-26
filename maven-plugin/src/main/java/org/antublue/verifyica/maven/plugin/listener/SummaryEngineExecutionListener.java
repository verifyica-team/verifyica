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

package org.antublue.verifyica.maven.plugin.listener;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.antublue.verifyica.engine.VerifyicaEngine;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.MethodTestDescriptor;
import org.antublue.verifyica.engine.descriptor.StatusEngineDescriptor;
import org.antublue.verifyica.engine.support.HumanReadableTimeSupport;
import org.antublue.verifyica.engine.util.AnsiColor;
import org.antublue.verifyica.engine.util.AnsiColorStringBuilder;
import org.antublue.verifyica.engine.util.StopWatch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement a SummaryEngineExecutionListener */
public class SummaryEngineExecutionListener implements EngineExecutionListener {

    private static final String BANNER =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Verifyica ")
                    .append(VerifyicaEngine.VERSION)
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private static final String SUMMARY_BANNER =
            BANNER + AnsiColor.TEXT_WHITE_BRIGHT.wrap(" Summary");

    private static final String SEPARATOR =
            AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                    "------------------------------------------------------------------------");

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .color(AnsiColor.TEXT_BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .append(" ")
                    .toString();

    private static final Map<String, String> counterKeyToMessageDisplayStringMap = new HashMap<>();

    static {
        counterKeyToMessageDisplayStringMap.put("test.class.count", "TEST CLASSES  ");
        counterKeyToMessageDisplayStringMap.put("test.class.count.successful", "PASSED");
        counterKeyToMessageDisplayStringMap.put("test.class.count.failed", "FAILED");
        counterKeyToMessageDisplayStringMap.put("test.class.count.aborted", "ABORTED");
        counterKeyToMessageDisplayStringMap.put("test.class.count.skipped", "SKIPPED");
        counterKeyToMessageDisplayStringMap.put("test.argument.count", "TEST ARGUMENTS");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.successful", "PASSED");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.failed", "FAILED");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.aborted", "ABORTED");
        counterKeyToMessageDisplayStringMap.put("test.argument.count.skipped", "SKIPPED");
        counterKeyToMessageDisplayStringMap.put("test.method.count", "TEST METHODS  ");
        counterKeyToMessageDisplayStringMap.put("test.method.count.successful", "PASSED");
        counterKeyToMessageDisplayStringMap.put("test.method.count.failed", "FAILED");
        counterKeyToMessageDisplayStringMap.put("test.method.count.aborted", "ABORTED");
        counterKeyToMessageDisplayStringMap.put("test.method.count.skipped", "SKIPPED");
    }

    private boolean initialized;
    private boolean hasTests;
    private boolean hasFailures;

    private final Map<ClassTestDescriptor, TestExecutionResult>
            classTestDescriptorTestExecutionResultMap;

    private final Map<ClassTestDescriptor, String> classTestDescriptorSkippedMap;

    private final Map<ArgumentTestDescriptor, TestExecutionResult>
            argumentTestDescriptorTestExecutionResultMap;

    private final Map<ArgumentTestDescriptor, String> argumentTestDescriptorSkippedMap;

    private final Map<MethodTestDescriptor, TestExecutionResult>
            methodTestDescriptorTestExecutionResultMap;

    private final Map<MethodTestDescriptor, String> methodTestDescriptorSkippedMap;

    private final Map<String, AtomicLong> counterMap;
    private final StopWatch stopWatch;

    /** Constructor */
    public SummaryEngineExecutionListener() {
        classTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        classTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        argumentTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        argumentTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        methodTestDescriptorTestExecutionResultMap = new ConcurrentHashMap<>();
        methodTestDescriptorSkippedMap = new ConcurrentHashMap<>();

        counterMap = new ConcurrentHashMap<>();
        stopWatch = new StopWatch();
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        synchronized (this) {
            if (!initialized) {
                stopWatch.reset();

                println(INFO + SEPARATOR);
                println(INFO + BANNER);
                println(INFO + SEPARATOR);

                initialized = true;
            }
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
        } else if (testDescriptor instanceof MethodTestDescriptor) {
            methodTestDescriptorSkippedMap.put((MethodTestDescriptor) testDescriptor, reason);
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
        } else if (testDescriptor instanceof MethodTestDescriptor) {
            methodTestDescriptorTestExecutionResultMap.put(
                    (MethodTestDescriptor) testDescriptor, testExecutionResult);
        }

        if (!testDescriptor.isRoot()
                && testExecutionResult.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
            hasFailures = true;
        }

        if (testDescriptor instanceof StatusEngineDescriptor) {
            if (hasFailures) {
                ((StatusEngineDescriptor) testDescriptor).setHasFailures();
            }

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

            for (Map.Entry<MethodTestDescriptor, TestExecutionResult> mapEntry :
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

            String[] keys = {"test.class", "test.argument", "test.method"};

            int padding = 0;
            for (String key : keys) {
                key += ".count";

                long count = counterMap.computeIfAbsent(key, o -> new AtomicLong()).get();
                padding = Math.max(padding, String.valueOf(count).length());

                String[] subKeys = {
                    key + ".successful", key + ".failed", key + ".aborted", key + ".skipped"
                };

                for (String subKey : subKeys) {
                    count = counterMap.computeIfAbsent(subKey, o -> new AtomicLong()).get();
                    padding = Math.max(padding, String.valueOf(count).length());
                }
            }

            for (String key : keys) {
                StringBuilder stringBuilder = new StringBuilder();

                key += ".count";
                stringBuilder.append(
                        INFO
                                + AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                                        counterKeyToMessageDisplayStringMap.get(key))
                                + " : "
                                + AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                                        pad(
                                                counterMap
                                                        .computeIfAbsent(key, o -> new AtomicLong())
                                                        .get(),
                                                padding)));

                String[] subKeys = {
                    key + ".successful", key + ".failed", key + ".aborted", key + ".skipped"
                };

                for (String subKey : subKeys) {
                    String messageDisplayString = counterKeyToMessageDisplayStringMap.get(subKey);
                    long count = counterMap.computeIfAbsent(subKey, o -> new AtomicLong()).get();
                    String countDisplayString;

                    if (subKey.endsWith(".successful")) {
                        messageDisplayString =
                                AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(messageDisplayString) + " ";
                        countDisplayString =
                                AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap(pad(count, padding));
                    } else if (subKey.endsWith(".failed")) {
                        messageDisplayString =
                                AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(messageDisplayString) + " ";
                        countDisplayString =
                                AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap(pad(count, padding));
                    } else if (subKey.endsWith(".aborted")) {
                        messageDisplayString =
                                AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(messageDisplayString);
                        countDisplayString =
                                AnsiColor.TEXT_YELLOW_BOLD_BRIGHT.wrap(pad(count, padding));
                    } else {
                        messageDisplayString = AnsiColor.TEXT_WHITE_BOLD.wrap(messageDisplayString);
                        countDisplayString = AnsiColor.TEXT_WHITE_BOLD.wrap(pad(count, padding));
                    }

                    stringBuilder.append(", " + messageDisplayString + " : " + countDisplayString);
                }

                println(stringBuilder.toString());
            }

            println(INFO + SEPARATOR);

            String message;
            if (hasFailures) {
                message = AnsiColor.TEXT_RED_BOLD_BRIGHT.wrap("TESTS FAILED");
            } else {
                message = AnsiColor.TEXT_GREEN_BOLD_BRIGHT.wrap("TESTS PASSED");
            }

            println(INFO + message);
            println(INFO + SEPARATOR);

            Duration elapsedTime = stopWatch.elapsedTime();

            println(
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Total time  : ")
                            .append(
                                    HumanReadableTimeSupport.toHumanReadable(
                                            elapsedTime.toNanos(),
                                            HumanReadableTimeSupport.Format.SHORT))
                            .append(" (")
                            .append(elapsedTime.toNanos() / 1e+6D)
                            .append(" ms)")
                            .color(AnsiColor.TEXT_RESET));

            println(
                    new AnsiColorStringBuilder()
                            .append(INFO)
                            .color(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Finished at : ")
                            .append(HumanReadableTimeSupport.now())
                            .color(AnsiColor.TEXT_RESET));

            if (!hasFailures) {
                println(INFO + SEPARATOR);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    /**
     * Method to get whether tests were executed
     *
     * @return true if there were tests executed, otherwise false
     */
    public boolean hasTests() {
        return hasTests;
    }

    /**
     * Method to get whether failures were encountered
     *
     * @return true if there were failures, otherwise false
     */
    public boolean hasFailures() {
        return hasFailures;
    }

    /**
     * Method to println an Object
     *
     * @param object object
     */
    private static void println(Object object) {
        System.out.println(object);
        System.out.flush();
    }

    /**
     * Method to column width of long values as Strings
     *
     * @param values values
     * @return the return value
     */
    private static int getColumnWith(long... values) {
        int width = 0;

        for (long value : values) {
            width = Math.max(String.valueOf(value).length(), width);
        }

        return width;
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
