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

package org.verifyica.engine.inject;

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

/** Class to implement Injector */
@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
public class Injector {

    private static final String JAVA_PACKAGE = "java.";
    private static final String SUN_PACKAGE = "sun.";

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ReentrantLock FIELD_LOCK = new ReentrantLock(true);

    /**
     * Constructor
     */
    private Injector() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to inject a value into named annotated fields
     *
     * @param name name
     * @param value value
     * @param target target
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
     * Method to inject a value into annotated fields
     *
     * @param annotation annotation
     * @param value value
     * @param target target
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
     * Method to inject a value into annotated static fields
     *
     * @param annotation annotation
     * @param value value
     * @param target target
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
     * Method to get all fields for a class
     *
     * @param clazz clazz
     * @return a List of fields for a class
     */
    private static List<Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> Arrays.asList(c.getDeclaredFields()));
    }

    /**
     * Method to inject a field
     *
     * @param field field
     * @param target target (null for static field)
     * @param value value
     */
    private static void setField(Field field, Object target, Object value) {
        FIELD_LOCK.lock();
        try {
            boolean originalAccessibility = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(target, value);
            } catch (IllegalAccessException e) {
                throw new EngineException(
                        String.format("Exception injecting object into field [%s]", field.getName()), e);
            } finally {
                field.setAccessible(originalAccessibility);
            }
        } finally {
            FIELD_LOCK.unlock();
        }
    }
}
