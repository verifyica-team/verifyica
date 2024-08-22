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

package org.antublue.verifyica.engine.interceptor;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.engine.support.ClassSupport;

/** Class to implement InterceptorPredicates */
public class InterceptorPredicates {

    /** Predicate to filter autowired engine interceptors classes */
    public static final Predicate<Class<?>> AUTOWIRED_ENGINE_INTERCEPTOR_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && EngineInterceptor.class.isAssignableFrom(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && clazz.isAnnotationPresent(Verifyica.AutowiredInterceptor.class)
                        && ClassSupport.hasDefaultConstructor(clazz);
            };

    /** Predicate to filter autowired class interceptor classes */
    public static final Predicate<Class<?>> AUTOWIRED_CLASS_INTERCEPTOR_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && ClassInterceptor.class.isAssignableFrom(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && clazz.isAnnotationPresent(Verifyica.AutowiredInterceptor.class)
                        && ClassSupport.hasDefaultConstructor(clazz);
            };

    /** Constructor */
    private InterceptorPredicates() {
        // INTENTIONALLY BLANK
    }
}
