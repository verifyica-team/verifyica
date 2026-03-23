/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.engine.support;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.verifyica.engine.common.Precondition;

/**
 * ObjectSupport provides utility methods for working with objects.
 */
@SuppressWarnings("unchecked")
public class ObjectSupport {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ObjectSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Creates an instance of a Class.
     *
     * @param clazz the class to instantiate
     * @param <T> the type of object to create
     * @return an instance of the specified class
     * @throws Throwable if instantiation fails
     */
    public static <T> T createObject(Class<?> clazz) throws Throwable {
        Precondition.notNull(clazz, "clazz is null");

        return (T) clazz.getConstructor().newInstance();
    }

    /**
     * Converts a List of Methods to a String representation.
     *
     * @param methods the methods to convert
     * @return a String representation of the Methods
     */
    public static String toString(Method... methods) {
        Precondition.notNull(methods, "methods is null");

        return toString(Arrays.stream(methods).collect(Collectors.toList()));
    }

    /**
     * Converts a List of Methods to a String representation.
     *
     * @param methods the methods to convert
     * @return a String representation of the Methods
     */
    public static String toString(List<Method> methods) {
        Precondition.notNull(methods, "methods is null");

        int size = methods.size();
        if (size == 0) {
            return "";
        }
        if (size == 1) {
            return methods.get(0).getName();
        }

        StringBuilder stringBuilder = new StringBuilder(size * 16);
        stringBuilder.append(methods.get(0).getName());
        for (int i = 1; i < size; i++) {
            stringBuilder.append(", ").append(methods.get(i).getName());
        }

        return stringBuilder.toString();
    }
}
