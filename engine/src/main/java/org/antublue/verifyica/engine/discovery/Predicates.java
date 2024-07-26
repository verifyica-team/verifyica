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
import org.antublue.verifyica.api.extension.ClassExtension;
import org.antublue.verifyica.api.extension.engine.EngineExtension;
import org.antublue.verifyica.engine.extension.internal.InternalClassExtension;
import org.antublue.verifyica.engine.extension.internal.engine.InternalEngineExtension;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.MethodSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;

/** Class to implement Predicates */
public class Predicates {

    /** Predicate to filter test engine internal extension classes */
    public static final Predicate<Class<?>> ENGINE_INTERNAL_EXTENSION_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && InternalEngineExtension.class.isAssignableFrom(clazz)
                        && EngineExtension.class.isAssignableFrom(clazz);
            };

    /** Predicate to filter test engine extension classes */
    public static final Predicate<Class<?>> ENGINE_EXTERNAL_EXTENSION_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && !InternalEngineExtension.class.isAssignableFrom(clazz)
                        && EngineExtension.class.isAssignableFrom(clazz);
            };

    /** Predicate to filter class internal extension classes */
    public static final Predicate<Class<?>> CLASS_INTERNAL_EXTENSION_CLASS =
            clazz -> {
                int modifiers = clazz.getModifiers();
                return Modifier.isPublic(modifiers)
                        && !Modifier.isAbstract(modifiers)
                        && !Modifier.isStatic(modifiers)
                        && ClassSupport.hasDefaultConstructor(clazz)
                        && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                        && InternalClassExtension.class.isAssignableFrom(clazz)
                        && ClassExtension.class.isAssignableFrom(clazz);
            };

    /** Predicate to filter class extension supplier methods */
    public static final Predicate<Method> CLASS_EXTENSION_SUPPLIER =
            method -> {
                int modifiers = method.getModifiers();

                return Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        // TODO check return type
                        && method.getParameterCount() == 0
                        && method.isAnnotationPresent(Verifyica.ClassExtensionSupplier.class);
            };

    /** Predicate to filter argument supplier methods */
    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD =
            method -> {
                int modifiers = method.getModifiers();

                return Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        // TODO check return type
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
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
                        // && acceptsContext(method)
                        && !method.isAnnotationPresent(Verifyica.Disabled.class)
                        && method.isAnnotationPresent(Verifyica.Conclude.class);
            };

    /** Constructor */
    private Predicates() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to return if a Method accepts a Context
     *
     * @param method method
     * @return true if the Method accepts Context, else false
     */
    /*
    private static boolean acceptsContext(Method method) {
        if (method.getParameterCount() != 1) {
            return false;
        }

        return method.getParameters()[0].getType().equals(Context.class);
    }
    */
}
