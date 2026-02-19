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

package org.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;

/**
 * Class to implement ResolverPredicates
 */
public class ResolverPredicates {

    // Cache for TEST_CLASS predicate results to avoid repeated reflection scans.
    // Using WeakHashMap to avoid classloader leaks - classes are only cached while
    // they are reachable elsewhere.
    private static final Map<Class<?>, Boolean> TEST_CLASS_CACHE = new WeakHashMap<>();

    /**
     * Predicate to filter class interceptor supplier methods
     */
    public static final Predicate<Method> CLASS_INTERCEPTOR_SUPPLIER = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.ClassInterceptorSupplier.class);
    };

    /**
     * Predicate to filter argument supplier methods
     */
    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.ArgumentSupplier.class);
    };

    /**
     * Predicate to filter test methods
     */
    public static final Predicate<Method> TEST_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.Test.class);
    };

    /**
     * Predicate to filter test classes.
     *
     * <p>Performance note: Results are cached per class to avoid repeated expensive reflection scans
     * during classpath scanning. The cache uses weak references to avoid classloader leaks.</p>
     */
    public static final Predicate<Class<?>> TEST_CLASS = clazz -> {
        synchronized (TEST_CLASS_CACHE) {
            Boolean result = TEST_CLASS_CACHE.get(clazz);
            if (result != null) {
                return result;
            }

            int modifiers = clazz.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                TEST_CLASS_CACHE.put(clazz, Boolean.FALSE);
                return false;
            }

            if (clazz.isAnnotationPresent(Verifyica.Disabled.class)) {
                TEST_CLASS_CACHE.put(clazz, Boolean.FALSE);
                return false;
            }

            if (!hasDefaultConstructor(clazz)) {
                TEST_CLASS_CACHE.put(clazz, Boolean.FALSE);
                return false;
            }

            // Check for argument supplier first (usually fewer methods to scan)
            if (ClassSupport.findMethods(clazz, ARGUMENT_SUPPLIER_METHOD, HierarchyTraversalMode.BOTTOM_UP)
                    .isEmpty()) {
                TEST_CLASS_CACHE.put(clazz, Boolean.FALSE);
                return false;
            }

            // Finally check for test methods
            boolean isTestClass = !ClassSupport.findMethods(clazz, TEST_METHOD, HierarchyTraversalMode.TOP_DOWN)
                    .isEmpty();

            TEST_CLASS_CACHE.put(clazz, isTestClass);

            return isTestClass;
        }
    };

    /**
     * Predicate to filter prepare methods
     */
    public static final Predicate<Method> PREPARE_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.Prepare.class);
    };

    /**
     * Predicate to filter before all methods
     */
    public static final Predicate<Method> BEFORE_ALL_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.BeforeAll.class);
    };

    /**
     * Predicate to filter before each methods
     */
    public static final Predicate<Method> BEFORE_EACH_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.BeforeEach.class);
    };

    /**
     * Predicate to filter after each methods
     */
    public static final Predicate<Method> AFTER_EACH_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.AfterEach.class);
    };

    /**
     * Predicate to filter after all methods
     */
    public static final Predicate<Method> AFTER_ALL_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.AfterAll.class);
    };

    /**
     * Predicate to filter conclude methods
     */
    public static final Predicate<Method> CONCLUDE_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.Conclude.class);
    };

    /**
     * Constructor
     */
    private ResolverPredicates() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to return if a Class has a default constructor
     *
     * @param clazz clazz
     * @return true if the Class has a default constructor, otherwise false
     */
    @SuppressWarnings("PMD.UselessPureMethodCall")
    private static boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getDeclaredConstructor();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
