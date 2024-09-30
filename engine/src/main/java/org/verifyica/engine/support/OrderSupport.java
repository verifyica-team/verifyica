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

package org.verifyica.engine.support;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;

/** Class to implement OrderSupport */
public class OrderSupport {

    /** Constructor */
    private OrderSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to order a List of Classes by display name then Order annotation
     *
     * @param classes classes
     */
    public static void orderClasses(List<Class<?>> classes) {
        Precondition.notNull(classes, "classes is null");

        classes.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));
        classes.sort(Comparator.comparingInt(OrderSupport::getOrder));
    }

    /**
     * Method to get the order annotation value
     *
     * @param clazz clazz
     * @return the order annotation value
     */
    public static int getOrder(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        int order = 0;

        Verifyica.Order annotation = clazz.getAnnotation(Verifyica.Order.class);
        if (annotation != null) {
            order = annotation.order();
        }

        return order;
    }

    /**
     * Method to order a List of Methods by display name then Order annotation
     *
     * @param methods methods
     */
    public static void orderMethods(List<Method> methods) {
        Precondition.notNull(methods, "methods is null");

        methods.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));
        methods.sort(Comparator.comparingInt(OrderSupport::getOrder));
    }

    /**
     * Method to get the order annotation value
     *
     * @param method method
     * @return the order annotation value
     */
    public static int getOrder(Method method) {
        Precondition.notNull(method, "method is null");

        Verifyica.Order orderAnnotation = method.getAnnotation(Verifyica.Order.class);

        if (orderAnnotation == null) {
            return Integer.MAX_VALUE;
        } else {
            return orderAnnotation.order();
        }
    }
}
