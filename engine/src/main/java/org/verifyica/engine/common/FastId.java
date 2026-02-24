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

import java.util.concurrent.ThreadLocalRandom;

/**
 * An immutable 128-bit identifier with fast random generation and UUID-style canonical string representation.
 *
 * <p>This class provides a more efficient alternative to UUID for generating unique identifiers.
 * It generates identifiers conforming to RFC 4122 version 4 (random UUID) with the IETF variant.
 *
 * <p>The string representation is 36 characters: 32 lowercase hexadecimal digits and 4 hyphens
 * in the standard UUID format {@code 8-4-4-4-12}.
 *
 * @see Comparable
 */
public final class FastId implements Comparable<FastId> {

    /**
     * Hexadecimal characters for converting to string representation.
     */
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * Masks and bits for setting the version and variant according to RFC 4122.
     */
    private static final long VERSION_4_MASK = 0xffffffffffff0ffFL;

    /**
     * The bits to set for version 4 (random UUID) in the most significant bits.
     */
    private static final long VERSION_4_BITS = 0x0000000000004000L;

    /**
     * Masks and bits for setting the IETF variant in the least significant bits.
     */
    private static final long IETF_VARIANT_MASK = 0x3fffffffffffffffL;

    /**
     * The bits to set for the IETF variant in the least significant bits (variant 2).
     */
    private static final long IETF_VARIANT_BITS = 0x8000000000000000L;

    /**
     * The most significant 64 bits of the identifier.
     */
    private final long msb;

    /**
     * The least significant 64 bits of the identifier.
     */
    private final long lsb;

    /**
     * Private constructor to enforce creation through static factory methods.
     *
     * @param msb the most significant 64 bits
     * @param lsb the least significant 64 bits
     */
    private FastId(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Creates a FastId from the raw 128-bit parts.
     *
     * @param msb the most significant 64 bits
     * @param lsb the least significant 64 bits
     * @return a new FastId instance with the specified bits
     */
    public static FastId from(long msb, long lsb) {
        return new FastId(msb, lsb);
    }

    /**
     * Generates a new random FastId conforming to RFC 4122 version 4 with IETF variant.
     *
     * @return a new FastId instance with randomly generated values
     */
    public static FastId randomFastId() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        long msb = rnd.nextLong();
        long lsb = rnd.nextLong();

        msb = (msb & VERSION_4_MASK) | VERSION_4_BITS;
        lsb = (lsb & IETF_VARIANT_MASK) | IETF_VARIANT_BITS;

        return new FastId(msb, lsb);
    }

    /**
     * Returns the most significant 64 bits of this identifier.
     *
     * @return the most significant 64 bits
     */
    public long getMostSignificantBits() {
        return msb;
    }

    /**
     * Returns the least significant 64 bits of this identifier.
     *
     * @return the least significant 64 bits
     */
    public long getLeastSignificantBits() {
        return lsb;
    }

    /**
     * Returns the string representation of this identifier in the standard UUID format.
     *
     * @return the string representation (e.g., "550e8400-e29b-41d4-a716-446655440000")
     */
    @Override
    public String toString() {
        final char[] out = new char[36];

        // 8-4-4-4-12
        writeHexLong(out, 0, msb >>> 32, 8);
        out[8] = '-';
        writeHexLong(out, 9, msb >>> 16, 4);
        out[13] = '-';
        writeHexLong(out, 14, msb, 4);
        out[18] = '-';
        writeHexLong(out, 19, lsb >>> 48, 4);
        out[23] = '-';
        writeHexLong(out, 24, lsb, 12);

        return new String(out);
    }

    /**
     * Writes the specified number of hexadecimal digits of the given value into the destination array.
     *
     * @param dest the destination character array
     * @param offset the starting offset in the array
     * @param value the value to write
     * @param digits the number of hexadecimal digits to write
     */
    private static void writeHexLong(char[] dest, int offset, long value, int digits) {
        for (int i = offset + digits - 1; i >= offset; i--) {
            dest[i] = HEX[((int) value) & 0xF];
            value >>>= 4;
        }
    }

    @Override
    public int compareTo(FastId other) {
        Precondition.notNull(other, "other is null");

        int cmp = (msb < other.msb) ? -1 : (msb > other.msb) ? 1 : 0;
        if (cmp != 0) {
            return cmp;
        }

        return (lsb < other.lsb) ? -1 : (lsb > other.lsb) ? 1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FastId)) return false;
        FastId other = (FastId) o;
        return msb == other.msb && lsb == other.lsb;
    }

    @Override
    public int hashCode() {
        // Standard UUID-style mixing
        long x = msb ^ lsb;
        return (int) (x ^ (x >>> 32));
    }
}
