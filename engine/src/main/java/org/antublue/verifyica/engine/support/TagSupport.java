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

import java.lang.reflect.Method;
import org.antublue.verifyica.api.Verifyica;

/** Class to implement TagSupport */
public class TagSupport {

    /** Constructor */
    private TagSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a tag for a Class
     *
     * @param clazz clazz
     * @return a tag
     */
    public static String getTag(Class<?> clazz) {
        Verifyica.Tag annotation = clazz.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String value = annotation.tag();
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }

    /**
     * Method to get a tag for a Method
     *
     * @param method method
     * @return a tag
     */
    public static String getTag(Method method) {
        Verifyica.Tag annotation = method.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String value = annotation.tag();
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }
}
