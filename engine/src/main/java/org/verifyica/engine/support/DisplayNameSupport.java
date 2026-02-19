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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement DisplayNameSupport
 */
public class DisplayNameSupport {

    // Caches for display names to avoid repeated annotation lookups.
    // Using WeakHashMap to avoid classloader leaks.
    private static final Map<Class<?>, String> CLASS_DISPLAY_NAME_CACHE = new WeakHashMap<>();
    private static final Map<Method, String> METHOD_DISPLAY_NAME_CACHE = new WeakHashMap<>();

    /**
     * Constructor
     */
    private DisplayNameSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to get a test class display name. If no name is declared, use the Class name
     *
     * <p>Performance note: Results are cached per class to avoid repeated annotation lookups.</p>
     *
     * @param clazz clazz
     * @return the display name
     */
    public static String getDisplayName(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        synchronized (CLASS_DISPLAY_NAME_CACHE) {
            String cached = CLASS_DISPLAY_NAME_CACHE.get(clazz);
            if (cached != null) {
                return cached;
            }

            String displayName = clazz.getName();

            Verifyica.DisplayName annotation = clazz.getAnnotation(Verifyica.DisplayName.class);
            if (annotation != null) {
                String name = annotation.value();
                if (name != null) {
                    String trimmed = name.trim();
                    if (!trimmed.isEmpty()) {
                        displayName = trimmed;
                    }
                }
            }

            CLASS_DISPLAY_NAME_CACHE.put(clazz, displayName);
            return displayName;
        }
    }

    /**
     * Method to get a method display name. If no name is declared, use the Method name
     *
     * <p>Performance note: Results are cached per method to avoid repeated annotation lookups.</p>
     *
     * @param method method
     * @return the display name
     */
    public static String getDisplayName(Method method) {
        Precondition.notNull(method, "method is null");

        synchronized (METHOD_DISPLAY_NAME_CACHE) {
            String cached = METHOD_DISPLAY_NAME_CACHE.get(method);
            if (cached != null) {
                return cached;
            }

            String displayName = method.getName();

            Verifyica.DisplayName annotation = method.getAnnotation(Verifyica.DisplayName.class);
            if (annotation != null) {
                String name = annotation.value();
                if (name != null) {
                    String trimmed = name.trim();
                    if (!trimmed.isEmpty()) {
                        displayName = trimmed;
                    }
                }
            }

            METHOD_DISPLAY_NAME_CACHE.put(method, displayName);
            return displayName;
        }
    }
}
