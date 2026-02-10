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

package org.verifyica.engine.support;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement HashSupport
 */
public class HashSupport {

    private static final String ALPHA_NUMERIC_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789";

    /**
     * Constructor
     */
    private HashSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to generate an alphanumeric hash
     *
     * @param length length
     * @return an alphanumeric hash
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
        if (hash.toLowerCase(Locale.US).contains("fail")) {
            hashBuilder.setLength(0);
            for (int i = 0; i < length; i++) {
                hashBuilder.append(ALPHA_NUMERIC_CHARACTERS.charAt(
                        ThreadLocalRandom.current().nextInt(ALPHA_NUMERIC_CHARACTERS.length())));
            }
            hash = hashBuilder.toString();
        }

        return hash;
    }
}
