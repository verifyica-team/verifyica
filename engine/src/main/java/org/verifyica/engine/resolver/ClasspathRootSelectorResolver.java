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

import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/**
 * Resolves {@link ClasspathRootSelector} instances into Verifyica test classes and methods.
 *
 * <p>For each selected classpath root, this resolver:</p>
 * <ol>
 *   <li>Finds all classes reachable from the root that match {@link ResolverPredicates#TEST_CLASS}.</li>
 *   <li>Applies JUnit Platform {@link ClassNameFilter} and {@link PackageNameFilter} constraints.</li>
 *   <li>For remaining classes, discovers methods matching {@link ResolverPredicates#TEST_METHOD} using a
 *       bottom-up hierarchy scan and applies Verifyica ordering.</li>
 *   <li>Populates {@code classMethodSet} with the discovered ordered methods.</li>
 * </ol>
 *
 * Optimizations
 * <ul>
 *   <li>Builds filter predicates once per {@link #resolve} call (not once per selector).</li>
 *   <li>Deduplicates repeated classpath roots to avoid rescanning the same root multiple times.</li>
 *   <li>Caches ordered test method lists per class within a single {@link #resolve} invocation to avoid
 *       redundant reflection scans when the same class is encountered via multiple roots.</li>
 *   <li>Uses simple loops instead of lambda/collector allocations.</li>
 * </ul>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class ClasspathRootSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathRootSelectorResolver.class);

    /**
     * Creates a new resolver instance.
     */
    public ClasspathRootSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves {@link ClasspathRootSelector} selectors from the supplied discovery request.
     *
     * <p>Discovered test methods are added to {@code classMethodSet}. If a class already exists in the map,
     * methods are merged into the existing set.</p>
     *
     * @param engineDiscoveryRequest the discovery request
     * @param classMethodSet output map of test class → discovered test methods (mutated)
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Set<Method>> classMethodSet) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        // Compose filters once per resolve call.
        Predicate<String> classNamePredicate = composeFilters(
                        engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class))
                .toPredicate();

        @SuppressWarnings("unchecked")
        List<PackageNameFilter> packageNameFilters = engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

        Predicate<String> packageNamePredicate =
                composeFilters(packageNameFilters).toPredicate();

        List<ClasspathRootSelector> selectors = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        int selectorCount = selectors.size();

        // Deduplicate identical roots.
        Set<URI> processedRoots = new HashSet<URI>(Math.max(16, selectorCount * 2));

        // Cache discovered ordered methods per class for this resolve call.
        Map<Class<?>, List<Method>> methodsCache =
                new HashMap<Class<?>, List<Method>>(Math.max(16, selectorCount * 16));

        int processedCount = 0;

        for (ClasspathRootSelector selector : selectors) {
            URI root = selector.getClasspathRoot();
            LOGGER.trace("classpathRoot [%s]", root);

            if (!processedRoots.add(root)) {
                continue;
            }
            processedCount++;

            List<Class<?>> testClasses = ClassSupport.findAllClasses(root, ResolverPredicates.TEST_CLASS);

            for (Class<?> testClass : testClasses) {
                // Defensive: Package can be null for some edge cases (e.g., default package).
                Package pkg = testClass.getPackage();
                String pkgName = (pkg != null) ? pkg.getName() : "";

                if (!classNamePredicate.test(testClass.getName()) || !packageNamePredicate.test(pkgName)) {
                    continue;
                }

                List<Method> orderedMethods = methodsCache.get(testClass);
                if (orderedMethods == null) {
                    orderedMethods = OrderSupport.orderMethods(ClassSupport.findMethods(
                            testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
                    methodsCache.put(testClass, orderedMethods);
                }

                Set<Method> methodSet = classMethodSet.computeIfAbsent(testClass, k -> new LinkedHashSet<Method>());

                methodSet.addAll(orderedMethods);
            }
        }

        LOGGER.trace(
                "resolve() classpathRootSelectors [%d] processedClasspathRoots [%d] elapsedTime [%d] ms",
                selectorCount, processedCount, stopwatch.elapsed().toMillis());
    }
}
