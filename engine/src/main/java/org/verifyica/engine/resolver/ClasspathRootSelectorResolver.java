/*
 * Copyright (C) Verifyica project authors and contributors
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
 * Class to implement ClasspathRootSelectorResolver
 */
public class ClasspathRootSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathRootSelectorResolver.class);

    /**
     * Constructor
     */
    public ClasspathRootSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to resolve ClassPathSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodSet classMethodSet
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Set<Method>> classMethodSet) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        AtomicInteger classpathRootSelectorCount = new AtomicInteger();

        engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class).forEach(classpathRootSelector -> {
            classpathRootSelectorCount.incrementAndGet();

            LOGGER.trace("classpathRoot [%s]", classpathRootSelector.getClasspathRoot());

            List<Class<?>> testClasses = ClassSupport.findAllClasses(
                    classpathRootSelector.getClasspathRoot(), ResolverPredicates.TEST_CLASS);

            List<ClassNameFilter> classNameFilters = engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

            Predicate<String> classNamePredicate =
                    composeFilters(classNameFilters).toPredicate();

            List<? extends PackageNameFilter> packageNameFilters =
                    engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

            Predicate<String> packageNamePredicate =
                    composeFilters(packageNameFilters).toPredicate();

            testClasses.forEach(testClass -> {
                if (classNamePredicate.test(testClass.getName())
                        && packageNamePredicate.test(testClass.getPackage().getName())) {
                    classMethodSet
                            .computeIfAbsent(testClass, set -> new LinkedHashSet<>())
                            .addAll(OrderSupport.orderMethods(ClassSupport.findMethods(
                                    testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP)));
                }
            });
        });

        LOGGER.trace(
                "resolve() classpathRootSelectors [%d] elapsedTime [%d] ms",
                classpathRootSelectorCount.get(), stopwatch.elapsed().toMillis());
    }
}
