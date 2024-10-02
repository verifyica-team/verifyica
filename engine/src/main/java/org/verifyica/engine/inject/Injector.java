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

package org.verifyica.engine.inject;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.verifyica.engine.exception.EngineException;

/** Class to implement Injector */
public class Injector {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Constructor
     */
    private Injector() {
        // INTENTIONALLY Blank
    }

    /**
     * Method to inject a value into named fields
     *
     * @param name name
     * @param value value
     * @param target target
     */
    public static void inject(String name, Object value, Object target) {
        try {
            Class<?> clazz = target.getClass();
            if (clazz.getName().startsWith("java") || clazz.getName().startsWith("sun")) {
                return;
            }
            for (Field field : getFields(clazz)) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject != null) {
                    Named named = field.getAnnotation(Named.class);
                    if (named != null && named.value().equals(name)) {
                        field.setAccessible(true);
                        field.set(target, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new EngineException(format("Exception injecting object into named [%s] field", name), e);
        }
    }

    private static List<Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, c -> Arrays.asList(c.getDeclaredFields()));
    }
}
