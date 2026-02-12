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

package org.verifyica.api;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@code Argument} is a container that associates a name with a given payload.
 *
 * @param <T> the payload type
 */
public interface Argument<T> extends Named {

    /**
     * An empty {@code Argument} instance.
     */
    Argument<Object> EMPTY = new Empty();

    /**
     * Returns the name of this argument.
     *
     * @return the argument name
     */
    @Override
    String getName();

    /**
     * Returns the payload of this argument.
     *
     * @return the argument payload
     */
    T getPayload();

    /**
     * Returns the payload cast to the given type.
     *
     * @param type the expected payload type
     * @param <V> the return type
     * @return the payload cast to {@code type}
     * @throws IllegalArgumentException if {@code type} is {@code null}
     * @throws ClassCastException if the payload cannot be cast to {@code type}
     */
    default <V> V getPayloadAs(Class<V> type) {
        notNull(type, "type is null");
        return type.cast(getPayload());
    }

    /**
     * Returns whether this argument has a non-null payload.
     *
     * @return {@code true} if the payload is non-null, otherwise {@code false}
     */
    default boolean hasPayload() {
        return getPayload() != null;
    }

    /**
     * Creates a boolean argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Boolean> ofBoolean(boolean value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a byte argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Byte> ofByte(byte value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a char argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Character> ofChar(char value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a short argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Short> ofShort(short value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates an int argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Integer> ofInt(int value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a long argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Long> ofLong(long value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a float argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Float> ofFloat(float value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a double argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<Double> ofDouble(double value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Creates a string argument with special handling for {@code null} and empty values.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<String> ofString(String value) {
        if (value == null) {
            return of("String=/null/", null);
        } else if (value.isEmpty()) {
            return of("String=/empty/", "");
        } else {
            return of(value, value);
        }
    }

    /**
     * Creates a {@code BigInteger} argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<BigInteger> ofBigInteger(BigInteger value) {
        return value == null ? of("BigInteger=/null/", null) : of(value.toString(), value);
    }

    /**
     * Creates a {@code BigInteger} argument from a string.
     *
     * @param value the value
     * @return an {@code Argument} containing the parsed value
     * @throws NumberFormatException if the string is not a valid representation of a BigInteger
     */
    static Argument<BigInteger> ofBigInteger(String value) {
        return value == null ? of("BigInteger=/null/", null) : ofBigInteger(new BigInteger(value));
    }

    /**
     * Creates a {@code BigDecimal} argument.
     *
     * @param value the value
     * @return an {@code Argument} containing the value
     */
    static Argument<BigDecimal> ofBigDecimal(BigDecimal value) {
        return value == null ? of("BigDecimal=/null/", null) : of(value.toString(), value);
    }

    /**
     * Creates a {@code BigDecimal} argument from a string.
     *
     * @param value the value
     * @return an {@code Argument} containing the parsed value
     * @throws NumberFormatException if the string is not a valid representation of a BigDecimal
     */
    static Argument<BigDecimal> ofBigDecimal(String value) {
        return value == null ? of("BigDecimal=/null/", null) : ofBigDecimal(new BigDecimal(value));
    }

    /**
     * Creates an argument with the given name and payload.
     *
     * @param name the argument name
     * @param payload the payload
     * @param <T> the payload type
     * @return a new {@code Argument}
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    static <T> Argument<T> of(String name, T payload) {
        notBlank(name, "name is null", "name is blank");

        return new Argument<T>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public T getPayload() {
                return payload;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Validates that the given object is not {@code null}.
     *
     * @param object the object to validate
     * @param message the exception message
     * @throws IllegalArgumentException if {@code object} is {@code null}
     */
    static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Validates that the given string is not {@code null} and not blank.
     *
     * @param string the string to validate
     * @param nullMessage the exception message if {@code string} is {@code null}
     * @param blankMessage the exception message if {@code string} is blank
     * @throws IllegalArgumentException if validation fails
     */
    static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /**
     * Empty {@code Argument} implementation.
     */
    final class Empty implements Argument<Object> {

        private static final String NAME = "---";

        private Empty() {
            // intentionally empty
        }

        /**
         * Returns the empty argument name.
         *
         * @return the name
         */
        @Override
        public String getName() {
            return NAME;
        }

        /**
         * Returns {@code null}.
         *
         * @return {@code null}
         */
        @Override
        public Object getPayload() {
            return null;
        }

        /**
         * Returns the argument name.
         *
         * @return the name
         */
        @Override
        public String toString() {
            return NAME;
        }
    }
}
