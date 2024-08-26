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

/** Class to implement AnsiColorString */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class AnsiColorString {

    /** Constructor */
    private AnsiColorString() {
        // INTENTIONALLY BLANK
    }

    /** Class to implement Builder */
    public static class Builder {

        private final StringBuilder stringBuilder;
        private AnsiColor lastColor;

        /** Constructor */
        public Builder() {
            this(null);
        }

        /**
         * Constructor
         *
         * @param ansiColor ansiColor
         */
        public Builder(AnsiColor ansiColor) {
            stringBuilder = new StringBuilder();

            if (ansiColor != null) {
                stringBuilder.append(ansiColor);
                lastColor = ansiColor;
            }
        }

        /**
         * Method to append a boolean
         *
         * @param b boolean
         * @return this
         */
        public Builder append(boolean b) {
            stringBuilder.append(b);
            return this;
        }

        /**
         * Method to append a boolean
         *
         * @param s short
         * @return this
         */
        public Builder append(short s) {
            stringBuilder.append(s);
            return this;
        }

        /**
         * Method to append a character
         *
         * @param c character
         * @return this
         */
        public Builder append(char c) {
            stringBuilder.append(c);
            return this;
        }

        /**
         * Method to append a character array
         *
         * @param chars character array
         * @return this
         */
        public Builder append(char[] chars) {
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
        public Builder append(char[] chars, int offset, int length) {
            stringBuilder.append(chars, offset, length);
            return this;
        }

        /**
         * Method to append an int
         *
         * @param i int
         * @return this
         */
        public Builder append(int i) {
            stringBuilder.append(i);
            return this;
        }

        /**
         * Method to append a long
         *
         * @param l long
         * @return this
         */
        public Builder append(long l) {
            stringBuilder.append(l);
            return this;
        }

        /**
         * Method to append a float
         *
         * @param f float
         * @return this
         */
        public Builder append(float f) {
            stringBuilder.append(f);
            return this;
        }

        /**
         * Method to append a double
         *
         * @param d double
         * @return this
         */
        public Builder append(double d) {
            stringBuilder.append(d);
            return this;
        }

        /**
         * Method to append a String
         *
         * @param s String
         * @return this
         */
        public Builder append(String s) {
            stringBuilder.append(s);
            return this;
        }

        /**
         * Method to append an Object
         *
         * @param o object
         * @return this
         */
        public Builder append(Object o) {
            stringBuilder.append(o);
            return this;
        }

        /**
         * Method to append a StringBuffer
         *
         * @param sb StringBuffer
         * @return this
         */
        public Builder append(StringBuffer sb) {
            stringBuilder.append(sb);
            return this;
        }

        /**
         * Method to append a CharSequence
         *
         * @param cs CharSequence
         * @return this
         */
        public Builder append(CharSequence cs) {
            stringBuilder.append(cs);
            return this;
        }

        /**
         * Method to set the current color.
         *
         * <p>If the color is null, defaults to AnsiColor.NONE.
         *
         * <p>If the color is equal to the last color, the method is a no-op
         *
         * @param ansiColor ansiColor
         * @return this
         */
        public Builder color(AnsiColor ansiColor) {
            if (ansiColor != lastColor) {
                if (stringBuilder.length() > 0) {
                    if (ansiColor != null) {
                        stringBuilder.append(ansiColor);
                        lastColor = ansiColor;
                    } else {
                        stringBuilder.append(AnsiColor.NONE);
                        lastColor = AnsiColor.NONE;
                    }
                } else if (ansiColor != null && ansiColor != AnsiColor.NONE) {
                    stringBuilder.append(ansiColor);
                    lastColor = ansiColor;
                }
            }

            return this;
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
    }
}
