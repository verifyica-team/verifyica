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

package org.verifyica.engine.inject;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Named;
import org.verifyica.engine.exception.EngineException;

/**
 * A dependency injection utility for populating fields in target objects.
 *
 * <p>This class provides static methods for injecting values into fields of target objects.
 * It supports injection based on the JSR-330 {@link Inject} annotation, optionally combined
 * with {@link Named} for named dependency injection. The injector traverses the class
 * hierarchy to find fields in superclasses as well.
 *
 * <p>This is a simple, lightweight injection mechanism suitable for test framework use cases.
 *
 * @see javax.inject.Inject
 * @see javax.inject.Named
 */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class Injector {

    private static final String JAVA_PACKAGE = "java.";
    private static final String SUN_PACKAGE = "sun.";

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ReentrantLock FIELD_LOCK = new ReentrantLock(true);

    /**
     * Private constructor to prevent instantiation since this is a utility class.
     */
    private Injector() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Injects a value into fields annotated with both {@link Inject} and {@link Named}
     * with the specified name.
     *
     * @param name the name value from the Named annotation
     * the value to inject
     * @param value the value to inject into matching fields
     * @param target the target object to inject into
     */
    public static void inject(String name, Object value, Object target) {
        Class<?> clazz = target.getClass();
        String className = clazz.getName();

        while (!className.startsWith(JAVA_PACKAGE) && !className.startsWith(SUN_PACKAGE)) {
            for (Field field : getFields(clazz)) {
                Inject inject = field.getAnnotation(Inject.class);
                Named named = field.getAnnotation(Named.class);
                if (inject != null
                        && named != null
                        && named.value().equals(name)
                        && !Modifier.isStatic(field.getModifiers())
                        && field.getType().isAssignableFrom(value.getClass())) {
                    setField(field, target, value);
                }
            }

            clazz = clazz.getSuperclass();
            className = clazz.getName();
        }
    }

    /**
     * Injects a value into fields annotated with the specified annotation.
     *
     * @param annotation the annotation class to look for on fields
     * @param value the value to inject into matching fields
     * @param target the target object to inject into
     */
    public static void inject(Class<? extends Annotation> annotation, Object value, Object target) {
        Class<?> clazz = target.getClass();
        String className = clazz.getName();

        while (!className.startsWith(JAVA_PACKAGE) && !className.startsWith(SUN_PACKAGE)) {
            for (Field field : getFields(clazz)) {
                if (!Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(annotation)
                        && field.getType().isAssignableFrom(value.getClass())) {
                    setField(field, target, value);
                }
            }

            clazz = clazz.getSuperclass();
            className = clazz.getName();
        }
    }

    /**
     * Injects a value into static fields annotated with the specified annotation.
     *
     * @param annotation the annotation class to look for on static fields
     * @param value the value to inject into matching fields
     * @param target the target class to inject into
     */
    public static void inject(Class<? extends Annotation> annotation, Object value, Class<?> target) {
        Class<?> clazz = target;
        String className = clazz.getName();

        while (!className.startsWith(JAVA_PACKAGE) && !className.startsWith(SUN_PACKAGE)) {
            for (Field field : getFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(annotation)
                        && field.getType().isAssignableFrom(value.getClass())) {
                    setField(field, null, value);
                }
            }

            clazz = clazz.getSuperclass();
            className = clazz.getName();
        }
    }

    /**
     * Returns all declared fields for the specified class, including inherited fields
     * from the class hierarchy.
     *
     * @param clazz the class to get fields for
     * @return a list of fields for the class
     */
    private static List<Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> Arrays.asList(c.getDeclaredFields()));
    }

    /**
     * Sets a field to the specified value, using reflection to bypass access controls.
     *
     * @param field the field to set
     * @param target the target object (null for static fields)
     * @param value the value to set
     * @throws EngineException if an illegal access exception occurs
     */
    private static void setField(Field field, Object target, Object value) {
        FIELD_LOCK.lock();
        try {
            boolean originalAccessibility = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new EngineException(format("Exception injecting object into field [%s]", field.getName()), e);
            } finally {
                field.setAccessible(originalAccessibility);
            }
        } finally {
            FIELD_LOCK.unlock();
        }
    }
}
