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

package org.antublue.verifyica.engine.discovery;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.MethodSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;

/** Class to implement Predicates */
public class Predicates {

    /** Predicate to filter discoverable engine interceptors classes */
    public static final Predicate<Class<?>> AUTOWIRED_ENGINE_INTERCEPTOR_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && EngineInterceptor.class.isAssignableFrom(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && clazz.isAnnotationPresent(Verifyica.AutowiredInterceptor.class);
            };

    /** Predicate to filter discoverable class interceptor classes */
    public static final Predicate<Class<?>> AUTOWIRED_CLASS_INTERCEPTOR_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && ClassInterceptor.class.isAssignableFrom(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && clazz.isAnnotationPresent(Verifyica.AutowiredInterceptor.class);
            };

    /** Predicate to filter class interceptor supplier methods */
    public static final Predicate<Method> CLASS_INTERCEPTOR_SUPPLIER =
            method -> {
                int modifiers = method.getModifiers();

                return Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 0
                        && method.isAnnotationPresent(Verifyica.ClassInterceptorSupplier.class);
            };

    /** Predicate to filter argument supplier methods */
    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 0
                        && method.isAnnotationPresent(Verifyica.ArgumentSupplier.class);
            };

    /** Predicate to filter test methods */
    public static final Predicate<Method> TEST_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.Test.class);
            };

    /** Predicate to filter test classes */
    public static final Predicate<Class<?>> TEST_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && !MethodSupport.findMethods(
                                        clazz,
                                        ARGUMENT_SUPPLIER_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP)
                                .isEmpty();
            };

    /** Predicate to filter prepare methods */
    public static final Predicate<Method> PREPARE_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.Prepare.class);
            };

    /** Predicate to filter before all methods */
    public static final Predicate<Method> BEFORE_ALL_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.BeforeAll.class);
            };

    /** Predicate to filter before each methods */
    public static final Predicate<Method> BEFORE_EACH_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.BeforeEach.class);
            };

    /** Predicate to filter after each methods */
    public static final Predicate<Method> AFTER_EACH_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.AfterEach.class);
            };

    /** Predicate to filter after all methods */
    public static final Predicate<Method> AFTER_ALL_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.AfterAll.class);
            };

    /** Predicate to filter conclude methods */
    public static final Predicate<Method> CONCLUDE_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return !Modifier.isAbstract(modifiers)
                        && Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.Conclude.class);
            };

    /** Constructor */
    private Predicates() {
        // INTENTIONALLY BLANK
    }
}
