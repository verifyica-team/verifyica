/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.exception.TestClassDefinitionException;

/**
 * Class to implement OrderSupport
 */
public class OrderSupport {

    // Cache for method tags to avoid repeated annotation lookups.
    // Using WeakHashMap to avoid classloader leaks.
    private static final Map<Method, String> METHOD_TAG_CACHE = new WeakHashMap<>();

    /**
     * Constructor
     */
    private OrderSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to order a List of Classes by display name then Order annotation
     *
     * <p>Performance note: Order annotation values are pre-cached before sorting to avoid
     * repeated annotation lookups during comparison operations.</p>
     *
     * @param classes classes
     * @return an order List of classes
     */
    public static List<Class<?>> orderClasses(List<Class<?>> classes) {
        Precondition.notNull(classes, "classes is null");

        classes.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));

        // Pre-cache order values to avoid repeated annotation lookups during sorting
        Map<Class<?>, Integer> orderCache = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            Verifyica.Order annotation = clazz.getAnnotation(Verifyica.Order.class);
            orderCache.put(clazz, annotation != null ? annotation.value() : null);
        }

        classes.sort((c1, c2) -> {
            Integer orderValue1 = orderCache.get(c1);
            Integer orderValue2 = orderCache.get(c2);

            if (orderValue1 == null && orderValue2 == null) {
                return 0;
            } else if (orderValue1 == null) {
                return 1;
            } else if (orderValue2 == null) {
                return -1;
            }

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
     * <p>Performance note: Order annotation values are pre-cached before sorting to avoid
     * repeated annotation lookups during comparison operations.</p>
     *
     * @param methods methods
     * @return an ordered List of Methods
     */
    public static List<Method> orderMethods(List<Method> methods) {
        Precondition.notNull(methods, "methods is null");

        methods.sort(Comparator.comparing(DisplayNameSupport::getDisplayName));

        // Pre-cache order values to avoid repeated annotation lookups during sorting
        Map<Method, Integer> orderCache = new HashMap<>(methods.size());
        for (Method method : methods) {
            Verifyica.Order annotation = method.getAnnotation(Verifyica.Order.class);
            orderCache.put(method, annotation != null ? annotation.value() : null);
        }

        methods.sort((m1, m2) -> {
            Integer orderValue1 = orderCache.get(m1);
            Integer orderValue2 = orderCache.get(m2);

            if (orderValue1 == null && orderValue2 == null) {
                return 0;
            } else if (orderValue1 == null) {
                return 1;
            } else if (orderValue2 == null) {
                return -1;
            }

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
        int methodCount = methods.size();
        Map<String, Method> methodMap = new HashMap<>(methodCount);
        Map<String, Integer> originalOrder = new HashMap<>(methodCount);

        for (int i = 0; i < methodCount; i++) {
            Method method = methods.get(i);
            String tag = getMethodTag(method);
            methodMap.put(tag, method);
            originalOrder.put(tag, i);
        }

        // Create dependency graph
        Map<String, Set<String>> dependents = new HashMap<>(methodCount);
        Map<String, Set<String>> dependencies = new HashMap<>(methodCount);

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

        List<Method> result = new ArrayList<>(methodCount);
        Set<String> processed = new HashSet<>(methodCount);
        Set<String> processing = new HashSet<>(methodCount);

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
     *
     * <p>Performance note: Results are cached per method to avoid repeated annotation lookups.</p>
     */
    private static String getMethodTag(Method method) {
        synchronized (METHOD_TAG_CACHE) {
            String cached = METHOD_TAG_CACHE.get(method);
            if (cached != null) {
                return cached;
            }

            Verifyica.Tag tagAnnotation = method.getAnnotation(Verifyica.Tag.class);
            String tag = tagAnnotation != null ? tagAnnotation.value() : method.getName();
            METHOD_TAG_CACHE.put(method, tag);
            return tag;
        }
    }
}
