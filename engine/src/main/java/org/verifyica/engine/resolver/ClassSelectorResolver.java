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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/**
 * Resolves {@link ClassSelector} instances into Verifyica test classes and methods.
 *
 * <p>A {@link ClassSelector} targets a specific Java class. Verifyica discovery expands a class selection
 * to include eligible nested (inner) classes as well. For each selected root class, this resolver performs
 * a depth-first traversal over {@link Class#getDeclaredClasses()} and, for each class that satisfies
 * {@link ResolverPredicates#TEST_CLASS}, discovers methods that satisfy {@link ResolverPredicates#TEST_METHOD}.</p>
 *
 * <p>Discovered methods are:</p>
 * <ul>
 *   <li>Collected using {@link ClassSupport#findMethods(Class, java.util.function.Predicate, HierarchyTraversalMode)} with
 *       {@link HierarchyTraversalMode#BOTTOM_UP}.</li>
 *   <li>Ordered using {@link OrderSupport#orderMethods(java.util.List)} to preserve Verifyica method ordering semantics.</li>
 *   <li>Merged into the provided {@code classMethodSet} map (class → set of methods).</li>
 * </ul>
 *
 * <h3>Optimizations</h3>
 * <ul>
 *   <li>Uses simple loops (no lambda allocations, no {@code AtomicInteger}).</li>
 *   <li>Traverses class + inner classes iteratively to avoid deep recursion.</li>
 *   <li>Deduplicates classes across selectors and across nested traversals (a class is processed at most once per resolve call).</li>
 *   <li>Caches ordered test method lists per class within a single {@link #resolve} invocation to avoid repeated reflection scans.</li>
 * </ul>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class ClassSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSelectorResolver.class);

    /**
     * Creates a new resolver instance.
     */
    public ClassSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves {@link ClassSelector} selectors from the supplied discovery request.
     *
     * <p>For each selected root class, this method processes that class and all of its nested classes.
     * For every class that matches {@link ResolverPredicates#TEST_CLASS}, ordered Verifyica test methods are
     * discovered and merged into {@code classMethodSet}.</p>
     *
     * @param engineDiscoveryRequest the discovery request
     * @param classMethodSet output map of test class → discovered test methods (mutated)
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Set<Method>> classMethodSet) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        List<ClassSelector> selectors = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        int selectorCount = selectors.size();

        // Ensures we only process each class once per resolve call.
        Set<Class<?>> visited = new HashSet<Class<?>>(Math.max(16, selectorCount * 8));

        // Cache ordered methods per class for this resolve call.
        Map<Class<?>, List<Method>> methodsCache =
                new HashMap<Class<?>, List<Method>>(Math.max(16, selectorCount * 16));

        for (ClassSelector selector : selectors) {
            Class<?> root = selector.getJavaClass();
            traverseAndCollect(root, classMethodSet, visited, methodsCache);
        }

        LOGGER.trace(
                "resolve() classSelectors [%d] elapsedTime [%d] ms",
                selectorCount, stopwatch.elapsed().toMillis());
    }

    /**
     * Traverses {@code root} and its declared nested classes depth-first and collects test methods.
     *
     * <p>Traversal is performed iteratively and will skip any class already present in {@code visited}.</p>
     *
     * @param root the starting class
     * @param classMethodSet output map of test class → discovered methods
     * @param visited set of already processed classes
     * @param methodsCache per-resolve cache of ordered methods per class
     */
    private static void traverseAndCollect(
            Class<?> root,
            Map<Class<?>, Set<Method>> classMethodSet,
            Set<Class<?>> visited,
            Map<Class<?>, List<Method>> methodsCache) {

        Deque<Class<?>> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Class<?> clazz = stack.pop();

            if (!visited.add(clazz)) {
                continue;
            }

            // Push children first so traversal remains depth-first.
            Class<?>[] inner = clazz.getDeclaredClasses();
            for (int i = 0; i < inner.length; i++) {
                stack.push(inner[i]);
            }

            if (!ResolverPredicates.TEST_CLASS.test(clazz)) {
                continue;
            }

            List<Method> ordered = methodsCache.get(clazz);
            if (ordered == null) {
                ordered = OrderSupport.orderMethods(ClassSupport.findMethods(
                        clazz, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
                methodsCache.put(clazz, ordered);
            }

            Set<Method> methodSet = classMethodSet.get(clazz);
            if (methodSet == null) {
                methodSet = new LinkedHashSet<Method>();
                classMethodSet.put(clazz, methodSet);
            }

            methodSet.addAll(ordered);
        }
    }
}
