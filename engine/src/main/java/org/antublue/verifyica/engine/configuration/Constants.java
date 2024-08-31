/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.configuration;

/** Class to implement Constants */
public final class Constants {

    /** Configuration constant */
    public static final String TRUE = "true";

    /** Configuration constant */
    public static final String PREFIX = "verifyica";

    /** Configuration constant */
    public static final String PLUGIN = PREFIX + ".plugin";

    /** Configuration constant */
    public static final String ENGINE = PREFIX + ".engine";

    /** Configuration constant */
    public static final String ENGINE_LOGGER_REGEX = ENGINE + ".logger.regex";

    /** Configuration constant */
    public static final String ENGINE_LOGGER_LEVEL = ENGINE + ".logger.level";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED = ENGINE + ".autowired";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE = ENGINE_AUTOWIRED + ".engine";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS =
            ENGINE_AUTOWIRED_ENGINE + ".interceptors";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".include";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE + ".regex";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS + ".exclude";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX =
            ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE + ".regex";

    /** Configuration constant */
    public static final String ENGINE_CLASS = ENGINE + ".class";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS = ENGINE_AUTOWIRED + ".class";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS =
            ENGINE_AUTOWIRED_CLASS + ".interceptors";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".include";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE + ".regex";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS + ".exclude";

    /** Configuration constant */
    public static final String ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX =
            ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE + ".regex";

    /** Configuration constant */
    public static final String ENGINE_FILTER = ENGINE + ".filter";

    /** Configuration constant */
    public static final String ENGINE_FILTER_DEFINITIONS = ENGINE_FILTER + ".definitions";

    /** Configuration constant */
    public static final String ENGINE_FILTER_DEFINITIONS_FILENAME =
            ENGINE_FILTER_DEFINITIONS + ".filename";

    /** Configuration constant */
    public static final String ENGINE_CLASS_PARALLELISM = ENGINE_CLASS + ".parallelism";

    /** Configuration constant */
    public static final String ENGINE_ARGUMENT = ENGINE + ".argument";

    /** Configuration constant */
    public static final String ENGINE_ARGUMENT_PARALLELISM = ENGINE_ARGUMENT + ".parallelism";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN = PREFIX + ".maven.plugin";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_MODE = MAVEN_PLUGIN + ".mode";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_VERSION = MAVEN_PLUGIN + ".version";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG = MAVEN_PLUGIN + ".log";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_TIMING_UNITS = MAVEN_PLUGIN_LOG + ".units";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_TEST_MESSAGE = MAVEN_PLUGIN_LOG + ".test.message";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_MESSAGES_STARTED =
            MAVEN_PLUGIN_LOG + ".messages.started";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_SKIP_MESSAGE = MAVEN_PLUGIN_LOG + ".skip.message";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_MESSAGES_SKIPPED =
            MAVEN_PLUGIN_LOG + ".skip.messages";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_PASS_MESSAGE = MAVEN_PLUGIN_LOG + ".pass.message";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_MESSAGES_FINISHED =
            MAVEN_PLUGIN_LOG + ".messages.finished";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_LOG_FAIL_MESSAGE = MAVEN_PLUGIN_LOG + ".fail.message";

    /** Constructor */
    private Constants() {
        // INTENTIONALLY BLANK
    }
}
