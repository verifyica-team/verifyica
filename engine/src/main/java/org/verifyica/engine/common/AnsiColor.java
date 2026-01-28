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

package org.verifyica.engine.common;

import java.io.Console;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Class to implement ANSIColor
 */
@SuppressWarnings("unused")
public class AnsiColor {

    /**
     * Constant
     */
    private static final String PREFIX = "verifyica.test.engine";

    /**
     * Constant
     */
    private static final String MAVEN_PLUGIN_MODE = PREFIX + ".maven.plugin.mode";

    /**
     * Constant
     */
    private static final String MAVEN_PLUGIN_BATCH = PREFIX + ".maven.plugin.batch";

    /**
     * Constant
     */
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*m");

    /**
     * Constant
     */
    private static final String ANSI_COLOR_ENVIRONMENT_VARIABLE = "ANSI_COLOR";

    /**
     * Constant
     */
    private static final String NO_COLOR_ENVIRONMENT_VARIABLE = "NO_COLOR";

    /**
     * Constant
     */
    private static final String EMPTY_STRING = "";

    /**
     * AnsiColor constant
     */
    public static final AnsiColor NONE = new AnsiColor("\033[0m");

    // Regular Colors

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLACK = new AnsiColor("\033[0;30m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_RED = new AnsiColor("\033[0;38;5;160m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_GREEN = new AnsiColor("\033[0;32m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_YELLOW = new AnsiColor("\033[0;33m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLUE = new AnsiColor("\033[0;34m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_PURPLE = new AnsiColor("\033[0;35m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_CYAN = new AnsiColor("\033[0;36m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_WHITE = new AnsiColor("\033[0;37m");

    // Bold

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLACK_BOLD = new AnsiColor("\033[1;30m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_RED_BOLD = new AnsiColor("\033[1;31m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_GREEN_BOLD = new AnsiColor("\033[1;32m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_YELLOW_BOLD = new AnsiColor("\033[1;33m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLUE_BOLD = new AnsiColor("\033[1;34m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_PURPLE_BOLD = new AnsiColor("\033[1;35m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_CYAN_BOLD = new AnsiColor("\033[1;36m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_WHITE_BOLD = new AnsiColor("\033[1;37m");

    // Underline

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLACK_UNDERLINED = new AnsiColor("\033[4;30m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_RED_UNDERLINED = new AnsiColor("\033[4;31m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_GREEN_UNDERLINED = new AnsiColor("\033[4;32m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_YELLOW_UNDERLINED = new AnsiColor("\033[4;33m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLUE_UNDERLINED = new AnsiColor("\033[4;34m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_PURPLE_UNDERLINED = new AnsiColor("\033[4;35m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_CYAN_UNDERLINED = new AnsiColor("\033[4;36m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_WHITE_UNDERLINED = new AnsiColor("\033[4;37m");

    // High Intensity

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLACK_BRIGHT = new AnsiColor("\033[0;90m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_RED_BRIGHT = new AnsiColor("\033[0;38;5;196m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_GREEN_BRIGHT = new AnsiColor("\033[0;92m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_YELLOW_BRIGHT = new AnsiColor("\033[0;93m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLUE_BRIGHT = new AnsiColor("\033[0;94m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_PURPLE_BRIGHT = new AnsiColor("\033[0;95m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_CYAN_BRIGHT = new AnsiColor("\033[0;96m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_WHITE_BRIGHT = new AnsiColor("\033[1;97m");

    // Bold High Intensity

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLACK_BOLD_BRIGHT = new AnsiColor("\033[1;90m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_RED_BOLD_BRIGHT = new AnsiColor("\033[1;38;5;160m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_GREEN_BOLD_BRIGHT = new AnsiColor("\033[1;92m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_YELLOW_BOLD_BRIGHT = new AnsiColor("\033[1;93m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_BLUE_BOLD_BRIGHT = new AnsiColor("\033[1;94m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_PURPLE_BOLD_BRIGHT = new AnsiColor("\033[1;95m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_CYAN_BOLD_BRIGHT = new AnsiColor("\033[1;96m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor TEXT_WHITE_BOLD_BRIGHT = new AnsiColor("\033[1;97m");

    // Background

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_BLACK = new AnsiColor("\033[40m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_RED = new AnsiColor("\033[41m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_GREEN = new AnsiColor("\033[42m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_YELLOW = new AnsiColor("\033[43m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_BLUE = new AnsiColor("\033[44m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_PURPLE = new AnsiColor("\033[45m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_CYAN = new AnsiColor("\033[46m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_WHITE = new AnsiColor("\033[47m");

    // High Intensity backgrounds

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_BLACK_BRIGHT = new AnsiColor("\033[0;100m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_RED_BRIGHT = new AnsiColor("\033[0;101m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_GREEN_BRIGHT = new AnsiColor("\033[0;102m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_YELLOW_BRIGHT = new AnsiColor("\033[0;103m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_BLUE_BRIGHT = new AnsiColor("\033[0;104m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_PURPLE_BRIGHT = new AnsiColor("\033[0;105m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_CYAN_BRIGHT = new AnsiColor("\033[0;106m");

    /**
     * AnsiColor constant
     */
    public static final AnsiColor BACKGROUND_WHITE_BRIGHT = new AnsiColor("\033[0;107m");

    private static boolean ANSI_COLOR_SUPPORTED;

    static {
        Console console = System.console();
        if (console == null) {
            ANSI_COLOR_SUPPORTED = false;
        } else {
            try {
                ANSI_COLOR_SUPPORTED = (Boolean) Class.forName("java.io.Console")
                        .getDeclaredMethod("isTerminal")
                        .invoke(console);
            } catch (Throwable t) {
                ANSI_COLOR_SUPPORTED = true;
            }
        }

        String noColor = System.getenv(NO_COLOR_ENVIRONMENT_VARIABLE);
        if (noColor != null && !noColor.trim().isEmpty()) {
            ANSI_COLOR_SUPPORTED = false;
        }

        String ansiColorForce = System.getenv(ANSI_COLOR_ENVIRONMENT_VARIABLE);
        if (ansiColorForce != null && !ansiColorForce.trim().isEmpty()) {
            ANSI_COLOR_SUPPORTED = true;
        }
    }

    private final String escapeSequence;

    /**
     * Constructor
     *
     * @param escapeSequence escapeSequence
     */
    private AnsiColor(String escapeSequence) {
        Precondition.notNullOrBlank(escapeSequence, "escapeSequence is null", "escapeSequence is blank");
        this.escapeSequence = escapeSequence;
    }

    /**
     * Method to wrap an Object's string representation (toString()) with an ANSI color escape
     * sequence
     *
     * @param object object
     * @return the return value
     */
    public String wrap(Object object) {
        return ANSI_COLOR_SUPPORTED ? escapeSequence + object + NONE : String.valueOf(object);
    }

    /**
     * Method to get the ANSI color escape sequence String
     *
     * @return if ANSI color is supported, the ANSI color escape sequence, else an empty string
     */
    @Override
    public String toString() {
        return ANSI_COLOR_SUPPORTED ? escapeSequence : EMPTY_STRING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnsiColor ansiColor = (AnsiColor) o;
        return Objects.equals(escapeSequence, ansiColor.escapeSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(escapeSequence);
    }

    /**
     * Method to strip ANSI escape sequences
     *
     * @param string string
     * @return a String stripped of ANSI escape sequences
     */
    public static String stripAnsiEscapeSequences(String string) {
        return string == null ? null : ANSI_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Method to indicate whether ANSI color escape sequences are supported
     *
     * @return true if ANSI color escape sequences are supported, otherwise false
     */
    public static boolean isSupported() {
        return ANSI_COLOR_SUPPORTED;
    }

    /**
     * Method to set/force ANSI color escape sequences to be supported
     *
     * @param ansiColorSupported ansiColorSupported
     */
    public static void setSupported(boolean ansiColorSupported) {
        ANSI_COLOR_SUPPORTED = ansiColorSupported;
    }

    /**
     * Method to get an ANSI color for a custom ANSI color escape sequence
     *
     * @param escapeSequence escapeSequence
     * @return an AnsiColor
     */
    public static AnsiColor ofSequence(String escapeSequence) {
        Precondition.notNullOrBlank(escapeSequence, "escapeSequence is null", "escapeSequence is blank");
        return new AnsiColor(escapeSequence);
    }
}
