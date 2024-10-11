/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.engine.interceptor;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.verifyica.api.ClassInterceptor;
import org.verifyica.api.EngineInterceptor;
import org.verifyica.api.Verifyica;

/** Class to implement InterceptorPredicates */
@SuppressWarnings("deprecation")
public class InterceptorPredicates {

    /** Predicate to filter autowired engine interceptors classes */
    public static final Predicate<Class<?>> AUTOWIRED_ENGINE_INTERCEPTOR_CLASS = clazz -> {
        int modifiers = clazz.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isAbstract(modifiers)
                && EngineInterceptor.class.isAssignableFrom(clazz)
                && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                && clazz.isAnnotationPresent(Verifyica.Autowired.class)
                && hasDefaultConstructor(clazz);
    };

    /** Predicate to filter autowired class interceptor classes */
    public static final Predicate<Class<?>> AUTOWIRED_CLASS_INTERCEPTOR_CLASS = clazz -> {
        int modifiers = clazz.getModifiers();
        return Modifier.isPublic(modifiers)
                && !Modifier.isAbstract(modifiers)
                && ClassInterceptor.class.isAssignableFrom(clazz)
                && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                && clazz.isAnnotationPresent(Verifyica.Autowired.class)
                && hasDefaultConstructor(clazz);
    };

    /** Constructor */
    private InterceptorPredicates() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if a Class has a default constructor
     *
     * @param clazz clazz
     * @return true if the Class has a default constructor, else false
     */
    private static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
