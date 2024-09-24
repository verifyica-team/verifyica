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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.engine.ClassDefinition;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;
import org.antublue.verifyica.engine.exception.TestClassDefinitionException;

/** Class to implement StepMethodOrderer */
public class StepMethodOrderer {

    /** Enum to define where to place test methods that don't define a Step */
    public enum StandardTests {
        /** Execute the standard test methods first */
        FIRST,
        /** Execute the standard test methods last */
        LAST
    }

    private final StandardTests standardTests;

    /** Constructor */
    public StepMethodOrderer() {
        this(StandardTests.LAST);
    }

    /**
     * Constructor
     *
     * @param standardTests unidgedTests
     */
    public StepMethodOrderer(StandardTests standardTests) {
        this.standardTests = standardTests;
    }

    /**
     * Method to order MethodDefinitions by Step ids, placing any test methods without Step ids last
     *
     * @param classDefinition classDefinition
     */
    public void orderMethods(ClassDefinition classDefinition) {
        Set<MethodDefinition> methodDefinitions = classDefinition.getTestMethodDefinitions();

        List<MethodDefinition> noStepMethodDefinitions = new ArrayList<>();
        for (MethodDefinition methodDefinition : methodDefinitions) {
            if (!methodDefinition.getMethod().isAnnotationPresent(Verifyica.Step.class)) {
                noStepMethodDefinitions.add(methodDefinition);
            }
        }

        Map<String, MethodDefinition> idToMethodDefinition = new HashMap<>();
        Map<String, String> idToNextIdMap = new HashMap<>();

        int emptyNextStepCount = 0;

        for (MethodDefinition methodDefinition : methodDefinitions) {
            if (methodDefinition.getMethod().isAnnotationPresent(Verifyica.Step.class)) {
                Verifyica.Step stepAnnotation =
                        methodDefinition.getMethod().getAnnotation(Verifyica.Step.class);

                String id = stepAnnotation.id().trim();
                if (id.isEmpty()) {
                    throw new TestClassDefinitionException(
                            format(
                                    "Incorrect usage of @Verifyica.Step annotation in test class"
                                            + " [%s]. @Verifyica.Step \"id\" is effectively"
                                            + " blank for test method [%s]",
                                    classDefinition.getTestClass().getName(),
                                    methodDefinition.getMethod().getName()));
                }

                idToMethodDefinition.put(id, methodDefinition);

                String nextId = stepAnnotation.nextId();
                if (nextId != null) {
                    nextId = nextId.trim();

                    if (nextId.isEmpty()) {
                        emptyNextStepCount++;
                    }
                }

                if (emptyNextStepCount > 1) {
                    throw new TestClassDefinitionException(
                            format(
                                    "Incorrect usage of @Verifyica.Step annotation in test class"
                                            + " [%s]. Multiple @Verifyica.Step annotations found"
                                            + " with a missing or blank \"nextId\"",
                                    classDefinition.getTestClass().getName()));
                }

                idToNextIdMap.put(stepAnnotation.id(), nextId);
            }
        }

        List<MethodDefinition> orderedMethodDefinitions = new ArrayList<>();

        Set<String> visitedIds = new HashSet<>();
        Set<String> processedIds = new HashSet<>();

        for (String id : idToMethodDefinition.keySet()) {
            if (!visitedIds.contains(id)) {
                visit(
                        classDefinition,
                        id,
                        idToNextIdMap,
                        idToMethodDefinition,
                        orderedMethodDefinitions,
                        visitedIds,
                        processedIds);
            }
        }

        Collections.reverse(orderedMethodDefinitions);

        methodDefinitions.clear();

        if (standardTests == StandardTests.FIRST) {
            methodDefinitions.addAll(noStepMethodDefinitions);
        }

        methodDefinitions.addAll(orderedMethodDefinitions);

        if (standardTests == StandardTests.LAST) {
            methodDefinitions.addAll(noStepMethodDefinitions);
        }
    }

    /**
     * Method to visit
     *
     * @param id id
     * @param idToNextIdMap idToNextIdMap
     * @param idToMethodDefinition idToMethodDefinition
     * @param orderedMethodDefinitions orderedMethodDefinitions
     * @param visitedIds visitedIds
     * @param processedIds processedIds
     */
    private void visit(
            ClassDefinition classDefinition,
            String id,
            Map<String, String> idToNextIdMap,
            Map<String, MethodDefinition> idToMethodDefinition,
            List<MethodDefinition> orderedMethodDefinitions,
            Set<String> visitedIds,
            Set<String> processedIds) {
        if (processedIds.contains(id)) {
            return;
        }

        if (visitedIds.contains(id)) {
            throw new TestClassDefinitionException(
                    format(
                            "Cycle detected in test class [%s] usage of @Verifyica.Step annotation."
                                    + " Check ids and nextIds.",
                            classDefinition.getTestClass().getName()));
        }

        visitedIds.add(id);
        String nextId = idToNextIdMap.get(id);

        if (nextId != null && !nextId.isEmpty()) {
            visit(
                    classDefinition,
                    nextId,
                    idToNextIdMap,
                    idToMethodDefinition,
                    orderedMethodDefinitions,
                    visitedIds,
                    processedIds);
        }

        orderedMethodDefinitions.add(idToMethodDefinition.get(id));
        processedIds.add(id);
    }
}
