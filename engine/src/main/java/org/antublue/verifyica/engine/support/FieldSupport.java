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

package org.antublue.verifyica.engine.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;

/** Class to implement FieldSupport */
public class FieldSupport {

    /** Constructor */
    private FieldSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to find fields of a Class
     *
     * @param clazz clazz
     * @param predicate predicate
     * @param hierarchyTraversalMode hierarchyTraversalMode
     * @return a List of Fields
     */
    public static List<Field> findFields(
            Class<?> clazz,
            Predicate<Field> predicate,
            HierarchyTraversalMode hierarchyTraversalMode) {
        return new ArrayList<>(
                ReflectionSupport.findFields(clazz, predicate, hierarchyTraversalMode));
    }

    /**
     * Method to set a Field value
     *
     * @param object object
     * @param field field
     * @param value value
     * @throws Throwable Throwable
     */
    public static void setField(Object object, Field field, Object value) throws Throwable {
        Preconditions.notNull(field, "field is null");

        if (value == null) {
            Class<?> fieldType = field.getType();
            if (fieldType.equals(boolean.class)) {
                field.set(object, false);
            } else if (fieldType.equals(byte.class)) {
                field.set(object, (byte) 0);
            } else if (fieldType.equals(char.class)) {
                field.set(object, (char) 0);
            } else if (fieldType.equals(short.class)) {
                field.set(object, (short) 0);
            } else if (fieldType.equals(int.class)) {
                field.set(object, 0);
            } else if (fieldType.equals(long.class)) {
                field.set(object, 0L);
            } else if (fieldType.equals(float.class)) {
                field.set(object, 0F);
            } else if (fieldType.equals(double.class)) {
                field.set(object, 0D);
            } else {
                field.set(object, null);
            }
        } else {
            if (field.getType().equals(String.class)) {
                field.set(object, value.toString());
            } else {
                field.set(object, value);
            }
        }
    }
}
