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

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.AnsiColoredString;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.descriptor.TestArgumentTestDescriptor;
import org.verifyica.engine.descriptor.TestClassTestDescriptor;
import org.verifyica.engine.descriptor.TestDescriptorStatus;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.TimestampSupport;

/**
 * StatusEngineExecutionListener provides status reporting for engine execution
 */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusEngineExecutionListener.class);

    /**
     * Predefined ANSI colored string for logging INFO statuses.
     */
    private static final String INFO = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE)
            .append("[")
            .append(AnsiColor.TEXT_BLUE_BOLD)
            .append("INFO")
            .append(AnsiColor.TEXT_WHITE)
            .append("]")
            .append(AnsiColor.NONE)
            .build();

    /**
     * Predefined ANSI colored string for logging TEST statuses.
     */
    private static final String TEST = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("TEST")
            .append(AnsiColor.NONE)
            .build();

    /**
     * Predefined ANSI colored string for logging PASS statuses.
     */
    private static final String PASS = new AnsiColoredString()
            .append(AnsiColor.TEXT_GREEN_BOLD_BRIGHT)
            .append("PASS")
            .append(AnsiColor.NONE)
            .build();

    /**
     * Predefined ANSI colored string for logging FAIL statuses.
     */
    private static final String FAIL = new AnsiColoredString()
            .append(AnsiColor.TEXT_RED_BOLD_BRIGHT)
            .append("FAIL")
            .append(AnsiColor.NONE)
            .build();

    /**
     * Predefined ANSI colored string for logging SKIP statuses.
     */
    private static final String SKIP = new AnsiColoredString()
            .append(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
            .append("SKIP")
            .append(AnsiColor.NONE)
            .build();

    /**
     * Predefined ANSI colored string for resetting color to none.
     */
    private static final String NONE_STRING = AnsiColor.NONE.toString();

    /**
     * Flag indicating whether to log tests to the console.
     */
    private final boolean consoleLogTests;

    /**
     * The timing units to use when logging test duration.
     */
    private final String consoleLogTimingUnits;

    /**
     * The map of test descriptors to stopwatches for tracking test duration.
     */
    private final Map<TestDescriptor, Stopwatch> stopwatches;

    /**
     * Thread-local StringBuilder for building log messages.
     */
    private final ThreadLocal<StringBuilder> stringBuilderThreadLocal;

    /**
     * Thread-local cache for descriptor information.
     */
    private final ThreadLocal<Map<TestDescriptor, DescriptorInfo>> descriptorInfoCache;

    /**
     * Constructs a new StatusEngineExecutionListener with default configuration settings.
     */
    public StatusEngineExecutionListener() {
        Configuration configuration = ConcreteConfiguration.getInstance();

        if ("false".equals(configuration.getProperties().getProperty(Constants.MAVEN_PLUGIN_LOG_TESTS, "true"))) {
            consoleLogTests = false;
        } else {
            consoleLogTests = true;
        }

        consoleLogTimingUnits =
                configuration.getProperties().getProperty(Constants.MAVEN_PLUGIN_LOG_TIMING_UNITS, "milliseconds");

        LOGGER.trace(
                "configuration property [%s] = [%s]", Constants.MAVEN_PLUGIN_LOG_TIMING_UNITS, consoleLogTimingUnits);

        stopwatches = new ConcurrentHashMap<>();

        stringBuilderThreadLocal = ThreadLocal.withInitial(() -> new StringBuilder(256));
        descriptorInfoCache = ThreadLocal.withInitial(() -> new ConcurrentHashMap<>());
    }

    /**
     * Helper class to cache descriptor information
     */
    private static class DescriptorInfo {
        final String testClassDisplayName;
        final String testArgumentDisplayName;
        final String testMethodDisplayName;

        DescriptorInfo(String testClassDisplayName, String testArgumentDisplayName, String testMethodDisplayName) {
            this.testClassDisplayName = testClassDisplayName;
            this.testArgumentDisplayName = testArgumentDisplayName;
            this.testMethodDisplayName = testMethodDisplayName;
        }
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof TestableTestDescriptor) {
            stopwatches.put(testDescriptor, new Stopwatch());

            if (consoleLogTests) {
                try {
                    DescriptorInfo info = getDescriptorInfo(testDescriptor);

                    StringBuilder stringBuilder = stringBuilderThreadLocal.get();
                    stringBuilder.setLength(0);

                    stringBuilder
                            .append(INFO)
                            .append(' ')
                            .append(Thread.currentThread().getName())
                            .append(" | ")
                            .append(TEST);

                    if (info.testArgumentDisplayName != null) {
                        stringBuilder.append(" | ").append(info.testArgumentDisplayName);
                    }

                    stringBuilder.append(" | ").append(info.testClassDisplayName);

                    if (info.testMethodDisplayName != null) {
                        stringBuilder.append(" | ").append(info.testMethodDisplayName);
                    }

                    stringBuilder.append(NONE_STRING);

                    System.out.println(stringBuilder.toString());
                } catch (Throwable t) {
                    StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
                }
            }
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (testDescriptor instanceof TestableTestDescriptor) {
            Duration elapsedTime = stopwatches.remove(testDescriptor).stop().elapsed();

            try {
                DescriptorInfo info = getDescriptorInfo(testDescriptor);

                StringBuilder stringBuilder = stringBuilderThreadLocal.get();
                stringBuilder.setLength(0);

                stringBuilder
                        .append(INFO)
                        .append(' ')
                        .append(Thread.currentThread().getName())
                        .append(" | ");

                stringBuilder.append(SKIP);

                if (info.testArgumentDisplayName != null) {
                    stringBuilder.append(" | ").append(info.testArgumentDisplayName);
                }

                stringBuilder.append(" | ").append(info.testClassDisplayName);

                if (info.testMethodDisplayName != null) {
                    stringBuilder.append(" | ").append(info.testMethodDisplayName);
                }

                stringBuilder
                        .append(' ')
                        .append(TimestampSupport.toTimingUnit(elapsedTime.toNanos(), consoleLogTimingUnits))
                        .append(NONE_STRING);

                System.out.println(stringBuilder.toString());
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (testDescriptor instanceof TestableTestDescriptor) {
            Duration elapsedTime = stopwatches.remove(testDescriptor).stop().elapsed();

            try {
                DescriptorInfo info = getDescriptorInfo(testDescriptor);

                StringBuilder stringBuilder = stringBuilderThreadLocal.get();
                stringBuilder.setLength(0);

                stringBuilder
                        .append(INFO)
                        .append(' ')
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT.toString());

                TestExecutionResult.Status status = getDescendantFailureCount(testDescriptor) > 0
                        ? TestExecutionResult.Status.FAILED
                        : testExecutionResult.getStatus();

                switch (status) {
                    case SUCCESSFUL: {
                        stringBuilder.append(PASS);
                        break;
                    }
                    case FAILED: {
                        stringBuilder.append(FAIL);
                        break;
                    }
                    case ABORTED: {
                        stringBuilder.append(SKIP);
                        break;
                    }
                    default: {
                        stringBuilder.append(AnsiColor.TEXT_CYAN_BOLD.wrap("????"));
                    }
                }

                stringBuilder.append(NONE_STRING);

                if (info.testArgumentDisplayName != null) {
                    stringBuilder.append(" | ").append(info.testArgumentDisplayName);
                }

                stringBuilder.append(" | ").append(info.testClassDisplayName);

                if (info.testMethodDisplayName != null) {
                    stringBuilder.append(" | ").append(info.testMethodDisplayName);
                }

                stringBuilder
                        .append(' ')
                        .append(TimestampSupport.toTimingUnit(elapsedTime.toNanos(), consoleLogTimingUnits))
                        .append(NONE_STRING);

                System.out.println(stringBuilder.toString());
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    /**
     * Gets or computes descriptor info with caching.
     *
     * @param testDescriptor the test descriptor
     * @return the descriptor info
     */
    private DescriptorInfo getDescriptorInfo(TestDescriptor testDescriptor) {
        Map<TestDescriptor, DescriptorInfo> cache = descriptorInfoCache.get();
        DescriptorInfo info = cache.get(testDescriptor);

        if (info == null) {
            String testClassDisplayName = null;
            String testArgumentDisplayName = null;
            String testMethodDisplayName = null;

            TestClassTestDescriptor classDescriptor = findClassTestDescriptor(testDescriptor);
            if (classDescriptor != null) {
                testClassDisplayName = classDescriptor.getDisplayName();
            }

            TestArgumentTestDescriptor testArgumentTestDescriptor = findArgumentTestDescriptor(testDescriptor);
            if (testArgumentTestDescriptor != null) {
                testArgumentDisplayName =
                        testArgumentTestDescriptor.getTestArgument().getName();
            }

            TestMethodTestDescriptor testMethodTestDescriptor = findTestMethodTestDescriptor(testDescriptor);
            if (testMethodTestDescriptor != null) {
                testMethodDisplayName = testMethodTestDescriptor.getDisplayName() + "()";
            }

            info = new DescriptorInfo(testClassDisplayName, testArgumentDisplayName, testMethodDisplayName);
            cache.put(testDescriptor, info);
        }

        return info;
    }

    /**
     * Gets the count of descendant test descriptors that have failed.
     *
     * @param testDescriptor the test descriptor
     * @return the count of descendant failures
     */
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
     * Finds the ClassTestDescriptor for the given test descriptor.
     *
     * @param testDescriptor the test descriptor
     * @return the ClassTestDescriptor or null if not found
     */
    private static TestClassTestDescriptor findClassTestDescriptor(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof TestClassTestDescriptor) {
            return (TestClassTestDescriptor) testDescriptor;
        }

        if (testDescriptor instanceof TestArgumentTestDescriptor) {
            return (TestClassTestDescriptor) testDescriptor.getParent().orElse(null);
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            return (TestClassTestDescriptor) testDescriptor
                    .getParent()
                    .flatMap(TestDescriptor::getParent)
                    .orElse(null);
        }

        return null;
    }

    /**
     * Resolves the ArgumentTestDescriptor for the given test descriptor.
     *
     * @param testDescriptor the test descriptor
     * @return the ArgumentTestDescriptor or null if not found
     */
    private static TestArgumentTestDescriptor findArgumentTestDescriptor(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof TestArgumentTestDescriptor) {
            return (TestArgumentTestDescriptor) testDescriptor;
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            return (TestArgumentTestDescriptor) testDescriptor.getParent().orElse(null);
        }

        return null;
    }

    /**
     * Finds the TestMethodTestDescriptor for the given test descriptor.
     *
     * @param testDescriptor the test descriptor
     * @return the TestMethodTestDescriptor or null if not found
     */
    private static TestMethodTestDescriptor findTestMethodTestDescriptor(TestDescriptor testDescriptor) {
        return testDescriptor instanceof TestMethodTestDescriptor ? (TestMethodTestDescriptor) testDescriptor : null;
    }
}
