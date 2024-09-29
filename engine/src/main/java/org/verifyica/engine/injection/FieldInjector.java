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

package org.verifyica.engine.injection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.verifyica.engine.exception.EngineException;

/** Class to implement FieldInjector */
public class FieldInjector {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private FieldInjector() {
        // INTENTIONALLY Blank
    }

    /**
     * Method to inject an object into an object's fields
     *
     * @param target target
     * @param value value
     */
    public static void injectFields(Object target, Object value) {
        try {
            Class<?> clazz = target.getClass();
            if (clazz.getName().startsWith("java") || clazz.getName().startsWith("sun")) {
                return;
            }
            List<Field> fields = getAllFields(clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                if (field.isAnnotationPresent(Inject.class)
                        && field.getType().isAssignableFrom(value.getClass())
                        && field.get(target) == null) {
                    field.set(target, value);
                } else if (fieldType.isArray()) {
                    Object array = field.get(target);
                    if (array != null) {
                        int length = Array.getLength(array);
                        for (int i = 0; i < length; i++) {
                            Object element = Array.get(array, i);
                            if (element != null) {
                                injectFields(element, value);
                            }
                        }
                    }
                } else if (Iterable.class.isAssignableFrom(fieldType)) {
                    Iterable<?> iterable = (Iterable<?>) field.get(target);
                    if (iterable != null) {
                        for (Object element : iterable) {
                            if (element != null) {
                                injectFields(element, value);
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new EngineException("Exception injecting object", e);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> {
            List<Field> fields = new ArrayList<>();

            while (c != null) {
                Field[] declaredFields = c.getDeclaredFields();
                Collections.addAll(fields, declaredFields);
                c = c.getSuperclass();
            }

            return fields;
        });
    }
}
