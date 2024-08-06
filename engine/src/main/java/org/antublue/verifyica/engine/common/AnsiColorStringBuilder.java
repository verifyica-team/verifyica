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

package org.antublue.verifyica.engine.common;

/** Class to implement AnsiColorStringBuilder */
public class AnsiColorStringBuilder {

    private final StringBuilder stringBuilder;

    /** Constructor */
    public AnsiColorStringBuilder() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Method to set the current color. If the color is null, defaults to AnsiColor.RESET
     *
     * @param ansiColor ansiColor
     * @return this
     */
    public AnsiColorStringBuilder color(AnsiColor ansiColor) {
        if (ansiColor == null) {
            stringBuilder.append(AnsiColor.TEXT_RESET);
        } else {
            stringBuilder.append(ansiColor);
        }

        return this;
    }

    /**
     * Method to append a boolean
     *
     * @param b boolean
     * @return this
     */
    public AnsiColorStringBuilder append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    /**
     * Method to append a character
     *
     * @param c character
     * @return this
     */
    public AnsiColorStringBuilder append(char c) {
        stringBuilder.append(c);
        return this;
    }

    /**
     * Method to append a character array
     *
     * @param chars character array
     * @return this
     */
    public AnsiColorStringBuilder append(char[] chars) {
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
    public AnsiColorStringBuilder append(char[] chars, int offset, int length) {
        stringBuilder.append(chars, offset, length);
        return this;
    }

    /**
     * Method to append a double
     *
     * @param d double
     * @return this
     */
    public AnsiColorStringBuilder append(double d) {
        stringBuilder.append(d);
        return this;
    }

    /**
     * Method to append a float
     *
     * @param f float
     * @return this
     */
    public AnsiColorStringBuilder append(float f) {
        stringBuilder.append(f);
        return this;
    }

    /**
     * Method to append an int
     *
     * @param i int
     * @return this
     */
    public AnsiColorStringBuilder append(int i) {
        stringBuilder.append(i);
        return this;
    }

    /**
     * Method to append a long
     *
     * @param l long
     * @return this
     */
    public AnsiColorStringBuilder append(long l) {
        stringBuilder.append(l);
        return this;
    }

    /**
     * Method to append a string
     *
     * @param string String
     * @return this
     */
    public AnsiColorStringBuilder append(String string) {
        stringBuilder.append(string);
        return this;
    }

    /**
     * Method to append an object's toString()
     *
     * @param object object
     * @return this
     */
    public AnsiColorStringBuilder append(Object object) {
        stringBuilder.append(object.toString());
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
