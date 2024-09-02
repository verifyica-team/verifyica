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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.antublue.verifyica.engine.common.Stopwatch;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.MethodSelector;

/** Class to implement MethodSelectorResolver */
public class MethodSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodSelectorResolver.class);

    /** Constructor */
    public MethodSelectorResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve MethodSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolve()");

        Stopwatch stopWatch = new Stopwatch();

        AtomicInteger methodSelectorCount = new AtomicInteger();

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                        methodSelector -> {
                            methodSelectorCount.incrementAndGet();

                            Class<?> testClass = methodSelector.getJavaClass();
                            Method testMethod = methodSelector.getJavaMethod();

                            LOGGER.trace(
                                    "testClass [%s] testMethod [%s]",
                                    testClass.getName(), testMethod.getName());

                            if (ResolverPredicates.TEST_CLASS.test(testClass)
                                    && ResolverPredicates.TEST_METHOD.test(testMethod)) {
                                classMethodMap
                                        .computeIfAbsent(testClass, method -> new ArrayList<>())
                                        .add(testMethod);
                            }
                        });

        LOGGER.trace(
                "resolve() methodSelectors [%d] elapsedTime [%d] ms",
                methodSelectorCount.get(), stopWatch.elapsedTime().toMillis());
    }
}
