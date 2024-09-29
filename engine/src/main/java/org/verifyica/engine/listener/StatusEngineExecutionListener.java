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

package org.verifyica.engine.listener;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
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
import org.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.verifyica.engine.descriptor.ClassTestDescriptor;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.HumanReadableTimeSupport;

/** Class to implement StatusEngineExecutionListener */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusEngineExecutionListener.class);

    private static final String INFO = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE)
            .append("[")
            .append(AnsiColor.TEXT_BLUE_BOLD)
            .append("INFO")
            .append(AnsiColor.TEXT_WHITE)
            .append("]")
            .append(AnsiColor.NONE)
            .build();

    private static final String TEST = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("TEST")
            .append(AnsiColor.NONE)
            .build();

    private static final String PASS = new AnsiColoredString()
            .append(AnsiColor.TEXT_GREEN_BOLD_BRIGHT)
            .append("PASS")
            .append(AnsiColor.NONE)
            .build();

    private static final String FAIL = new AnsiColoredString()
            .append(AnsiColor.TEXT_RED_BOLD_BRIGHT)
            .append("FAIL")
            .append(AnsiColor.NONE)
            .build();

    private static final String SKIP = new AnsiColoredString()
            .append(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
            .append("SKIP")
            .append(AnsiColor.NONE)
            .build();

    private final String consoleLogTimingUnits;
    private final Map<TestDescriptor, Stopwatch> stopWatchMap;

    /** Constructor */
    public StatusEngineExecutionListener() {
        Configuration configuration = ConcreteConfiguration.getInstance();

        consoleLogTimingUnits = Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_TIMING_UNITS))
                .orElse("milliseconds");

        LOGGER.trace("configuration property [%s] = [%s]", Constants.MAVEN_PLUGIN_TIMING_UNITS, consoleLogTimingUnits);

        stopWatchMap = new ConcurrentHashMap<>();
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (shouldProcessDescriptor(testDescriptor)) {
            stopWatchMap.put(testDescriptor, new Stopwatch());

            try {
                String testArgumentDisplayName = null;
                String testMethodDisplayName = null;
                String testClassDisplayName =
                        findClassTestDescriptor(testDescriptor).getDisplayName();

                // TODO ? check maven configuration, if truncate.class.name=true and class name != display name,
                // truncate
                // testClassDisplayName = truncateClassName(testClassDisplayName);

                ArgumentTestDescriptor argumentTestDescriptor = findArgumentTestDescriptor(testDescriptor);
                if (argumentTestDescriptor != null) {
                    testArgumentDisplayName =
                            argumentTestDescriptor.getArgument().getName();
                }

                TestMethodTestDescriptor testMethodTestDescriptor = findTestMethodTestDescriptor(testDescriptor);
                if (testMethodTestDescriptor != null) {
                    testMethodDisplayName = testMethodTestDescriptor.getDisplayName() + "()";
                }

                AnsiColoredString ansiColorAnsiColoredString = new AnsiColoredString()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(TEST)
                        .append(AnsiColor.NONE);

                if (testArgumentDisplayName != null) {
                    ansiColorAnsiColoredString.append(" | ").append(testArgumentDisplayName);
                }

                ansiColorAnsiColoredString.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColorAnsiColoredString.append(" | ").append(testMethodDisplayName);
                }

                ansiColorAnsiColoredString.append(AnsiColor.NONE);

                System.out.println(ansiColorAnsiColoredString);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (shouldProcessDescriptor(testDescriptor)) {
            Duration elapsedTime = stopWatchMap.remove(testDescriptor).stop().elapsedTime();

            try {
                String testArgumentDisplayName = null;
                String testMethodDisplayName = null;
                String testClassDisplayName =
                        findClassTestDescriptor(testDescriptor).getDisplayName();

                // TODO ? check maven configuration, if truncate.class.name=true and class name != display name,
                // truncate
                // testClassDisplayName = truncateClassName(testClassDisplayName);

                ArgumentTestDescriptor argumentTestDescriptor = findArgumentTestDescriptor(testDescriptor);
                if (argumentTestDescriptor != null) {
                    testArgumentDisplayName =
                            argumentTestDescriptor.getArgument().getName();
                }

                TestMethodTestDescriptor testMethodTestDescriptor = findTestMethodTestDescriptor(testDescriptor);
                if (testMethodTestDescriptor != null) {
                    testMethodDisplayName = testMethodTestDescriptor.getDisplayName() + "()";
                }

                AnsiColoredString ansiColoredString = new AnsiColoredString()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT);

                ansiColoredString.append(SKIP).append(AnsiColor.NONE);

                if (testArgumentDisplayName != null) {
                    ansiColoredString.append(" | ").append(testArgumentDisplayName);
                }

                ansiColoredString.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColoredString.append(" | ").append(testMethodDisplayName);
                }

                ansiColoredString
                        .append(" ")
                        .append(HumanReadableTimeSupport.toTimingUnit(elapsedTime.toNanos(), consoleLogTimingUnits));

                ansiColoredString.append(AnsiColor.NONE);

                System.out.println(ansiColoredString);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (shouldProcessDescriptor(testDescriptor)) {
            Duration elapsedTime = stopWatchMap.remove(testDescriptor).stop().elapsedTime();

            try {
                String testArgumentDisplayName = null;
                String testMethodDisplayName = null;
                String testClassDisplayName =
                        findClassTestDescriptor(testDescriptor).getDisplayName();

                // TODO ? check maven configuration, if truncate.class.name=true and class name != display name,
                // truncate
                // testClassDisplayName = truncateClassName(testClassDisplayName);

                ArgumentTestDescriptor argumentTestDescriptor = findArgumentTestDescriptor(testDescriptor);
                if (argumentTestDescriptor != null) {
                    testArgumentDisplayName =
                            argumentTestDescriptor.getArgument().getName();
                }

                TestMethodTestDescriptor testMethodTestDescriptor = findTestMethodTestDescriptor(testDescriptor);
                if (testMethodTestDescriptor != null) {
                    testMethodDisplayName = testMethodTestDescriptor.getDisplayName() + "()";
                }

                AnsiColoredString ansiColoredString = new AnsiColoredString()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT);

                TestExecutionResult.Status status = testExecutionResult.getStatus();

                switch (status) {
                    case SUCCESSFUL: {
                        ansiColoredString.append(PASS);
                        break;
                    }
                    case FAILED: {
                        ansiColoredString.append(FAIL);
                        break;
                    }
                    case ABORTED: {
                        ansiColoredString.append(SKIP);
                        break;
                    }
                    default: {
                        ansiColoredString.append(AnsiColor.TEXT_CYAN_BOLD.wrap("????"));
                    }
                }

                ansiColoredString.append(AnsiColor.NONE);

                if (testArgumentDisplayName != null) {
                    ansiColoredString.append(" | ").append(testArgumentDisplayName);
                }

                ansiColoredString.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColoredString.append(" | ").append(testMethodDisplayName);
                }

                ansiColoredString
                        .append(" ")
                        .append(HumanReadableTimeSupport.toTimingUnit(elapsedTime.toNanos(), consoleLogTimingUnits))
                        .append(AnsiColor.NONE);

                System.out.println(ansiColoredString);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    /**
     * Method to truncate a class name
     *
     * @param className className
     * @return a truncated class name of there are more than 3 tokens, else return the unmodified class name
     */
    /*
    private static String truncateClassName(String className) {
        String[] tokens = className.split("\\.");
        int length = tokens.length;

        if (length <= 3) {
            return className;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length - 3; i++) {
            stringBuilder.append(tokens[i].toCharArray()[0]).append("..");
        }

        return stringBuilder
                .append(tokens[length - 3])
                .append(".")
                .append(tokens[length - 2])
                .append(".")
                .append(tokens[length - 1])
                .toString();
    }
    */

    /**
     * Method to find the ClassTestDescriptor
     *
     * @param testDescriptor testDescriptor
     * @return the ClassTestDescriptor or null if not found
     */
    private static ClassTestDescriptor findClassTestDescriptor(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            return (ClassTestDescriptor) testDescriptor;
        }

        if (testDescriptor instanceof ArgumentTestDescriptor) {
            return (ClassTestDescriptor) testDescriptor.getParent().orElse(null);
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            return (ClassTestDescriptor) testDescriptor
                    .getParent()
                    .flatMap(TestDescriptor::getParent)
                    .orElse(null);
        }

        return null;
    }

    /**
     * Method to resolve the ArgumentTestDescriptor
     *
     * @param testDescriptor testDescriptor
     * @return the ArgumentTestDescriptor or null if not found
     */
    private static ArgumentTestDescriptor findArgumentTestDescriptor(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof ArgumentTestDescriptor) {
            return (ArgumentTestDescriptor) testDescriptor;
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            return (ArgumentTestDescriptor) testDescriptor.getParent().orElse(null);
        }

        return null;
    }

    /**
     * Method to find the TestMethodTestDescriptor
     *
     * @param testDescriptor testDescriptor
     * @return the TestMethodTestDescriptor or null if not found
     */
    private static TestMethodTestDescriptor findTestMethodTestDescriptor(TestDescriptor testDescriptor) {
        return testDescriptor instanceof TestMethodTestDescriptor ? (TestMethodTestDescriptor) testDescriptor : null;
    }

    /**
     * Method to return whether we should process the TestDescriptor
     *
     * @param testDescriptor testDescriptor
     * @return true if we should process the TestDescriptor, else false
     */
    private static boolean shouldProcessDescriptor(TestDescriptor testDescriptor) {
        return testDescriptor instanceof ClassTestDescriptor
                || testDescriptor instanceof ArgumentTestDescriptor
                || testDescriptor instanceof TestMethodTestDescriptor;
    }
}
