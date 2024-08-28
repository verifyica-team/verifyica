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

package org.antublue.verifyica.engine.discovery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.antublue.verifyica.engine.common.Stopwatch;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.HierarchyTraversalMode;
import org.antublue.verifyica.engine.support.MethodSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassSelector;

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
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap) {
        LOGGER.trace("resolve()");

        Stopwatch stopWatch = new Stopwatch();

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(
                        classSelector -> {
                            Class<?> testClass = classSelector.getJavaClass();

                            if (ResolverPredicates.TEST_CLASS.test(testClass)) {
                                classMethodMap
                                        .computeIfAbsent(testClass, method -> new ArrayList<>())
                                        .addAll(
                                                MethodSupport.findMethods(
                                                        testClass,
                                                        ResolverPredicates.TEST_METHOD,
                                                        HierarchyTraversalMode.BOTTOM_UP));
                            }
                        });

        LOGGER.trace("resolve() [%d] ms", stopWatch.elapsedTime().toMillis());
    }
}
