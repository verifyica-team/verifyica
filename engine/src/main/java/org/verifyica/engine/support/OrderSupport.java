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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
     * Method to get the Order annotation value
     *
     * @param clazz clazz
     * @return the order annotation value
     */
    public static int getOrder(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        int order = 0;

        Verifyica.Order annotation = clazz.getAnnotation(Verifyica.Order.class);
        if (annotation != null) {
            order = annotation.value();
        }

        return order;
    }

    /**
     * Method to order a List of Methods by display name then Order annotation
     *
     * <p>orders the List in place
     *
     * @param methods methods
     * @return an ordered List of Methods
     */
    public static List<Method> orderMethods(List<Method> methods) {
        Precondition.notNull(methods, "methods is null");

        methods.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));

        methods.sort((m1, m2) -> {
            Verifyica.Order o1 = m1.getAnnotation(Verifyica.Order.class);
            Verifyica.Order o2 = m2.getAnnotation(Verifyica.Order.class);

            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }

            int orderValue1 = o1.value();
            int orderValue2 = o2.value();

            if (orderValue1 == 0 && orderValue2 == 0) {
                return 0;
            } else if (orderValue1 == 0) {
                return -1;
            } else if (orderValue2 == 0) {
                return 1;
            } else {
                return Integer.compare(orderValue1, orderValue2);
            }
        });

        return methods;
    }

    /**
     * Method to order a Set of Methods by display name then Order annotation
     *
     * <p>orders the Set in place
     *
     * @param methods methods
     * @return an ordered Set of Methods
     */
    public static Set<Method> orderMethods(Set<Method> methods) {
        List<Method> list = orderMethods(new ArrayList<>(methods));

        methods.clear();
        methods.addAll(list);

        return methods;
    }
}
