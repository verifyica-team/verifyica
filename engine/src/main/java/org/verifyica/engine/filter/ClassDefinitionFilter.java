/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.engine.filter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/**
 * Class to implement ClassDefinitionFilter
 */
public class ClassDefinitionFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassDefinitionFilter.class);

    /**
     * Constructor
     */
    private ClassDefinitionFilter() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to filter class definitions
     *
     * @param classDefinitions classDefinitions
     */
    public static void filter(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("filter()");

        List<Filter> filters = FilterFactory.loadFilters();

        // Separate filters by type
        List<Filter> includeFilters = new ArrayList<>();
        List<Filter> excludeFilters = new ArrayList<>();

        for (Filter filter : filters) {
            if (filter.getType() == Filter.Type.INCLUDE_CLASS || filter.getType() == Filter.Type.INCLUDE_TAGGED_CLASS) {
                includeFilters.add(filter);
            } else if (filter.getType() == Filter.Type.EXCLUDE_CLASS
                    || filter.getType() == Filter.Type.EXCLUDE_TAGGED_CLASS) {
                excludeFilters.add(filter);
            }
        }

        Set<ClassDefinition> workingClassDefinitionSet = new LinkedHashSet<>(classDefinitions);

        // If there are INCLUDE filters, start with empty set and only add matching classes
        if (!includeFilters.isEmpty()) {
            workingClassDefinitionSet.clear();
            for (ClassDefinition classDefinition : classDefinitions) {
                Class<?> testClass = classDefinition.getTestClass();
                for (Filter filter : includeFilters) {
                    if (filter.matches(testClass)) {
                        workingClassDefinitionSet.add(classDefinition);
                        break; // Once matched by any include filter, add it
                    }
                }
            }
        }

        // Apply EXCLUDE filters - remove matching classes
        for (ClassDefinition classDefinition : new ArrayList<>(workingClassDefinitionSet)) {
            Class<?> testClass = classDefinition.getTestClass();
            for (Filter filter : excludeFilters) {
                if (filter.matches(testClass)) {
                    workingClassDefinitionSet.remove(classDefinition);
                    break; // Once matched by any exclude filter, remove it
                }
            }
        }

        classDefinitions.clear();
        classDefinitions.addAll(workingClassDefinitionSet);
    }
}
