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

// TODO change to environment variable names

/** Class to implement Constants */
public final class Constants {

    /** Configuration constant */
    public static final String TRUE = "true";

    /** Configuration constant */
    public static final String PREFIX = "verifyica";

    /** Configuration constant */
    public static final String VERSION = PREFIX + ".version";

    /** Configuration constant */
    public static final String THREAD_TYPE = PREFIX + ".thread.type";

    /** Configuration constant */
    public static final String PARALLELISM = PREFIX + ".parallelism";

    /** Configuration constant */
    public static final String LOGGER_REGEX = PREFIX + ".logger.regex";

    /** Configuration constant */
    public static final String LOGGER_LEVEL = PREFIX + ".logger.level";

    /** Configuration constant */
    public static final String CONSOLE_LOG = PREFIX + ".console.log";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TIMING = CONSOLE_LOG + ".timing";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TIMING_UNITS = CONSOLE_LOG_TIMING + ".units";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TEST_MESSAGE = CONSOLE_LOG + ".test.message";

    /** Configuration constant */
    public static final String CONSOLE_LOG_TEST_MESSAGES = CONSOLE_LOG + ".test.messages";

    /** Configuration constant */
    public static final String CONSOLE_LOG_SKIP_MESSAGE = CONSOLE_LOG + ".skip.message";

    /** Configuration constant */
    public static final String CONSOLE_LOG_SKIP_MESSAGES = CONSOLE_LOG + ".skip.messages";

    /** Configuration constant */
    public static final String CONSOLE_LOG_PASS_MESSAGE = CONSOLE_LOG + ".pass.message";

    /** Configuration constant */
    public static final String CONSOLE_LOG_PASS_MESSAGES = CONSOLE_LOG + ".pass.messages";

    /** Configuration constant */
    public static final String CONSOLE_LOG_FAIL_MESSAGE = CONSOLE_LOG + ".fail.message";

    /** Configuration constant */
    public static final String TEST_CLASS_SHUFFLE = PREFIX + ".test.class.shuffle";

    /** Configuration constant */
    public static final String TEST_CLASS_INCLUDE_REGEX = PREFIX + ".test.class.include.regex";

    /** Configuration constant */
    public static final String TEST_CLASS_EXCLUDE_REGEX = PREFIX + ".test.class.exclude.regex";

    /** Configuration constant */
    public static final String TEST_METHOD_INCLUDE_REGEX = PREFIX + ".test.method.include.regex";

    /** Configuration constant */
    public static final String TEST_METHOD_EXCLUDE_REGEX = PREFIX + ".test.method.exclude.regex";

    /** Configuration constant */
    public static final String TEST_CLASS_TAG_INCLUDE_REGEX =
            PREFIX + ".test.class.tag.include.regex";

    /** Configuration constant */
    public static final String TEST_CLASS_TAG_EXCLUDE_REGEX =
            PREFIX + ".test.class.tag.exclude.regex";

    /** Configuration constant */
    public static final String TEST_METHOD_TAG_INCLUDE_REGEX =
            PREFIX + ".test.method.tag.include.regex";

    /** Configuration constant */
    public static final String TEST_METHOD_TAG_EXCLUDE_REGEX =
            PREFIX + ".test.method.tag.exclude.regex";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN = PREFIX + ".maven.plugin";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_MODE = MAVEN_PLUGIN + ".mode";

    /** Configuration constant */
    public static final String MAVEN_PLUGIN_VERSION = MAVEN_PLUGIN + ".version";

    /** Constructor */
    private Constants() {
        // DO NOTHING
    }
}
