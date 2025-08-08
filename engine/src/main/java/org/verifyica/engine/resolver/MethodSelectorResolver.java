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

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.MethodSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;

/** Class to implement MethodSelectorResolver */
public class MethodSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodSelectorResolver.class);

    /**
     * Constructor
     */
    public MethodSelectorResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to resolve MethodSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodSet classMethodSet
     */
    public void resolve(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, Set<Method>> classMethodSet) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        AtomicInteger methodSelectorCount = new AtomicInteger();

        engineDiscoveryRequest.getSelectorsByType(MethodSelector.class).forEach(methodSelector -> {
            methodSelectorCount.incrementAndGet();

            Class<?> testClass = methodSelector.getJavaClass();
            Method testMethod = methodSelector.getJavaMethod();

            LOGGER.trace("testClass [%s] testMethod [%s]", testClass.getName(), testMethod.getName());

            if (ResolverPredicates.TEST_CLASS.test(testClass) && ResolverPredicates.TEST_METHOD.test(testMethod)) {
                List<Method> testMethods = OrderSupport.orderMethods(ClassSupport.findMethods(
                        testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));

                for (Method method : testMethods) {
                    classMethodSet
                            .computeIfAbsent(testClass, set -> new LinkedHashSet<>())
                            .add(method);

                    if (method.equals(testMethod)) {
                        break;
                    }
                }
            }
        });

        LOGGER.trace(
                "resolve() methodSelectors [%d] elapsedTime [%d] ms",
                methodSelectorCount.get(), stopwatch.elapsed().toMillis());
    }
}
