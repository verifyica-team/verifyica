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
 * Immutable 128-bit identifier with fast random generation and UUID-style
 * (RFC 4122 version 4 / IETF variant) canonical string form.
 *
 * <p>The string form is 36 characters: 32 lowercase hex digits and 4 hyphens
 * in the pattern {@code 8-4-4-4-12}.</p>
 */
public final class FastId implements Comparable<FastId> {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    // RFC 4122: set version (4) and IETF variant (10xx...)
    private static final long VERSION_4_MASK = 0xffffffffffff0ffFL;
    private static final long VERSION_4_BITS = 0x0000000000004000L;

    private static final long IETF_VARIANT_MASK = 0x3fffffffffffffffL;
    private static final long IETF_VARIANT_BITS = 0x8000000000000000L;

    private final long msb;
    private final long lsb;

    private FastId(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Creates a {@link FastId} from raw 128-bit parts.
     *
     * @param msb most significant 64 bits
     * @param lsb least significant 64 bits
     * @return a FastId with the provided bits
     */
    public static FastId from(long msb, long lsb) {
        return new FastId(msb, lsb);
    }

    /**
     * Generates a new random FastId (RFC 4122 version 4 with IETF variant).
     *
     * @return a new FastId instance with random values
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
     * Returns the most significant 64 bits.
     *
     * @return the most significant 64 bits
     */
    public long getMostSignificantBits() {
        return msb;
    }

    /**
     * Returns the least significant 64 bits.
     *
     * @return the least significant 64 bits
     */
    public long getLeastSignificantBits() {
        return lsb;
    }

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
     * Writes the low {@code digits} hex digits of {@code value} into {@code dest}
     * ending at {@code offset + digits - 1}.
     *
     * <p>Uses lowercase hex.</p>
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
