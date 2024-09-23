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

package org.antublue.verifyica.test.order;

import java.util.*;
import org.antublue.verifyica.api.interceptor.engine.MethodDefinition;

/** Class to implement StepMethodOrderer */
public class StepMethodOrderer {

    /** Constructor */
    public StepMethodOrderer() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to order MethodDefinitions by Step tags, placing any test methods without Step tags
     * last
     *
     * @param methodDefinitions methodDefinitions
     */
    public void orderMethods(Set<MethodDefinition> methodDefinitions) {
        System.out.println("order methods");

        Map<String, MethodDefinition> tagToMethodDefinition = new HashMap<>();
        Map<String, String> tagToNextTagMap = new HashMap<>();

        for (MethodDefinition methodDefinition : methodDefinitions) {
            if (methodDefinition.getMethod().isAnnotationPresent(Step.class)) {
                Step stepAnnotation = methodDefinition.getMethod().getAnnotation(Step.class);
                tagToMethodDefinition.put(stepAnnotation.tag(), methodDefinition);
                tagToNextTagMap.put(stepAnnotation.tag(), stepAnnotation.nextTag());
            }
        }

        List<MethodDefinition> orderedMethods = new ArrayList<>();
        Set<String> visitedTags = new HashSet<>();
        Set<String> processedTags = new HashSet<>();

        for (String tag : tagToMethodDefinition.keySet()) {
            if (!visitedTags.contains(tag)) {
                visit(
                        tag,
                        tagToNextTagMap,
                        tagToMethodDefinition,
                        orderedMethods,
                        visitedTags,
                        processedTags);
            }
        }

        Collections.reverse(orderedMethods);

        for (MethodDefinition methodDefinition : methodDefinitions) {
            if (!methodDefinition.getMethod().isAnnotationPresent(Step.class)) {
                orderedMethods.add(methodDefinition);
            }
        }

        methodDefinitions.clear();
        methodDefinitions.addAll(orderedMethods);
    }

    /**
     * Method to visit
     *
     * @param tag tag
     * @param tagToNextTagMap tagToNextTagMap
     * @param tagToMethodDefinition tagToMethodDefinition
     * @param orderedMethods orderedMethods
     * @param visitedTags visitedTags
     * @param processedTags processedTags
     */
    private void visit(
            String tag,
            Map<String, String> tagToNextTagMap,
            Map<String, MethodDefinition> tagToMethodDefinition,
            List<MethodDefinition> orderedMethods,
            Set<String> visitedTags,
            Set<String> processedTags) {
        if (processedTags.contains(tag)) {
            return;
        }

        if (visitedTags.contains(tag)) {
            throw new IllegalStateException("Cycle detected in tagged test method dependencies");
        }

        visitedTags.add(tag);
        String nextTag = tagToNextTagMap.get(tag);

        if (nextTag != null && !nextTag.isEmpty()) {
            visit(
                    nextTag,
                    tagToNextTagMap,
                    tagToMethodDefinition,
                    orderedMethods,
                    visitedTags,
                    processedTags);
        }

        orderedMethods.add(tagToMethodDefinition.get(tag));
        processedTags.add(tag);
    }
}
