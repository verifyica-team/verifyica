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

package org.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.descriptor.TestClassTestDescriptor;
import org.verifyica.engine.exception.UncheckedClassNotFoundException;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/**
 * Resolves {@link UniqueIdSelector} instances into Verifyica test classes/methods.
 *
 * <p>JUnit Platform can supply {@link UniqueIdSelector} selectors that target:
 *
 * <ul>
 *   <li>A test class (discovery should include all matching Verifyica test methods)</li>
 *   <li>A specific argument index within a class (discovery should include methods plus the selected argument index)</li>
 * </ul>
 *
 * <p>This resolver extracts class names and (optionally) argument indices from the {@link UniqueId} segments.
 * It then populates:</p>
 *
 * <ul>
 *   <li>{@code classMethodSet}: mapping from test class → discovered Verifyica test methods</li>
 *   <li>{@code argumentIndexMap}: mapping from test class → selected argument indices</li>
 * </ul>
 *
 * Compatibility
 *
 * <p>Verifyica has historically used multiple segment type conventions. This implementation supports both:</p>
 * <ul>
 *   <li>Segment type {@code "class"} (as produced by {@code UniqueId.append("class", className)})</li>
 *   <li>Segment type {@code TestClassTestDescriptor.class.getName()} (legacy)</li>
 * </ul>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class UniqueIdSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIdSelectorResolver.class);

    /**
     * Creates a new resolver instance.
     */
    public UniqueIdSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves {@link UniqueIdSelector} selectors from the supplied discovery request.
     *
     * <p>Discovered test methods are added to {@code classMethodSet}. If a selector targets a specific argument,
     * its argument index is added to {@code argumentIndexMap} for the corresponding class.</p>
     *
     * @param engineDiscoveryRequest the discovery request
     * @param classMethodSet output map of test class → discovered test methods (mutated)
     * @param argumentIndexMap output map of test class → selected argument indices (mutated)
     * @throws UncheckedClassNotFoundException if a referenced class cannot be loaded
     * @throws NumberFormatException if an argument index segment cannot be parsed as an integer
     */
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodSet,
            Map<Class<?>, Set<Integer>> argumentIndexMap) {

        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();
        int selectorCount = 0;

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<UniqueIdSelector> selectors = engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
        for (UniqueIdSelector selector : selectors) {
            selectorCount++;

            UniqueId uniqueId = selector.getUniqueId();
            List<UniqueId.Segment> segments = uniqueId.getSegments();

            LOGGER.trace("uniqueId [%s]", uniqueId);

            // Fast-path: historically, Verifyica used a 3-segment format where the last segment is an argument index.
            // Example: <engine>/<class>/<argument>
            if (segments.size() == 3) {
                UniqueId.Segment classSegment = segments.get(1);
                UniqueId.Segment argumentSegment = segments.get(2);

                Class<?> testClass = loadClass(classLoader, classSegment.getValue());

                // Ensure methods are present for the class.
                addTestMethods(testClass, classMethodSet, true);

                // Record the selected argument index.
                addArgumentIndex(testClass, argumentSegment.getValue(), argumentIndexMap);
                continue;
            }

            // General path: scan segments and react to class segments.
            for (UniqueId.Segment segment : segments) {
                if (isClassSegment(segment)) {
                    Class<?> testClass = loadClass(classLoader, segment.getValue());
                    addTestMethods(testClass, classMethodSet, false);
                }
            }
        }

        LOGGER.trace(
                "resolve() uniqueIdSelectorCount [%d] elapsedTime [%d] ms",
                selectorCount, stopwatch.elapsed().toMillis());
    }

    /**
     * Returns {@code true} if the provided segment identifies a Java class.
     *
     * @param segment a unique id segment
     * @return {@code true} if the segment is a class segment
     */
    private static boolean isClassSegment(UniqueId.Segment segment) {
        String type = segment.getType();
        return "class".equals(type) || TestClassTestDescriptor.class.getName().equals(type);
    }

    /**
     * Loads a class using the given class loader and rethrows {@link ClassNotFoundException} as an unchecked exception.
     *
     * @param classLoader the class loader
     * @param className the fully qualified class name
     * @return the loaded class
     * @throws UncheckedClassNotFoundException if the class cannot be found
     */
    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            UncheckedClassNotFoundException.throwUnchecked(e);
            return null; // unreachable
        }
    }

    /**
     * Adds Verifyica test methods for the given class to {@code classMethodSet}.
     *
     * <p>Methods are discovered using {@link ResolverPredicates#TEST_METHOD} and
     * {@link HierarchyTraversalMode#BOTTOM_UP}. When {@code order} is {@code true}, methods are ordered via
     * {@link OrderSupport#orderMethods(Set)} (java.util.Collection)} before being added.</p>
     *
     * @param testClass the test class
     * @param classMethodSet output map of test class → method set
     * @param order whether to apply Verifyica method ordering before adding
     */
    private static void addTestMethods(Class<?> testClass, Map<Class<?>, Set<Method>> classMethodSet, boolean order) {
        Set<Method> methods = classMethodSet.get(testClass);
        if (methods == null) {
            methods = new LinkedHashSet<Method>();
            classMethodSet.put(testClass, methods);
        }

        List<Method> discovered =
                ClassSupport.findMethods(testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        if (order) {
            methods.addAll(OrderSupport.orderMethods(discovered));
        } else {
            methods.addAll(discovered);
        }
    }

    /**
     * Parses and records an argument index for a given class.
     *
     * @param testClass the test class
     * @param rawIndex the raw segment value
     * @param argumentIndexMap output map of test class → selected argument indices
     * @throws NumberFormatException if {@code rawIndex} is not an integer
     */
    private static void addArgumentIndex(
            Class<?> testClass, String rawIndex, Map<Class<?>, Set<Integer>> argumentIndexMap) {
        int index = Integer.parseInt(rawIndex);

        Set<Integer> indices = argumentIndexMap.get(testClass);
        if (indices == null) {
            indices = new LinkedHashSet<Integer>();
            argumentIndexMap.put(testClass, indices);
        }

        indices.add(index);
    }
}
