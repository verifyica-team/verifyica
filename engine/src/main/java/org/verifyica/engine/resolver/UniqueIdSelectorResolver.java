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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.exception.UncheckedClassNotFoundException;
import org.verifyica.engine.execution.ClassTestDescriptor;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;

/** Class to implement UniqueIdSelectorResolver */
public class UniqueIdSelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIdSelectorResolver.class);

    /** Constructor */
    public UniqueIdSelectorResolver() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to resolve UniqueIdSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     * @param argumentIndexMap argumentIndexMap
     */
    public void resolve(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, List<Method>> classMethodMap,
            Map<Class<?>, Set<Integer>> argumentIndexMap) {
        LOGGER.trace("resolve()");

        Stopwatch stopwatch = new Stopwatch();

        AtomicInteger uniqueIdSelectorCount = new AtomicInteger();

        engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class).forEach(uniqueIdSelector -> {
            uniqueIdSelectorCount.incrementAndGet();

            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            List<UniqueId.Segment> segments = uniqueId.getSegments();

            LOGGER.trace("uniqueId [%s]", uniqueId);

            // Specific argument selected
            if (segments.size() == 3) {
                UniqueId.Segment classSegment = segments.get(1);
                UniqueId.Segment argumentSegment = segments.get(2);

                Class<?> testClass = null;

                try {
                    testClass = Thread.currentThread().getContextClassLoader().loadClass(classSegment.getValue());
                } catch (ClassNotFoundException e) {
                    UncheckedClassNotFoundException.throwUnchecked(e);
                }

                classMethodMap
                        .computeIfAbsent(testClass, method -> new ArrayList<>())
                        .addAll(ClassSupport.findMethods(
                                testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));

                argumentIndexMap
                        .computeIfAbsent(testClass, clazz -> new LinkedHashSet<>())
                        .add(Integer.parseInt(argumentSegment.getValue()));
            } else {
                segments.forEach(segment -> {
                    String segmentType = segment.getType();

                    if (segmentType.equals(ClassTestDescriptor.class.getName())) {
                        String javaClassName = segment.getValue();

                        Class<?> testClass = null;

                        try {
                            testClass = Thread.currentThread()
                                    .getContextClassLoader()
                                    .loadClass(javaClassName);
                        } catch (ClassNotFoundException e) {
                            UncheckedClassNotFoundException.throwUnchecked(e);
                        }

                        classMethodMap
                                .computeIfAbsent(testClass, method -> new ArrayList<>())
                                .addAll(ClassSupport.findMethods(
                                        testClass, ResolverPredicates.TEST_METHOD, HierarchyTraversalMode.BOTTOM_UP));
                    }
                });
            }
        });

        LOGGER.trace(
                "resolve() uniqueIdSelectors [%d] elapsedTime [%d] ms",
                uniqueIdSelectorCount.get(), stopwatch.elapsedTime().toMillis());
    }
}
