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

package org.verifyica.engine.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;

/** Class to implement ClassSelectorResolver */
public class ClassSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassSelectorResolver.class);

    /** Constructor */
    public ClassSelectorResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve ClassSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        AtomicInteger classSelectorCount = new AtomicInteger();

        engineDiscoveryRequest.getSelectorsByType(ClassSelector.class).forEach(classSelector -> {
            classSelectorCount.incrementAndGet();

            Class<?> testClass = classSelector.getJavaClass();

            if (ResolverPredicates.TEST_CLASS.test(testClass)) {
                classMethodMap
                        .computeIfAbsent(testClass, method -> new ArrayList<>())
                        .addAll(ClassSupport.findMethods(
                                testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
            }

            processInnerClasses(testClass, classMethodMap);
        });

        LOGGER.trace(
                "resolve() classSelectors [%d] elapsedTime [%d] ms",
                classSelectorCount.get(), stopwatch.elapsedTime().toMillis());
    }

    private void processInnerClasses(Class<?> testClass, Map<Class<?>, List<Method>> classMethodMap) {
        Class<?>[] innerClasses = testClass.getDeclaredClasses();
        for (Class<?> innerClass : innerClasses) {
            processInnerClasses(innerClass, classMethodMap);
        }

        if (ResolverPredicates.TEST_CLASS.test(testClass)) {
            classMethodMap
                    .computeIfAbsent(testClass, method -> new ArrayList<>())
                    .addAll(ClassSupport.findMethods(
                            testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
        }
    }
}