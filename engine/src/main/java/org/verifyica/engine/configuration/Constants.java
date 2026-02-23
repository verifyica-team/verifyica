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

package org.verifyica.engine.configuration;

/**
 * A collection of constants used throughout the Verifyica engine for configuration.
 *
 * <p>This class defines string constants for all configuration property keys used by
 * the engine. These constants provide a centralized way to reference configuration
 * properties and ensure consistency across the codebase.
 *
 * <p>Configuration properties follow the pattern {@code verifyica.engine.*} and control
 * various aspects of engine behavior including threading, logging, filtering, and parallelism.
 */
public final class Constants {

    /**
     * String representation of boolean true for configuration values.
     */
    public static final String TRUE = "true";

    /**
     * String representation of boolean false for configuration values.
     */
    public static final String FALSE = "false";

    /**
     * Configuration value indicating virtual thread execution.
     */
    public static final String VIRTUAL = "virtual";

    /**
     * Configuration value indicating platform thread execution.
     */
    public static final String PLATFORM = "platform";

    /**
     * Configuration value indicating ephemeral platform thread execution.
     */
    public static final String PLATFORM_EPHEMERAL = "platform-ephemeral";

    /**
     * The base prefix for all Verifyica configuration properties.
     */
    public static final String PREFIX = "verifyica";

    /**
     * The base prefix for engine-related configuration properties.
     */
    public static final String ENGINE = PREFIX + ".engine";

    /**
     * Configuration property for thread-related settings.
     */
    public static final String ENGINE_THREAD = ENGINE + ".thread";

    /**
     * Configuration property for thread type (virtual, platform, or platform-ephemeral).
     */
    public static final String ENGINE_THREAD_TYPE = ENGINE_THREAD + ".type";

    /**
     * Configuration property for logger regex pattern to filter which loggers are enabled.
     */
    public static final String ENGINE_LOGGER_REGEX = ENGINE + ".logger.regex";

    /**
     * Configuration property for logger level (TRACE, DEBUG, INFO, WARN, ERROR).
     */
    public static final String ENGINE_LOGGER_LEVEL = ENGINE + ".logger.level";

    /**
     * Configuration property for autowiring dependencies.
     */
    public static final String ENGINE_AUTOWIRED = ENGINE + ".autowired";

    /**
     * Configuration property for pruning stack traces in error output.
     */
    public static final String ENGINE_PRUNE_STACK_TRACE = ENGINE + ".prune.stacktraces";

    /**
     * Configuration property for engine-level autowired dependencies.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE = ENGINE_AUTOWIRED + ".engine";

    /**
     * Configuration property for engine-level interceptor autowiring.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS = ENGINE_AUTOWIRED_ENGINE + ".interceptors";

    /**
     * Configuration property for engine-level interceptor include patterns.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".include";

    /**
     * Configuration property for engine-level interceptor include regex patterns.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE + ".regex";

    /**
     * Configuration property for engine-level interceptor exclude patterns.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".exclude";

    /**
     * Configuration property for engine-level interceptor exclude regex patterns.
     */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE + ".regex";

    /**
     * Configuration property for test class-related settings.
     */
    public static final String ENGINE_CLASS = ENGINE + ".class";

    /**
     * Configuration property for class-level autowired dependencies.
     */
    public static final String ENGINE_AUTOWIRED_CLASS = ENGINE_AUTOWIRED + ".class";

    /**
     * Configuration property for class-level interceptor autowiring.
     */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS = ENGINE_AUTOWIRED_CLASS + ".interceptors";

    /**
     * Configuration property for class-level interceptor include patterns.
     */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".include";

    /**
     * Configuration property for class-level interceptor include regex patterns.
     */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE + ".regex";

    /**
     * Configuration property for class-level interceptor exclude patterns.
     */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".exclude";

    /**
     * Configuration property for class-level interceptor exclude regex patterns.
     */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE + ".regex";

    /**
     * Configuration property for test class filtering.
     */
    public static final String ENGINE_FILTER = ENGINE + ".filter";

    /**
     * Configuration property for filter definitions.
     */
    public static final String ENGINE_FILTER_DEFINITIONS = ENGINE_FILTER + ".definitions";

    /**
     * Configuration property for filter definitions filename.
     */
    public static final String ENGINE_FILTER_DEFINITIONS_FILENAME = ENGINE_FILTER_DEFINITIONS + ".filename";

    /**
     * Configuration property for test class parallelism.
     */
    public static final String ENGINE_CLASS_PARALLELISM = ENGINE_CLASS + ".parallelism";

    /**
     * Configuration property for test class state machine throttle.
     */
    public static final String ENGINE_CLASS_STATE_MACHINE_THROTTLE = ENGINE_CLASS + ".state.machine.throttle";

    /**
     * Configuration property for test argument-related settings.
     */
    public static final String ENGINE_ARGUMENT = ENGINE + ".argument";

    /**
     * Configuration property for test argument parallelism.
     */
    public static final String ENGINE_ARGUMENT_PARALLELISM = ENGINE_ARGUMENT + ".parallelism";

    /**
     * Configuration property for test argument state machine throttle.
     */
    public static final String ENGINE_ARGUMENT_STATE_MACHINE_THROTTLE = ENGINE_ARGUMENT + ".state.machine.throttle";

    /**
     * Configuration property for test-related settings.
     */
    public static final String ENGINE_TEST = ENGINE + ".test";

    /**
     * Configuration property for test state machine throttle.
     */
    public static final String ENGINE_TEST_STATE_MACHINE_THROTTLE = ENGINE_TEST + ".state.machine.throttle";

    /**
     * Configuration property for Maven plugin detection.
     */
    public static final String MAVEN_PLUGIN = PREFIX + ".maven.plugin";

    /**
     * Configuration property for Maven plugin mode.
     */
    public static final String MAVEN_PLUGIN_MODE = MAVEN_PLUGIN + ".mode";

    /**
     * Configuration property for Maven plugin version.
     */
    public static final String MAVEN_PLUGIN_VERSION = MAVEN_PLUGIN + ".version";

    /**
     * Configuration property for Maven plugin logging.
     */
    public static final String MAVEN_PLUGIN_LOG = MAVEN_PLUGIN + ".log";

    /**
     * Configuration property for Maven plugin test logging.
     */
    public static final String MAVEN_PLUGIN_LOG_TESTS = MAVEN_PLUGIN_LOG + ".tests";

    /**
     * Configuration property for Maven plugin timing units.
     */
    public static final String MAVEN_PLUGIN_LOG_TIMING_UNITS = MAVEN_PLUGIN_LOG + ".units";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Constants() {
        // INTENTIONALLY EMPTY
    }
}
