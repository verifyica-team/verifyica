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

package org.verifyica.engine.support;

import java.lang.reflect.Method;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement DisplayNameSupport
 */
public class DisplayNameSupport {

    /**
     * Constructor
     */
    private DisplayNameSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to get a test class display name. If no name is declared, use the Class name
     *
     * @param clazz clazz
     * @return the display name
     */
    public static String getDisplayName(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        String displayName = clazz.getName();

        Verifyica.DisplayName annotation = clazz.getAnnotation(Verifyica.DisplayName.class);
        if (annotation != null) {
            String name = annotation.value();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }

    /**
     * Method to get a method display name. If no name is declared, use the Method name
     *
     * @param method method
     * @return the display name
     */
    public static String getDisplayName(Method method) {
        Precondition.notNull(method, "method is null");

        String displayName = method.getName();

        Verifyica.DisplayName annotation = method.getAnnotation(Verifyica.DisplayName.class);
        if (annotation != null) {
            String name = annotation.value();
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            }
        }

        return displayName;
    }
}
