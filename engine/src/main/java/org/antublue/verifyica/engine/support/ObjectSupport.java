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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.antublue.verifyica.engine.common.Precondition;

/** Class to implement ObjectSupport */
@SuppressWarnings("unchecked")
public class ObjectSupport {

    /** Constructor */
    private ObjectSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create an instance of a Class
     *
     * @param clazz clazz
     * @param <T> T
     * @return an Object of type T
     * @throws Throwable Throwable
     */
    public static <T> T createObject(Class<?> clazz) throws Throwable {
        Precondition.notNull(clazz, "clazz is null");

        return (T) clazz.getConstructor().newInstance();
    }

    /**
     * Method to convert a List Methods to a String representation
     *
     * @param methods methods
     * @return a String representation of the Methods
     */
    public static String toString(Method... methods) {
        Precondition.isTrue(methods != null, "methods is null");

        return toString(Arrays.stream(methods).collect(Collectors.toList()));
    }

    /**
     * Method to convert a List Methods to a String representation
     *
     * @param methods methods
     * @return a String representation of the Methods
     */
    public static String toString(List<Method> methods) {
        Precondition.notNull(methods, "methods is null");
        Precondition.isTrue(!methods.isEmpty(), "methods is empty");

        StringBuilder stringBuilder = new StringBuilder();

        Iterator<Method> iterator = methods.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next().getName());
            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        return stringBuilder.toString();
    }
}
