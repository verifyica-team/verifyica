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
 * Immutable 128-bit ID with fast random generation and UUID-style string form.
 */
public final class FastId implements Comparable<FastId> {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private final long msb;
    private final long lsb;

    /**
     * Constructor
     *
     * @param msb the most significant bits of the ID
     * @param lsb the least significant bits of the ID
     */
    private FastId(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Generates a new random FastId (version 4 UUID-style).
     *
     * @return a new FastId instance with random values
     */
    public static FastId randomFastId() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        long msb = rnd.nextLong();
        long lsb = rnd.nextLong();

        // Set the version to 4 (random)
        msb = (msb & 0xffffffffffff0ffFL) | 0x0000000000004000L;
        // Set the variant to IETF variant
        lsb = (lsb & 0x3fffffffffffffffL) | 0x8000000000000000L;

        return new FastId(msb, lsb);
    }

    @Override
    public String toString() {
        char[] out = new char[36];
        writeHex(out, 0, (msb >>> 32) & 0xffffffffL, 8);
        out[8] = '-';
        writeHex(out, 9, (msb >>> 16) & 0xffffL, 4);
        out[13] = '-';
        writeHex(out, 14, msb & 0xffffL, 4);
        out[18] = '-';
        writeHex(out, 19, (lsb >>> 48) & 0xffffL, 4);
        out[23] = '-';
        writeHex(out, 24, lsb & 0xffffffffffffL, 12);
        return new String(out);
    }

    private static void writeHex(char[] dest, int offset, long value, int digits) {
        for (int i = digits - 1; i >= 0; i--) {
            dest[offset + i] = HEX[(int) (value & 0xF)];
            value >>>= 4;
        }
    }

    @Override
    public int compareTo(FastId other) {
        int cmp = Long.compare(this.msb, other.msb);
        return (cmp != 0) ? cmp : Long.compare(this.lsb, other.lsb);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FastId)) return false;
        FastId other = (FastId) obj;
        return this.msb == other.msb && this.lsb == other.lsb;
    }

    @Override
    public int hashCode() {
        return (int) (msb ^ (msb >>> 32) ^ lsb ^ (lsb >>> 32));
    }
}
