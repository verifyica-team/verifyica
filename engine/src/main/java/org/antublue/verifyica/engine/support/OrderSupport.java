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
import java.util.Comparator;
import java.util.List;
import org.antublue.verifyica.api.Verifyica;

/** Class to implement OrderSupport */
public class OrderSupport {

    /** Constructor */
    private OrderSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to order a List of Classes by Order annotation then display name
     *
     * @param classes classes
     */
    public static void order(List<Class<?>> classes) {
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
        int order = Integer.MAX_VALUE;

        Verifyica.Order annotation = clazz.getAnnotation(Verifyica.Order.class);
        if (annotation != null) {
            order = annotation.order();
        }

        return order;
    }

    /**
     * Method to get the order annotation value
     *
     * @param method method
     * @return the order annotation value
     */
    public static int getOrder(Method method) {
        int order = Integer.MAX_VALUE;

        Verifyica.Order annotation = method.getAnnotation(Verifyica.Order.class);
        if (annotation != null) {
            order = annotation.order();
        }

        return order;
    }
}
