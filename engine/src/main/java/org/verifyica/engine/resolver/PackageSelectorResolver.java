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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.PackageSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/**
 * Resolves {@link PackageSelector} instances into Verifyica test classes and methods.
 *
 * <p>For each selected package, this resolver:</p>
 * <ol>
 *   <li>Finds all classes in the package matching {@link ResolverPredicates#TEST_CLASS}.</li>
 *   <li>For each test class, finds methods matching {@link ResolverPredicates#TEST_METHOD} (bottom-up hierarchy scan).</li>
 *   <li>Adds the discovered methods to {@code classMethodSet}.</li>
 * </ol>
 *
 * <h3>Optimizations</h3>
 * <ul>
 *   <li>Uses simple loops (no {@code AtomicInteger} / lambda allocations).</li>
 *   <li>Deduplicates repeated package selectors to avoid rescanning identical packages.</li>
 *   <li>Caches per-class discovered method lists within this resolve call to avoid redundant reflection scans
 *       when multiple package selectors contain the same class.</li>
 * </ul>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class PackageSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageSelectorResolver.class);

    /**
     * Creates a new resolver instance.
     */
    public PackageSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves {@link PackageSelector} selectors from the supplied discovery request.
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

        List<PackageSelector> selectors = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        int selectorCount = selectors.size();

        // Avoid rescanning the same package multiple times.
        Set<String> processedPackages = new HashSet<String>(Math.max(16, selectorCount * 2));

        // Cache discovered test methods per class within this resolve call.
        Map<Class<?>, List<Method>> methodsCache = new HashMap<Class<?>, List<Method>>(Math.max(16, selectorCount * 8));

        for (PackageSelector selector : selectors) {
            String packageName = selector.getPackageName();
            LOGGER.trace("packageName [%s]", packageName);

            if (!processedPackages.add(packageName)) {
                // Duplicate selector for the same package.
                continue;
            }

            List<Class<?>> testClasses = ClassSupport.findAllClasses(packageName, ResolverPredicates.TEST_CLASS);

            for (Class<?> testClass : testClasses) {
                // Find/compute discovered methods once per class for this resolver invocation.
                List<Method> discovered = methodsCache.get(testClass);
                if (discovered == null) {
                    discovered = ClassSupport.findMethods(
                            testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP);

                    // Preserve Verifyica method ordering for package-based discovery.
                    discovered = OrderSupport.orderMethods(discovered);
                    methodsCache.put(testClass, discovered);
                }

                Set<Method> methods = classMethodSet.get(testClass);
                if (methods == null) {
                    methods = new LinkedHashSet<Method>();
                    classMethodSet.put(testClass, methods);
                }

                methods.addAll(discovered);
            }
        }

        LOGGER.trace(
                "resolve() packageSelectorsCount [%d] elapsedTime [%d] ms",
                selectorCount, stopwatch.elapsed().toMillis());
    }
}
