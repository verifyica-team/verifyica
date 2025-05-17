/*
 * Copyright (C) Verifyica project authors and contributors
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

    /** Empty Argument */
    Argument<Object> EMPTY = new Empty();

    @Override
    default String name() {
        return getName();
    }

    @Override
    String getName();

    /**
     * Method to get the Argument payload
     *
     * @return the Argument payload
     */
    default T payload() {
        return getPayload();
    }

    /**
     * Method to get the Argument payload
     *
     * @return the Argument payload
     */
    T getPayload();

    /**
     * Method to get the Argument payload
     *
     * @param type type
     * @return the Argument payload
     * @param <V> the return type
     */
    default <V> V payload(Class<V> type) {
        return getPayload(type);
    }

    /**
     * Method to get the Argument payload
     *
     * @param type type
     * @return the Argument payload
     * @param <V> the return type
     */
    default <V> V getPayload(Class<V> type) {
        notNull(type, "type is null");
        return type.cast(getPayload());
    }

    /**
     * Method to return if the Argument has a non-null payload
     *
     * @return true if the Argument payload is not null, else false
     */
    default boolean hasPayload() {
        return getPayload() != null;
    }

    /**
     * Method to create an Argument of type boolean
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Boolean> ofBoolean(boolean value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type byte
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Byte> ofByte(byte value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type char
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Character> ofChar(char value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type short
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Short> ofShort(short value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type int
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Integer> ofInt(int value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type long
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Long> ofLong(long value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type float
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Float> ofFloat(float value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type double
     *
     * @param value value
     * @return an Argument
     */
    static Argument<Double> ofDouble(double value) {
        return of(String.valueOf(value), value);
    }

    /**
     * Method to create an Argument of type String
     *
     * @param value value
     * @return an Argument
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
     * Method to create an Argument of type BigInteger
     *
     * @param value value
     * @return an Argument
     */
    static Argument<BigInteger> ofBigInteger(BigInteger value) {
        if (value == null) {
            return of("BigInteger=/null/", null);
        } else {
            return of(value.toString(), value);
        }
    }

    /**
     * Method to create an Argument of type BigDecimal
     *
     * @param value value
     * @return an Argument
     */
    static Argument<BigDecimal> ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return of("BigDecimal=/null/", null);
        } else {
            return of(value.toString(), value);
        }
    }

    /**
     * Method to create an Argument of type T
     *
     * @param name name
     * @param payload payload
     * @return an Argument
     * @param <T> type T
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
     * Method to validate an Object is not null, throwing an IllegalArgumentException if it is null
     *
     * @param object object
     * @param message message
     */
    static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method to validate a String is not null and not blank, throwing an IllegalArgumentException
     * if it is null or blank
     *
     * @param string string
     * @param nullMessage nullMessage
     * @param blankMessage blankMessage
     */
    static void notBlank(String string, String nullMessage, String blankMessage) {
        if (string == null) {
            throw new IllegalArgumentException(nullMessage);
        }

        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException(blankMessage);
        }
    }

    /** Class to implement Empty */
    class Empty implements Argument<Object> {

        private final String NAME = "---";

        /** Constructor */
        private Empty() {
            // INITIALLY BLANK
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Object getPayload() {
            return null;
        }

        @Override
        public <V> V getPayload(Class<V> type) {
            notNull(type, "type is null");
            return type.cast(null);
        }

        @Override
        public boolean hasPayload() {
            return false;
        }

        @Override
        public String toString() {
            return NAME;
        }
    }
}
