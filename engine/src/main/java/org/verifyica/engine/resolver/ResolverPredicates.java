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

package org.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;

/** Class to implement ResolverPredicates */
public class ResolverPredicates {

    /** Predicate to filter class interceptor supplier methods */
    public static final Predicate<Method> CLASS_INTERCEPTOR_SUPPLIER = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.ClassInterceptorSupplier.class);
    };

    /** Predicate to filter argument supplier methods */
    public static final Predicate<Method> ARGUMENT_SUPPLIER_METHOD = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.ArgumentSupplier.class);
    };

    /** Predicate to filter test methods */
    public static final Predicate<Method> TEST_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && method.isAnnotationPresent(Verifyica.Test.class);
    };

    /** Predicate to filter test classes */
    public static final Predicate<Class<?>> TEST_CLASS = clazz -> {
        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && !clazz.isAnnotationPresent(Verifyica.Disabled.class)
                && hasDefaultConstructor(clazz)
                && !ClassSupport.findMethods(clazz, ARGUMENT_SUPPLIER_METHOD, HierarchyTraversalMode.BOTTOM_UP)
                        .isEmpty()
                && !ClassSupport.findMethods(clazz, TEST_METHOD, HierarchyTraversalMode.TOP_DOWN)
                        .isEmpty();
    };

    /** Predicate to filter prepare methods */
    public static final Predicate<Method> PREPARE_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.Prepare.class);
    };

    /** Predicate to filter before all methods */
    public static final Predicate<Method> BEFORE_ALL_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.BeforeAll.class);
    };

    /** Predicate to filter before each methods */
    public static final Predicate<Method> BEFORE_EACH_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.BeforeEach.class);
    };

    /** Predicate to filter after each methods */
    public static final Predicate<Method> AFTER_EACH_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.AfterEach.class);
    };

    /** Predicate to filter after all methods */
    public static final Predicate<Method> AFTER_ALL_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.AfterAll.class);
    };

    /** Predicate to filter conclude methods */
    public static final Predicate<Method> CONCLUDE_METHOD = method -> {
        int modifiers = method.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && Modifier.isPublic(modifiers)
                && method.getReturnType().equals(Void.TYPE)
                && !method.isAnnotationPresent(Verifyica.Disabled.class)
                && method.isAnnotationPresent(Verifyica.Conclude.class);
    };

    /** Constructor */
    private ResolverPredicates() {
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
