/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.engine.support;

import java.util.concurrent.ThreadLocalRandom;
import org.verifyica.engine.common.Precondition;

/**
 * HashSupport provides utility methods for generating hashes.
 */
public class HashSupport {

    /**
     * The set of alphanumeric characters used for generating hashes.
     */
    private static final String ALPHA_NUMERIC_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private HashSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Generates an alphanumeric hash.
     *
     * @param length the length of the hash to generate
     * @return an alphanumeric hash of the specified length
     */
    public static String alphanumeric(int length) {
        Precondition.isTrue(length > 0, "length is less than 1");

        StringBuilder hashBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            hashBuilder.append(ALPHA_NUMERIC_CHARACTERS.charAt(
                    ThreadLocalRandom.current().nextInt(ALPHA_NUMERIC_CHARACTERS.length())));
        }

        String hash = hashBuilder.toString();

        // Check and retry once if necessary (bounded retry to avoid infinite loop)
        if (containsFailCaseInsensitive(hash)) {
            hashBuilder.setLength(0);
            for (int i = 0; i < length; i++) {
                hashBuilder.append(ALPHA_NUMERIC_CHARACTERS.charAt(
                        ThreadLocalRandom.current().nextInt(ALPHA_NUMERIC_CHARACTERS.length())));
            }
            hash = hashBuilder.toString();
        }

        return hash;
    }

    /**
     * Checks if a string contains "fail" (case-insensitive) without creating a new lowercased string.
     *
     * @param s the string to check
     * @return true if the string contains "fail", false otherwise
     */
    private static boolean containsFailCaseInsensitive(String s) {
        int len = s.length();
        int failLen = 4;
        if (len < failLen) {
            return false;
        }

        for (int i = 0; i <= len - failLen; i++) {
            if (equalsIgnoreCase(s, i, "fail", 0, failLen)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares a substring of one string with a substring of another (case-insensitive).
     *
     * @param s1 the first string
     * @param start1 the start index in the first string
     * @param s2 the second string
     * @param start2 the start index in the second string
     * @param length the number of characters to compare
     * @return true if the substrings are equal ignoring case
     */
    private static boolean equalsIgnoreCase(String s1, int start1, String s2, int start2, int length) {
        for (int i = 0; i < length; i++) {
            if (Character.toLowerCase(s1.charAt(start1 + i)) != s2.charAt(start2 + i)) {
                return false;
            }
        }

        return true;
    }
}
