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
 * Implements ANSI color escape sequences for terminal text formatting.
 *
 * <p>Provides a set of predefined colors and styles for console output, including
 * regular colors, bold, underlined, bright colors, and background colors.
 *
 * <p>ANSI color support is automatically detected based on the environment:
 * <ul>
 *   <li>Checks if the JVM is attached to a terminal
 *   <li>Respects the NO_COLOR environment variable to disable colors
 *   <li>Respects the ANSI_COLOR environment variable to force enable colors
 * </ul>
 */
@SuppressWarnings("unused")
public class AnsiColor {

    /**
     * Pattern to match ANSI escape sequences in strings.
     */
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;\\d]*m");

    /**
     * Environment variable to force enable ANSI colors.
     */
    private static final String ANSI_COLOR_ENVIRONMENT_VARIABLE = "ANSI_COLOR";

    /**
     * Environment variable to disable ANSI colors.
     */
    private static final String NO_COLOR_ENVIRONMENT_VARIABLE = "NO_COLOR";

    /**
     * Empty string constant.
     */
    private static final String EMPTY_STRING = "";

    /**
     * ANSI escape sequence to reset all formatting.
     */
    private static final String RESET_ESCAPE_SEQUENCE = "\033[0m";

    /**
     * No color (reset).
     */
    public static final AnsiColor NONE = new AnsiColor(RESET_ESCAPE_SEQUENCE);

    // Regular Colors

    /**
     * Black text color.
     */
    public static final AnsiColor TEXT_BLACK = new AnsiColor("\033[0;30m");

    /**
     * Red text color.
     */
    public static final AnsiColor TEXT_RED = new AnsiColor("\033[0;38;5;160m");

    /**
     * Green text color.
     */
    public static final AnsiColor TEXT_GREEN = new AnsiColor("\033[0;32m");

    /**
     * Yellow text color.
     */
    public static final AnsiColor TEXT_YELLOW = new AnsiColor("\033[0;33m");

    /**
     * Blue text color.
     */
    public static final AnsiColor TEXT_BLUE = new AnsiColor("\033[0;34m");

    /**
     * Purple (magenta) text color.
     */
    public static final AnsiColor TEXT_PURPLE = new AnsiColor("\033[0;35m");

    /**
     * Cyan text color.
     */
    public static final AnsiColor TEXT_CYAN = new AnsiColor("\033[0;36m");

    /**
     * White text color.
     */
    public static final AnsiColor TEXT_WHITE = new AnsiColor("\033[0;37m");

    // Bold

    /**
     * Bold black text color.
     */
    public static final AnsiColor TEXT_BLACK_BOLD = new AnsiColor("\033[1;30m");

    /**
     * Bold red text color.
     */
    public static final AnsiColor TEXT_RED_BOLD = new AnsiColor("\033[1;31m");

    /**
     * Bold green text color.
     */
    public static final AnsiColor TEXT_GREEN_BOLD = new AnsiColor("\033[1;32m");

    /**
     * Bold yellow text color.
     */
    public static final AnsiColor TEXT_YELLOW_BOLD = new AnsiColor("\033[1;33m");

    /**
     * Bold blue text color.
     */
    public static final AnsiColor TEXT_BLUE_BOLD = new AnsiColor("\033[1;34m");

    /**
     * Bold purple (magenta) text color.
     */
    public static final AnsiColor TEXT_PURPLE_BOLD = new AnsiColor("\033[1;35m");

    /**
     * Bold cyan text color.
     */
    public static final AnsiColor TEXT_CYAN_BOLD = new AnsiColor("\033[1;36m");

    /**
     * Bold white text color.
     */
    public static final AnsiColor TEXT_WHITE_BOLD = new AnsiColor("\033[1;37m");

    // Underline

    /**
     * Underlined black text color.
     */
    public static final AnsiColor TEXT_BLACK_UNDERLINED = new AnsiColor("\033[4;30m");

    /**
     * Underlined red text color.
     */
    public static final AnsiColor TEXT_RED_UNDERLINED = new AnsiColor("\033[4;31m");

    /**
     * Underlined green text color.
     */
    public static final AnsiColor TEXT_GREEN_UNDERLINED = new AnsiColor("\033[4;32m");

    /**
     * Underlined yellow text color.
     */
    public static final AnsiColor TEXT_YELLOW_UNDERLINED = new AnsiColor("\033[4;33m");

    /**
     * Underlined blue text color.
     */
    public static final AnsiColor TEXT_BLUE_UNDERLINED = new AnsiColor("\033[4;34m");

    /**
     * Underlined purple (magenta) text color.
     */
    public static final AnsiColor TEXT_PURPLE_UNDERLINED = new AnsiColor("\033[4;35m");

    /**
     * Underlined cyan text color.
     */
    public static final AnsiColor TEXT_CYAN_UNDERLINED = new AnsiColor("\033[4;36m");

    /**
     * Underlined white text color.
     */
    public static final AnsiColor TEXT_WHITE_UNDERLINED = new AnsiColor("\033[4;37m");

    // High Intensity

    /**
     * Bright black (gray) text color.
     */
    public static final AnsiColor TEXT_BLACK_BRIGHT = new AnsiColor("\033[0;90m");

    /**
     * Bright red text color.
     */
    public static final AnsiColor TEXT_RED_BRIGHT = new AnsiColor("\033[0;38;5;196m");

    /**
     * Bright green text color.
     */
    public static final AnsiColor TEXT_GREEN_BRIGHT = new AnsiColor("\033[0;92m");

    /**
     * Bright yellow text color.
     */
    public static final AnsiColor TEXT_YELLOW_BRIGHT = new AnsiColor("\033[0;93m");

    /**
     * Bright blue text color.
     */
    public static final AnsiColor TEXT_BLUE_BRIGHT = new AnsiColor("\033[0;94m");

    /**
     * Bright purple (magenta) text color.
     */
    public static final AnsiColor TEXT_PURPLE_BRIGHT = new AnsiColor("\033[0;95m");

    /**
     * Bright cyan text color.
     */
    public static final AnsiColor TEXT_CYAN_BRIGHT = new AnsiColor("\033[0;96m");

    /**
     * Bright white text color.
     */
    public static final AnsiColor TEXT_WHITE_BRIGHT = new AnsiColor("\033[1;97m");

    // Bold High Intensity

    /**
     * Bold bright black (gray) text color.
     */
    public static final AnsiColor TEXT_BLACK_BOLD_BRIGHT = new AnsiColor("\033[1;90m");

    /**
     * Bold bright red text color.
     */
    public static final AnsiColor TEXT_RED_BOLD_BRIGHT = new AnsiColor("\033[1;38;5;160m");

    /**
     * Bold bright green text color.
     */
    public static final AnsiColor TEXT_GREEN_BOLD_BRIGHT = new AnsiColor("\033[1;92m");

    /**
     * Bold bright yellow text color.
     */
    public static final AnsiColor TEXT_YELLOW_BOLD_BRIGHT = new AnsiColor("\033[1;93m");

    /**
     * Bold bright blue text color.
     */
    public static final AnsiColor TEXT_BLUE_BOLD_BRIGHT = new AnsiColor("\033[1;94m");

    /**
     * Bold bright purple (magenta) text color.
     */
    public static final AnsiColor TEXT_PURPLE_BOLD_BRIGHT = new AnsiColor("\033[1;95m");

    /**
     * Bold bright cyan text color.
     */
    public static final AnsiColor TEXT_CYAN_BOLD_BRIGHT = new AnsiColor("\033[1;96m");

    /**
     * Bold bright white text color.
     */
    public static final AnsiColor TEXT_WHITE_BOLD_BRIGHT = new AnsiColor("\033[1;97m");

    // Background

    /**
     * Black background color.
     */
    public static final AnsiColor BACKGROUND_BLACK = new AnsiColor("\033[40m");

    /**
     * Red background color.
     */
    public static final AnsiColor BACKGROUND_RED = new AnsiColor("\033[41m");

    /**
     * Green background color.
     */
    public static final AnsiColor BACKGROUND_GREEN = new AnsiColor("\033[42m");

    /**
     * Yellow background color.
     */
    public static final AnsiColor BACKGROUND_YELLOW = new AnsiColor("\033[43m");

    /**
     * Blue background color.
     */
    public static final AnsiColor BACKGROUND_BLUE = new AnsiColor("\033[44m");

    /**
     * Purple (magenta) background color.
     */
    public static final AnsiColor BACKGROUND_PURPLE = new AnsiColor("\033[45m");

    /**
     * Cyan background color.
     */
    public static final AnsiColor BACKGROUND_CYAN = new AnsiColor("\033[46m");

    /**
     * White background color.
     */
    public static final AnsiColor BACKGROUND_WHITE = new AnsiColor("\033[47m");

    // High Intensity backgrounds

    /**
     * Bright black (gray) background color.
     */
    public static final AnsiColor BACKGROUND_BLACK_BRIGHT = new AnsiColor("\033[0;100m");

    /**
     * Bright red background color.
     */
    public static final AnsiColor BACKGROUND_RED_BRIGHT = new AnsiColor("\033[0;101m");

    /**
     * Bright green background color.
     */
    public static final AnsiColor BACKGROUND_GREEN_BRIGHT = new AnsiColor("\033[0;102m");

    /**
     * Bright yellow background color.
     */
    public static final AnsiColor BACKGROUND_YELLOW_BRIGHT = new AnsiColor("\033[0;103m");

    /**
     * Bright blue background color.
     */
    public static final AnsiColor BACKGROUND_BLUE_BRIGHT = new AnsiColor("\033[0;104m");

    /**
     * Bright purple (magenta) background color.
     */
    public static final AnsiColor BACKGROUND_PURPLE_BRIGHT = new AnsiColor("\033[0;105m");

    /**
     * Bright cyan background color.
     */
    public static final AnsiColor BACKGROUND_CYAN_BRIGHT = new AnsiColor("\033[0;106m");

    /**
     * Bright white background color.
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
     * Creates a new AnsiColor with the specified escape sequence.
     *
     * @param escapeSequence the ANSI escape sequence
     */
    private AnsiColor(final String escapeSequence) {
        Precondition.notNullOrBlank(escapeSequence, "escapeSequence is null", "escapeSequence is blank");
        this.escapeSequence = escapeSequence;
    }

    /**
     * Wraps an Object's string representation (toString()) with an ANSI color escape
     * sequence.
     *
     * @param object the object to wrap
     * @return the string representation of the object wrapped with ANSI color codes
     */
    public String wrap(final Object object) {
        if (ANSI_COLOR_SUPPORTED) {
            final String text = String.valueOf(object);
            return new StringBuilder(escapeSequence.length() + text.length() + RESET_ESCAPE_SEQUENCE.length())
                    .append(escapeSequence)
                    .append(text)
                    .append(RESET_ESCAPE_SEQUENCE)
                    .toString();
        }
        return String.valueOf(object);
    }

    /**
     * Returns the ANSI color escape sequence String.
     *
     * @return if ANSI color is supported, the ANSI color escape sequence, else an empty string
     */
    @Override
    public String toString() {
        return ANSI_COLOR_SUPPORTED ? escapeSequence : EMPTY_STRING;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AnsiColor ansiColor = (AnsiColor) o;
        return Objects.equals(escapeSequence, ansiColor.escapeSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(escapeSequence);
    }

    /**
     * Strips ANSI escape sequences from the given string.
     *
     * @param string the string to process
     * @return a String stripped of ANSI escape sequences, or null if the input was null
     */
    public static String stripAnsiEscapeSequences(final String string) {
        return string == null ? null : ANSI_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Returns whether ANSI color escape sequences are supported.
     *
     * @return true if ANSI color escape sequences are supported, otherwise false
     */
    public static boolean isSupported() {
        return ANSI_COLOR_SUPPORTED;
    }

    /**
     * Sets/forces ANSI color escape sequences to be supported.
     *
     * @param ansiColorSupported true to enable ANSI color support, false to disable
     */
    public static void setSupported(final boolean ansiColorSupported) {
        ANSI_COLOR_SUPPORTED = ansiColorSupported;
    }

    /**
     * Returns an ANSI color for a custom ANSI color escape sequence.
     *
     * @param escapeSequence the ANSI escape sequence
     * @return an AnsiColor for the specified escape sequence
     */
    public static AnsiColor ofSequence(final String escapeSequence) {
        Precondition.notNullOrBlank(escapeSequence, "escapeSequence is null", "escapeSequence is blank");
        return new AnsiColor(escapeSequence);
    }
}
