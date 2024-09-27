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

package org.verifyica.engine.common;

import java.util.Objects;

/** Class to implement AnsiColoredString */
public class AnsiColoredString {

    private final StringBuilder stringBuilder;
    private AnsiColor lastAnsiColor;

    /** Constructor */
    public AnsiColoredString() {
        this((AnsiColor) null);
    }

    /**
     * Constructor
     *
     * @param string string
     */
    public AnsiColoredString(String string) {
        stringBuilder = new StringBuilder();
        stringBuilder.append(string);
        lastAnsiColor = null;
    }

    /**
     * Constructor
     *
     * @param ansiColor ansiColor
     */
    public AnsiColoredString(AnsiColor ansiColor) {
        stringBuilder = new StringBuilder();

        if (ansiColor != null) {
            stringBuilder.append(ansiColor);
            lastAnsiColor = ansiColor;
        }
    }

    /**
     * Method to append a boolean
     *
     * @param b boolean
     * @return this
     */
    public AnsiColoredString append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    /**
     * Method to append a boolean
     *
     * @param s short
     * @return this
     */
    public AnsiColoredString append(short s) {
        stringBuilder.append(s);
        return this;
    }

    /**
     * Method to append a character
     *
     * @param c character
     * @return this
     */
    public AnsiColoredString append(char c) {
        stringBuilder.append(c);
        return this;
    }

    /**
     * Method to append a character array
     *
     * @param chars character array
     * @return this
     */
    public AnsiColoredString append(char[] chars) {
        stringBuilder.append(chars);
        return this;
    }

    /**
     * Method to append a character array
     *
     * @param chars character array
     * @param offset offset
     * @param length length
     * @return this
     */
    public AnsiColoredString append(char[] chars, int offset, int length) {
        stringBuilder.append(chars, offset, length);
        return this;
    }

    /**
     * Method to append an int
     *
     * @param i int
     * @return this
     */
    public AnsiColoredString append(int i) {
        stringBuilder.append(i);
        return this;
    }

    /**
     * Method to append a long
     *
     * @param l long
     * @return this
     */
    public AnsiColoredString append(long l) {
        stringBuilder.append(l);
        return this;
    }

    /**
     * Method to append a float
     *
     * @param f float
     * @return this
     */
    public AnsiColoredString append(float f) {
        stringBuilder.append(f);
        return this;
    }

    /**
     * Method to append a double
     *
     * @param d double
     * @return this
     */
    public AnsiColoredString append(double d) {
        stringBuilder.append(d);
        return this;
    }

    /**
     * Method to append a String
     *
     * @param s String
     * @return this
     */
    public AnsiColoredString append(String s) {
        stringBuilder.append(s);
        return this;
    }

    /**
     * Method to append an Object
     *
     * @param o object
     * @return this
     */
    public AnsiColoredString append(Object o) {
        stringBuilder.append(o);
        return this;
    }

    /**
     * Method to append a StringBuffer
     *
     * @param sb StringBuffer
     * @return this
     */
    public AnsiColoredString append(StringBuffer sb) {
        stringBuilder.append(sb);
        return this;
    }

    /**
     * Method to append a CharSequence
     *
     * @param cs CharSequence
     * @return this
     */
    public AnsiColoredString append(CharSequence cs) {
        stringBuilder.append(cs);
        return this;
    }

    /**
     * Method to append an AnsiColor
     *
     * @param ansiColor ansiColor
     * @return this
     */
    public AnsiColoredString append(AnsiColor ansiColor) {
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
     * Method to get the length
     *
     * @return the length
     */
    public int length() {
        return stringBuilder.length();
    }

    /**
     * Method to return if Ansi colors are supported
     *
     * @return true if Ansi colors are supported, else false
     */
    public boolean isAnsiColorSupported() {
        return AnsiColor.isSupported();
    }

    /**
     * Method to build the Builder
     *
     * @return a String
     */
    public String build() {
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnsiColoredString that = (AnsiColoredString) o;
        return Objects.equals(stringBuilder.toString(), that.stringBuilder.toString())
                && Objects.equals(lastAnsiColor, that.lastAnsiColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringBuilder, lastAnsiColor);
    }
}
