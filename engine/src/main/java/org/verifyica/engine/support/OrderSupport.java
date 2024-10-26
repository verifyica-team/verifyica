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

package org.verifyica.engine.support;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.exception.TestClassDefinitionException;

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
     * @return an order List of classes
     */
    public static List<Class<?>> orderClasses(List<Class<?>> classes) {
        Precondition.notNull(classes, "classes is null");

        classes.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));

        classes.sort((c1, c2) -> {
            Verifyica.Order o1 = c1.getAnnotation(Verifyica.Order.class);
            Verifyica.Order o2 = c2.getAnnotation(Verifyica.Order.class);

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

        return classes;
    }

    /**
     * Method to order a Set of Classes by display name then Order annotation
     *
     * <p>orders the Set in place
     *
     * @param classes classes
     * @return an ordered Set of Methods
     */
    public static Set<Class<?>> orderClasses(Set<Class<?>> classes) {
        List<Class<?>> list = orderClasses(new ArrayList<>(classes));

        classes.clear();
        classes.addAll(list);

        return classes;
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

        orderMethodsByDependencies(methods);

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

    /**
     * Orders methods based on their DependsOn annotations while maintaining stability.
     * Methods with the same dependencies maintain their original relative order.
     *
     * @param methods List of methods to order
     * @return Ordered list of methods with dependency groups
     * @throws TestClassDefinitionException if circular dependencies are detected
     */
    private static List<Method> orderMethodsByDependencies(List<Method> methods) {
        // Create map of method name to Method object and original position
        Map<String, Method> methodMap = new HashMap<>();
        Map<String, Integer> originalOrder = new HashMap<>();

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            String tag = getMethodTag(method);
            methodMap.put(tag, method);
            originalOrder.put(tag, i);
        }

        // Create dependency graph
        Map<String, Set<String>> dependents = new HashMap<>();
        Map<String, Set<String>> dependencies = new HashMap<>();

        // Initialize graphs
        methods.forEach(method -> {
            String methodTag = getMethodTag(method);
            dependents.put(methodTag, new HashSet<>());
            dependencies.put(methodTag, new HashSet<>());
        });

        // Build the dependency relationships
        for (Method method : methods) {
            Verifyica.DependsOn[] dependsOnAnnotations = method.getAnnotationsByType(Verifyica.DependsOn.class);

            if (dependsOnAnnotations != null && dependsOnAnnotations.length > 0) {
                String dependent = getMethodTag(method);

                for (Verifyica.DependsOn dependency : dependsOnAnnotations) {
                    String dependencyTag = dependency.value();
                    if (!methodMap.containsKey(dependencyTag)) {
                        throw new TestClassDefinitionException(
                                format("Dependency tag [%s] not found for method tag [%s]", dependencyTag, dependent));
                    }
                    dependents.get(dependencyTag).add(dependent);
                    dependencies.get(dependent).add(dependencyTag);
                }
            }
        }

        List<Method> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        Set<String> processing = new HashSet<>();

        // Process methods in original order, grouping dependencies
        for (Method method : methods) {
            String methodTag = getMethodTag(method);
            // If method has no dependencies, process it and its dependents
            if (!processed.contains(methodTag) && dependencies.get(methodTag).isEmpty()) {
                processMethodAndDependents(
                        methodTag, methodMap, dependents, dependencies, processed, processing, result, originalOrder);
            }
        }

        // Handle any remaining methods
        for (Method method : methods) {
            String methodTag = getMethodTag(method);
            if (!processed.contains(methodTag)) {
                processMethodAndDependents(
                        methodTag, methodMap, dependents, dependencies, processed, processing, result, originalOrder);
            }
        }

        // Verify all methods were processed
        if (result.size() != methods.size()) {
            throw new TestClassDefinitionException("Circular dependency detected in method ordering");
        }

        methods.clear();
        methods.addAll(result);

        return result;
    }

    /**
     * Recursively processes a method and all its dependent methods.
     * Methods with the same dependencies are processed in their original order.
     */
    private static void processMethodAndDependents(
            String methodTag,
            Map<String, Method> methodMap,
            Map<String, Set<String>> dependents,
            Map<String, Set<String>> dependencies,
            Set<String> processed,
            Set<String> processing,
            List<Method> result,
            Map<String, Integer> originalOrder) {

        // Check for circular dependencies
        if (processing.contains(methodTag)) {
            throw new TestClassDefinitionException(
                    format("Circular dependency detected involving method tag [%s]", methodTag));
        }

        // Skip if already processed
        if (processed.contains(methodTag)) {
            return;
        }

        processing.add(methodTag);

        // Process all dependencies first
        for (String dependency : dependencies.get(methodTag)) {
            if (!processed.contains(dependency)) {
                processMethodAndDependents(
                        dependency, methodMap, dependents, dependencies, processed, processing, result, originalOrder);
            }
        }

        // Add current method
        result.add(methodMap.get(methodTag));
        processed.add(methodTag);

        // Find all immediate dependents that have all dependencies processed
        List<String> readyDependents = dependents.get(methodTag).stream()
                .filter(dependent -> !processed.contains(dependent)
                        && dependencies.get(dependent).stream().allMatch(processed::contains))
                .sorted((a, b) -> {
                    // If dependencies are the same, maintain original order
                    Set<String> depsA = dependencies.get(a);
                    Set<String> depsB = dependencies.get(b);
                    if (depsA.equals(depsB)) {
                        return originalOrder.get(a).compareTo(originalOrder.get(b));
                    }
                    // Otherwise order by dependency sets
                    return depsA.size() != depsB.size()
                            ? Integer.compare(depsA.size(), depsB.size())
                            : originalOrder.get(a).compareTo(originalOrder.get(b));
                })
                .collect(Collectors.toList());

        // Process ready dependents in order
        for (String dependent : readyDependents) {
            processMethodAndDependents(
                    dependent, methodMap, dependents, dependencies, processed, processing, result, originalOrder);
        }

        processing.remove(methodTag);
    }

    /**
     * Gets the tag associated with a method, which is used for dependency matching.
     * If no tag annotation is present, returns the method name.
     */
    private static String getMethodTag(Method method) {
        Verifyica.Tag tagAnnotation = method.getAnnotation(Verifyica.Tag.class);
        return tagAnnotation != null ? tagAnnotation.value() : method.getName();
    }
}
