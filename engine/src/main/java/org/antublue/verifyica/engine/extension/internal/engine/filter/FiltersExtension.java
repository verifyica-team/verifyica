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

package org.antublue.verifyica.engine.extension.internal.engine.filter;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.extension.TestClassDefinition;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.extension.internal.engine.InternalEngineExtension;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement FiltersExtension */
@Verifyica.Order(order = 0)
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class FiltersExtension implements InternalEngineExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltersExtension.class);

    /** Constructor */
    public FiltersExtension() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void onTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            List<TestClassDefinition> testClassDefinitions) {
        LOGGER.trace("onTestDiscovery()");

        applyFilters(testClassDefinitions);
    }

    private static void applyFilters(List<TestClassDefinition> testClassDefinitions) {
        LOGGER.trace("applyFilters()");

        List<Filter> filters = FilterFactory.getInstance().loadFilters();

        Map<Class<?>, Map<String, Method>> workingClassMethodMap = new LinkedHashMap<>();

        testClassDefinitions.forEach(
                testClassDefinition -> {
                    Class<?> testClass = testClassDefinition.getTestClass();
                    testClassDefinition
                            .getTestMethods()
                            .forEach(
                                    testMethod ->
                                            workingClassMethodMap
                                                    .computeIfAbsent(
                                                            testClass, m -> new LinkedHashMap<>())
                                                    .put(testMethod.getName(), testMethod));
                });

        testClassDefinitions.forEach(
                testClassDefinition ->
                        testClassDefinition
                                .getTestMethods()
                                .forEach(
                                        testMethod -> {
                                            for (Filter filter : filters) {
                                                Class<?> testClass =
                                                        testClassDefinition.getTestClass();
                                                switch (filter.getType()) {
                                                    case INCLUDE_CLASS:
                                                    case INCLUDE_TAGGED_CLASS:
                                                        {
                                                            if (filter.matches(
                                                                    testClass, testMethod)) {
                                                                workingClassMethodMap
                                                                        .computeIfAbsent(
                                                                                testClass,
                                                                                m ->
                                                                                        new LinkedHashMap<>())
                                                                        .put(
                                                                                testMethod
                                                                                        .getName(),
                                                                                testMethod);
                                                            }
                                                            break;
                                                        }
                                                    case EXCLUDE_CLASS:
                                                    case EXCLUDE_TAGGED_CLASS:
                                                        {
                                                            if (filter.matches(
                                                                    testClass, testMethod)) {
                                                                workingClassMethodMap
                                                                        .computeIfAbsent(
                                                                                testClass,
                                                                                m ->
                                                                                        new LinkedHashMap<>())
                                                                        .remove(
                                                                                testMethod
                                                                                        .getName());
                                                            }
                                                            break;
                                                        }
                                                    default:
                                                        {
                                                            // INTENTIONALLY BLANK
                                                        }
                                                }
                                            }
                                        }));

        workingClassMethodMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        Iterator<TestClassDefinition> testClassDefinitionIterator = testClassDefinitions.iterator();
        while (testClassDefinitionIterator.hasNext()) {
            TestClassDefinition testClassDefinition = testClassDefinitionIterator.next();
            Class<?> testClass = testClassDefinition.getTestClass();
            if (!workingClassMethodMap.containsKey(testClass)) {
                testClassDefinitionIterator.remove();
            } else {
                testClassDefinition.getTestMethods().clear();
                testClassDefinition
                        .getTestMethods()
                        .addAll(new LinkedHashSet<>(workingClassMethodMap.get(testClass).values()));
            }
        }
    }
}
