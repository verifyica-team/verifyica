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

package org.antublue.verifyica.engine.resolver;

import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.antublue.verifyica.engine.common.Stopwatch;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassSupport;
import org.antublue.verifyica.engine.support.HierarchyTraversalMode;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;

/** Class to implement ClasspathRootSelectorResolver */
public class ClasspathRootSelectorResolver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClasspathRootSelectorResolver.class);

    /** Constructor */
    public ClasspathRootSelectorResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve ClassPathSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolve()");

        Stopwatch stopWatch = new Stopwatch();

        AtomicInteger classpathRootSelectorCount = new AtomicInteger();

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> {
                            classpathRootSelectorCount.incrementAndGet();

                            LOGGER.trace(
                                    "classpathRoot [%s]", classpathRootSelector.getClasspathRoot());

                            List<Class<?>> testClasses =
                                    ClassSupport.findAllClasses(
                                            classpathRootSelector.getClasspathRoot(),
                                            ResolverPredicates.TEST_CLASS);

                            List<ClassNameFilter> classNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

                            /*
                             * Code remove default ConsoleLauncher test-classname filter
                             */
                            /*
                            if (classNameFilters.size() == 1) {
                                Iterator<ClassNameFilter> classNameFilterIterator =
                                        classNameFilters.iterator();
                                while (classNameFilterIterator.hasNext()) {
                                    ClassNameFilter classNameFilter =
                                            classNameFilterIterator.next();
                                    String classNameFilterString = classNameFilter.toString();
                                    if (classNameFilterString.startsWith("IncludeClassNameFilter")
                                            && classNameFilterString.endsWith(
                                                    "'^(Test.*|.+[.$]Test.*|.*Tests?)$'")) {
                                        classNameFilterIterator.remove();
                                    }
                                }
                            }
                            */

                            Predicate<String> classNamePredicate =
                                    composeFilters(classNameFilters).toPredicate();

                            List<? extends PackageNameFilter> packageNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(
                                            PackageNameFilter.class);

                            Predicate<String> packageNamePredicate =
                                    composeFilters(packageNameFilters).toPredicate();

                            testClasses.forEach(
                                    testClass -> {
                                        if (classNamePredicate.test(testClass.getName())
                                                && packageNamePredicate.test(
                                                        testClass.getPackage().getName())) {
                                            classMethodMap
                                                    .computeIfAbsent(
                                                            testClass, method -> new ArrayList<>())
                                                    .addAll(
                                                            ClassSupport.findMethods(
                                                                    testClass,
                                                                    ResolverPredicates.TEST_METHOD,
                                                                    HierarchyTraversalMode
                                                                            .BOTTOM_UP));
                                        }
                                    });
                        });

        LOGGER.trace(
                "resolve() classpathRootSelectors [%d] elapsedTime [%d] ms",
                classpathRootSelectorCount.get(), stopWatch.elapsedTime().toMillis());
    }
}