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

/** Class to implement ClassSupport */
public class ClassSupport {

    /** Constructor */
    private ClassSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return whether a Class has a default (empty) constructor
     *
     * @param clazz clazz
     * @return true if the Class has a default (empty) constructor, else false
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        ArgumentSupport.notNull(clazz, "clazz is null");

        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
