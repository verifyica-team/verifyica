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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.MethodSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/**
 * Resolves {@link MethodSelector} instances into Verifyica test classes and methods.
 *
 * <p>A {@link MethodSelector} targets a specific Java method on a specific class. Verifyica discovery also
 * applies method ordering rules. The legacy behavior of this resolver is to include:</p>
 *
 * <ul>
 *   <li>All Verifyica test methods in the class, ordered according to {@link OrderSupport#orderMethods(java.util.Collection)},</li>
 *   <li>but only up to (and including) the specifically selected method.</li>
 * </ul>
 *
 * <p>This behavior allows a method selection to implicitly include earlier-ordered test methods (for example,
 * when tests are expected to be executed in a defined order).</p>
 *
 * <h3>Optimizations</h3>
 * <ul>
 *   <li>Uses simple loops (no lambda allocations, no {@code AtomicInteger}).</li>
 *   <li>Caches ordered test method lists per class within a single {@link #resolve} call to avoid repeated
 *       reflection scans when multiple selectors target the same class.</li>
 *   <li>Adds methods using index math rather than iterating and comparing on every add.</li>
 * </ul>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class MethodSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodSelectorResolver.class);

    /**
     * Creates a new resolver instance.
     */
    public MethodSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves {@link MethodSelector} selectors from the supplied discovery request.
     *
     * <p>For each selector, if the selected class matches {@link ResolverPredicates#TEST_CLASS} and the
     * selected method matches {@link ResolverPredicates#TEST_METHOD}, this resolver discovers the ordered
     * Verifyica test methods for that class and adds methods up to (and including) the selected method into
     * {@code classMethodSet}.</p>
     *
     * <p>If the selected method is not present in the discovered ordered method list (for example, if it does
     * not satisfy {@link ResolverPredicates#TEST_METHOD}), nothing is added for that selector.</p>
     *
     * @param engineDiscoveryRequest the discovery request
     * @param classMethodSet output map of test class → discovered test methods (mutated)
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Set<Method>> classMethodSet) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        List<MethodSelector> selectors = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        int selectorCount = selectors.size();

        // Cache ordered test methods per class for this resolve call.
        Map<Class<?>, List<Method>> orderedMethodsCache =
                new HashMap<Class<?>, List<Method>>(Math.max(16, selectorCount * 4));

        for (MethodSelector methodSelector : selectors) {
            Class<?> testClass = methodSelector.getJavaClass();
            Method selectedMethod = methodSelector.getJavaMethod();

            LOGGER.trace("testClass [%s] testMethod [%s]", testClass.getName(), selectedMethod.getName());

            if (!ResolverPredicates.TEST_CLASS.test(testClass)
                    || !ResolverPredicates.TEST_METHOD.test(selectedMethod)) {
                continue;
            }

            List<Method> orderedMethods = orderedMethodsCache.get(testClass);
            if (orderedMethods == null) {
                orderedMethods = OrderSupport.orderMethods(ClassSupport.findMethods(
                        testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
                orderedMethodsCache.put(testClass, orderedMethods);
            }

            int selectedIndex = indexOf(orderedMethods, selectedMethod);
            if (selectedIndex < 0) {
                // The selected method is not in the ordered Verifyica test method list.
                continue;
            }

            Set<Method> methodSet = classMethodSet.get(testClass);
            if (methodSet == null) {
                methodSet = new LinkedHashSet<Method>();
                classMethodSet.put(testClass, methodSet);
            }

            // Add methods up to (and including) the selected method.
            for (int i = 0; i <= selectedIndex; i++) {
                methodSet.add(orderedMethods.get(i));
            }
        }

        LOGGER.trace(
                "resolve() methodSelectorCount [%d] elapsedTime [%d] ms",
                selectorCount, stopwatch.elapsed().toMillis());
    }

    /**
     * Returns the index of {@code needle} within {@code haystack} using {@link Method#equals(Object)}.
     *
     * @param haystack the list to search
     * @param needle the method to find
     * @return index of {@code needle}, or {@code -1} if not found
     */
    private static int indexOf(List<Method> haystack, Method needle) {
        for (int i = 0; i < haystack.size(); i++) {
            if (haystack.get(i).equals(needle)) {
                return i;
            }
        }
        return -1;
    }
}
