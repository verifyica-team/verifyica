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

import java.util.Objects;

/**
 * Builds ANSI colored strings.
 */
public class AnsiColoredString {

    private static final int DEFAULT_CAPACITY = 64;

    private final StringBuilder stringBuilder;
    private AnsiColor lastAnsiColor;

    /**
     * Creates an empty AnsiColoredString.
     */
    public AnsiColoredString() {
        this((AnsiColor) null);
    }

    /**
     * Creates an AnsiColoredString with the specified initial string.
     *
     * @param string the initial string to append
     */
    public AnsiColoredString(final String string) {
        stringBuilder = new StringBuilder(string.length());
        stringBuilder.append(string);
        lastAnsiColor = null;
    }

    /**
     * Creates an AnsiColoredString with the specified initial ANSI color.
     *
     * @param ansiColor the initial ANSI color to append
     */
    public AnsiColoredString(final AnsiColor ansiColor) {
        stringBuilder = new StringBuilder(DEFAULT_CAPACITY);

        if (ansiColor != null) {
            stringBuilder.append(ansiColor);
            lastAnsiColor = ansiColor;
        }
    }

    /**
     * Appends a boolean value.
     *
     * @param b the boolean value to append
     * @return this
     */
    public AnsiColoredString append(final boolean b) {
        stringBuilder.append(b);

        return this;
    }

    /**
     * Appends a short value.
     *
     * @param s the short value to append
     * @return this
     */
    public AnsiColoredString append(final short s) {
        stringBuilder.append(s);

        return this;
    }

    /**
     * Appends a character.
     *
     * @param c the character to append
     * @return this
     */
    public AnsiColoredString append(final char c) {
        stringBuilder.append(c);

        return this;
    }

    /**
     * Appends a character array.
     *
     * @param chars the character array to append
     * @return this
     */
    public AnsiColoredString append(final char[] chars) {
        stringBuilder.append(chars);

        return this;
    }

    /**
     * Appends a portion of a character array.
     *
     * @param chars the character array to append from
     * @param offset the starting offset in the array
     * @param length the number of characters to append
     * @return this
     */
    public AnsiColoredString append(final char[] chars, final int offset, final int length) {
        stringBuilder.append(chars, offset, length);
        return this;
    }

    /**
     * Appends an integer value.
     *
     * @param i the integer value to append
     * @return this
     */
    public AnsiColoredString append(final int i) {
        stringBuilder.append(i);
        return this;
    }

    /**
     * Appends a long value.
     *
     * @param l the long value to append
     * @return this
     */
    public AnsiColoredString append(final long l) {
        stringBuilder.append(l);
        return this;
    }

    /**
     * Appends a float value.
     *
     * @param f the float value to append
     * @return this
     */
    public AnsiColoredString append(final float f) {
        stringBuilder.append(f);
        return this;
    }

    /**
     * Appends a double value.
     *
     * @param d the double value to append
     * @return this
     */
    public AnsiColoredString append(final double d) {
        stringBuilder.append(d);
        return this;
    }

    /**
     * Appends a string.
     *
     * @param s the string to append
     * @return this
     */
    public AnsiColoredString append(final String s) {
        stringBuilder.append(s);
        return this;
    }

    /**
     * Appends an object.
     *
     * @param o the object to append
     * @return this
     */
    public AnsiColoredString append(final Object o) {
        stringBuilder.append(o);
        return this;
    }

    /**
     * Appends a StringBuffer.
     *
     * @param sb the StringBuffer to append
     * @return this
     */
    public AnsiColoredString append(final StringBuffer sb) {
        stringBuilder.append(sb);
        return this;
    }

    /**
     * Appends a CharSequence.
     *
     * @param cs the CharSequence to append
     * @return this
     */
    public AnsiColoredString append(final CharSequence cs) {
        stringBuilder.append(cs);
        return this;
    }

    /**
     * Appends an ANSI color.
     *
     * @param ansiColor the ANSI color to append
     * @return this
     */
    public AnsiColoredString append(final AnsiColor ansiColor) {
        if (ansiColor == null || (ansiColor.equals(AnsiColor.NONE) && stringBuilder.length() == 0)) {
            return this;
        }

        if (!ansiColor.equals(lastAnsiColor)) {
            stringBuilder.append(ansiColor);
            lastAnsiColor = ansiColor;
        }

        return this;
    }

    /**
     * Returns the length of the string.
     *
     * @return the length of the string
     */
    public int length() {
        return stringBuilder.length();
    }

    /**
     * Returns whether ANSI colors are supported.
     *
     * @return true if ANSI colors are supported, otherwise false
     */
    public boolean isAnsiColorSupported() {
        return AnsiColor.isSupported();
    }

    /**
     * Builds and returns the colored string.
     *
     * @return the colored string
     */
    public String build() {
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AnsiColoredString that = (AnsiColoredString) o;
        return stringBuilder.length() == that.stringBuilder.length()
                && Objects.equals(lastAnsiColor, that.lastAnsiColor)
                && stringBuilder.toString().equals(that.stringBuilder.toString());
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(lastAnsiColor);
        // Compute hash from StringBuilder contents without creating a temporary String
        for (int i = 0; i < stringBuilder.length(); i++) {
            result = 31 * result + stringBuilder.charAt(i);
        }
        return result;
    }
}
