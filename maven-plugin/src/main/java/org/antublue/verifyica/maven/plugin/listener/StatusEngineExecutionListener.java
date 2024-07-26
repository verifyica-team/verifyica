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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ExecutableTestDescriptor;
import org.antublue.verifyica.engine.descriptor.MethodTestDescriptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.HumanReadableTimeSupport;
import org.antublue.verifyica.engine.util.AnsiColor;
import org.antublue.verifyica.engine.util.AnsiColorStringBuilder;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/** Class to implement StatusEngineExecutionListener */
public class StatusEngineExecutionListener implements EngineExecutionListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StatusEngineExecutionListener.class);

    private static final String INFO =
            new AnsiColorStringBuilder()
                    .color(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .color(AnsiColor.TEXT_BLUE_BOLD)
                    .append("INFO")
                    .color(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .color(AnsiColor.TEXT_RESET)
                    .toString();

    private final boolean consoleLogTiming;
    private final String consoleLogTimingUnits;
    private final boolean consoleLogMessagesStarted;
    private final String consoleTestMessage;
    private final boolean consoleLogMessaagesSkipped;
    private final String consoleSkipMessage;
    private final boolean consoleLogMessagesFinished;
    private final String consolePassMessage;
    private final String consoleFailMessage;

    /** Constructor */
    public StatusEngineExecutionListener() {
        Configuration configuration = DefaultEngineContext.getInstance().getConfiguration();

        consoleLogTiming =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_TIMING))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]", Constants.MAVEN_PLUGIN_LOG_TIMING, consoleLogTiming);

        consoleLogTimingUnits =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_TIMING_UNITS))
                        .orElse("milliseconds");

        LOGGER.trace(
                "configuration [%s] = [%s]",
                Constants.MAVEN_PLUGIN_TIMING_UNITS, consoleLogTimingUnits);

        consoleLogMessagesStarted =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_MESSAGES_STARTED))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_LOG_MESSAGES_STARTED, consoleLogMessagesStarted);

        consoleLogMessagesFinished =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_MESSAGES_FINISHED))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_LOG_MESSAGES_FINISHED, consoleLogMessagesFinished);

        consoleLogMessaagesSkipped =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_MESSAGES_SKIPPED))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_LOG_MESSAGES_SKIPPED, consoleLogMessaagesSkipped);

        consoleTestMessage =
                new AnsiColorStringBuilder()
                        .append(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append(
                                Optional.ofNullable(
                                                configuration.get(
                                                        Constants.MAVEN_PLUGIN_LOG_TEST_MESSAGE))
                                        .orElse("TEST"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consolePassMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_GREEN_BOLD_BRIGHT)
                        .append(
                                Optional.ofNullable(
                                                configuration.get(
                                                        Constants.MAVEN_PLUGIN_LOG_PASS_MESSAGE))
                                        .orElse("PASS"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consoleSkipMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_YELLOW_BOLD_BRIGHT)
                        .append(
                                Optional.ofNullable(
                                                configuration.get(
                                                        Constants.MAVEN_PLUGIN_LOG_SKIP_MESSAGE))
                                        .orElse("SKIP"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();

        consoleFailMessage =
                new AnsiColorStringBuilder()
                        .color(AnsiColor.TEXT_RED_BOLD_BRIGHT)
                        .append(
                                Optional.ofNullable(
                                                configuration.get(
                                                        Constants.MAVEN_PLUGIN_LOG_FAIL_MESSAGE))
                                        .orElse("FAIL"))
                        .color(AnsiColor.TEXT_RESET)
                        .toString();
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        if (consoleLogMessagesStarted && testDescriptor instanceof ExecutableTestDescriptor) {
            try {
                ExecutableTestDescriptor executableTestDescriptor =
                        (ExecutableTestDescriptor) testDescriptor;

                Class<?> testClass = executableTestDescriptor.getTestClass();
                String testClassDisplayName = DisplayNameSupport.getDisplayName(testClass);

                String testMethodDisplayName = null;
                if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Method testMethod =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestMethod();
                    testMethodDisplayName = DisplayNameSupport.getDisplayName(testMethod) + "()";
                }

                String testArgumentDisplayName = null;
                if (executableTestDescriptor instanceof ArgumentTestDescriptor) {
                    Argument<?> testArgument =
                            ((ArgumentTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                } else if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Argument<?> testArgument =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                }

                AnsiColorStringBuilder ansiColorStringBuilder =
                        new AnsiColorStringBuilder()
                                .append(INFO)
                                .append(" ")
                                .append(Thread.currentThread().getName())
                                .append(" | ")
                                .append(consoleTestMessage)
                                .color(AnsiColor.TEXT_RESET);

                if (testArgumentDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testArgumentDisplayName);
                }

                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        if (consoleLogMessaagesSkipped && testDescriptor instanceof ExecutableTestDescriptor) {
            try {
                ExecutableTestDescriptor executableTestDescriptor =
                        (ExecutableTestDescriptor) testDescriptor;
                Class<?> testClass = executableTestDescriptor.getTestClass();
                String testClassDisplayName = DisplayNameSupport.getDisplayName(testClass);

                String testMethodDisplayName = null;
                if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Method testMethod =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestMethod();
                    testMethodDisplayName = DisplayNameSupport.getDisplayName(testMethod) + "()";
                }

                String testArgumentDisplayName = null;
                if (executableTestDescriptor instanceof ArgumentTestDescriptor) {
                    Argument<?> testArgument =
                            ((ArgumentTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                } else if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Argument<?> testArgument =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                }

                Duration elapsedTime = executableTestDescriptor.getStopWatch().elapsedTime();

                AnsiColorStringBuilder ansiColorStringBuilder =
                        new AnsiColorStringBuilder()
                                .append(INFO)
                                .append(" ")
                                .append(Thread.currentThread().getName())
                                .append(" | ")
                                .append(AnsiColor.TEXT_WHITE_BRIGHT);

                ansiColorStringBuilder.append(consoleSkipMessage).color(AnsiColor.TEXT_RESET);

                if (testArgumentDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testArgumentDisplayName);
                }

                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
                }

                if (consoleLogTiming && elapsedTime != null) {
                    ansiColorStringBuilder
                            .append(" ")
                            .append(
                                    HumanReadableTimeSupport.toTimingUnit(
                                            elapsedTime.toNanos(), consoleLogTimingUnits));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        if (consoleLogMessagesFinished && testDescriptor instanceof ExecutableTestDescriptor) {
            try {
                ExecutableTestDescriptor executableTestDescriptor =
                        (ExecutableTestDescriptor) testDescriptor;
                Class<?> testClass = executableTestDescriptor.getTestClass();
                String testClassDisplayName = DisplayNameSupport.getDisplayName(testClass);

                String testMethodDisplayName = null;
                if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Method testMethod =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestMethod();
                    testMethodDisplayName = DisplayNameSupport.getDisplayName(testMethod) + "()";
                }

                String testArgumentDisplayName = null;
                if (executableTestDescriptor instanceof ArgumentTestDescriptor) {
                    Argument<?> testArgument =
                            ((ArgumentTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                } else if (executableTestDescriptor instanceof MethodTestDescriptor) {
                    Argument<?> testArgument =
                            ((MethodTestDescriptor) executableTestDescriptor).getTestArgument();
                    testArgumentDisplayName = testArgument.getName();
                }

                Duration elapsedTime = executableTestDescriptor.getStopWatch().elapsedTime();

                AnsiColorStringBuilder ansiColorStringBuilder =
                        new AnsiColorStringBuilder()
                                .append(INFO)
                                .append(" ")
                                .append(Thread.currentThread().getName())
                                .append(" | ")
                                .append(AnsiColor.TEXT_WHITE_BRIGHT);

                TestExecutionResult.Status status = testExecutionResult.getStatus();

                switch (status) {
                    case SUCCESSFUL:
                        {
                            ansiColorStringBuilder.append(consolePassMessage);
                            break;
                        }
                    case FAILED:
                        {
                            ansiColorStringBuilder.append(consoleFailMessage);
                            break;
                        }
                    case ABORTED:
                        {
                            ansiColorStringBuilder.append(consoleSkipMessage);
                            break;
                        }
                    default:
                        {
                            ansiColorStringBuilder.append(AnsiColor.TEXT_CYAN_BOLD.wrap("????"));
                        }
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                if (testArgumentDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testArgumentDisplayName);
                }

                ansiColorStringBuilder.append(" | ").append(testClassDisplayName);

                if (testMethodDisplayName != null) {
                    ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
                }

                if (consoleLogTiming && elapsedTime != null) {
                    ansiColorStringBuilder
                            .append(" ")
                            .append(
                                    HumanReadableTimeSupport.toTimingUnit(
                                            elapsedTime.toNanos(), consoleLogTimingUnits));
                }

                ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

                System.out.println(ansiColorStringBuilder);
                System.out.flush();
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }
}
