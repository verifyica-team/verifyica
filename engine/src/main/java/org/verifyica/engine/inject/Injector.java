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

package org.verifyica.engine.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.verifyica.engine.exception.EngineException;

/** Class to implement Injector */
@SuppressWarnings({"deprecation", "PMD.AvoidAccessibilityAlteration"})
public class Injector {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private Injector() {
        // INTENTIONALLY Blank
    }

    /**
     * Method to inject a value into annotated named fields
     *
     * @param name name
     * @param value value
     * @param target target
     */
    public static void inject(String name, Object value, Object target) {
        Class<?> clazz = target.getClass();

        while (clazz != null
                && !clazz.getName().startsWith("java")
                && !clazz.getName().startsWith("sun")) {
            for (Field field : getFields(clazz)) {
                Inject inject = field.getAnnotation(Inject.class);
                Named named = field.getAnnotation(Named.class);
                if (inject != null
                        && named != null
                        && named.value().equals(name)
                        && !Modifier.isStatic(field.getModifiers())
                        && field.getType().isAssignableFrom(value.getClass())) {
                    injectField(field, target, value);
                }
            }

            clazz = clazz.getSuperclass();
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

        while (clazz != null
                && !clazz.getName().startsWith("java")
                && !clazz.getName().startsWith("sun")) {
            for (Field field : getFields(clazz)) {
                if (!Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(annotation)
                        && field.getType().isAssignableFrom(value.getClass())) {
                    injectField(field, target, value);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Method to inject a value into annotated fields
     *
     * @param annotation annotation
     * @param value value
     * @param target target
     */
    public static void inject(Class<? extends Annotation> annotation, Object value, Class<?> target) {
        Class<?> clazz = target;

        while (clazz != null
                && !clazz.getName().startsWith("java")
                && !clazz.getName().startsWith("sun")) {
            for (Field field : getFields(clazz)) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(annotation)
                        && field.getType().isAssignableFrom(value.getClass())) {
                    injectField(field, null, value);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Method to inject a field
     *
     * @param field field
     * @param target target (null for static field)
     * @param value value
     */
    private static void injectField(Field field, Object target, Object value) {
        synchronized (field) {
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
}
