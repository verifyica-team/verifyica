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
import org.antublue.verifyica.engine.descriptor.Metadata;
import org.antublue.verifyica.engine.descriptor.MetadataTestDescriptor;
import org.antublue.verifyica.engine.descriptor.MetadataTestDescriptorConstants;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
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
    private final boolean consoleLogTestMessages;
    private final String consoleTestMessage;
    private final boolean consoleLogSkipMessages;
    private final String consoleSkipMessage;
    private final boolean consoleLogPassMessages;
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

        consoleLogTestMessages =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_TEST_MESSAGES))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_TEST_MESSAGES, consoleLogTestMessages);

        consoleLogPassMessages =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_PASS_MESSAGES))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_LOG_PASS_MESSAGES, consoleLogPassMessages);

        consoleLogSkipMessages =
                Optional.ofNullable(configuration.get(Constants.MAVEN_PLUGIN_LOG_SKIP_MESSAGES))
                        .map(Boolean::parseBoolean)
                        .orElse(true);

        LOGGER.trace(
                "configuration [%s] = [%b]",
                Constants.MAVEN_PLUGIN_LOG_SKIP_MESSAGES, consoleLogSkipMessages);

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
        try {
            if (consoleLogTestMessages && testDescriptor instanceof MetadataTestDescriptor) {
                executionStarted((MetadataTestDescriptor) testDescriptor);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void executionStarted(MetadataTestDescriptor metadataTestDescriptor) {
        Metadata metadata = metadataTestDescriptor.getMetadata();

        Class<?> testClass = metadata.get(MetadataTestDescriptorConstants.TEST_CLASS);

        String testClassDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME);

        Method testMethod = metadata.get(MetadataTestDescriptorConstants.TEST_METHOD);

        String testMethodDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME) + "()";

        Argument<?> testArgument = metadata.get(MetadataTestDescriptorConstants.TEST_ARGUMENT);

        AnsiColorStringBuilder ansiColorStringBuilder =
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(consoleTestMessage)
                        .color(AnsiColor.TEXT_RESET);

        if (testArgument != null) {
            ansiColorStringBuilder.append(" | ").append(testArgument.getName());
        }

        if (testClass != null) {
            ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
        }

        if (testMethod != null) {
            ansiColorStringBuilder.append(" | ").append(testMethodDisplayName);
        }

        ansiColorStringBuilder.color(AnsiColor.TEXT_RESET);

        System.out.println(ansiColorStringBuilder);
        System.out.flush();
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        try {
            if (consoleLogSkipMessages && testDescriptor instanceof MetadataTestDescriptor) {
                executionSkipped((MetadataTestDescriptor) testDescriptor);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void executionSkipped(MetadataTestDescriptor metadataTestDescriptor) {
        Metadata metadata = metadataTestDescriptor.getMetadata();

        Class<?> testClass = metadata.get(MetadataTestDescriptorConstants.TEST_CLASS);

        String testClassDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME);

        Argument<?> testArgument = metadata.get(MetadataTestDescriptorConstants.TEST_ARGUMENT);

        Method testMethod = metadata.get(MetadataTestDescriptorConstants.TEST_METHOD);

        String testMethodDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME) + "()";

        Duration elapsedTime =
                metadata.get(MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION);

        AnsiColorStringBuilder ansiColorStringBuilder =
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT);

        ansiColorStringBuilder.append(consoleSkipMessage).color(AnsiColor.TEXT_RESET);

        if (testArgument != null) {
            ansiColorStringBuilder.append(" | ").append(testArgument.getName());
        }

        if (testClass != null) {
            ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
        }

        if (testMethod != null) {
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
    }

    @Override
    public void executionFinished(
            TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        try {
            if (consoleLogPassMessages && testDescriptor instanceof MetadataTestDescriptor) {
                executionFinished((MetadataTestDescriptor) testDescriptor);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void executionFinished(MetadataTestDescriptor metadataTestDescriptor) {
        Metadata metadata = metadataTestDescriptor.getMetadata();

        Class<?> testClass = metadata.get(MetadataTestDescriptorConstants.TEST_CLASS);

        String testClassDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_CLASS_DISPLAY_NAME);

        Argument<?> testArgument = metadata.get(MetadataTestDescriptorConstants.TEST_ARGUMENT);

        Method testMethod = metadata.get(MetadataTestDescriptorConstants.TEST_METHOD);

        String testMethodDisplayName =
                metadata.get(MetadataTestDescriptorConstants.TEST_METHOD_DISPLAY_NAME) + "()";

        Duration elapsedTime =
                metadata.get(MetadataTestDescriptorConstants.TEST_DESCRIPTOR_DURATION);

        String testDescriptorStatus =
                metadata.get(MetadataTestDescriptorConstants.TEST_DESCRIPTOR_STATUS);

        AnsiColorStringBuilder ansiColorStringBuilder =
                new AnsiColorStringBuilder()
                        .append(INFO)
                        .append(" ")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(AnsiColor.TEXT_WHITE_BRIGHT);

        switch (testDescriptorStatus) {
            case "PASS":
                {
                    ansiColorStringBuilder.append(consolePassMessage);
                    break;
                }
            case "FAIL":
                {
                    ansiColorStringBuilder.append(consoleFailMessage);
                    break;
                }
            case "SKIP":
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

        if (testArgument != null) {
            ansiColorStringBuilder.append(" | ").append(testArgument.getName());
        }

        if (testClass != null) {
            ansiColorStringBuilder.append(" | ").append(testClassDisplayName);
        }

        if (testMethod != null) {
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
    }
}
