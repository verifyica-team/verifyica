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

package org.antublue.verifyica.engine.support;

import java.security.SecureRandom;
import java.util.Random;

/** Class to implement HashSupport */
public class HashSupport {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789";

    private static final Random RANDOM = new SecureRandom();

    /** Constructor */
    private HashSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to generate an alphanumeric hash
     *
     * @param length length
     * @return an alphanumeric hash
     */
    public static String alphanumeric(int length) {
        ArgumentSupport.isTrue(length > 0, "length is less than 1");

        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return stringBuilder.toString();
    }
}
