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

package org.verifyica.engine.filter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;

/** Class to implement ClassDefinitionFilter */
public class ClassDefinitionFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassDefinitionFilter.class);

    /** Constructor */
    private ClassDefinitionFilter() {
        // INTENTIONALLY BLANK
    }

    public static void filter(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("filter()");

        List<Filter> filters = FilterFactory.loadFilters();

        Set<ClassDefinition> workingClassDefinitionSet = new LinkedHashSet<>(classDefinitions);

        classDefinitions.forEach(classDefinition -> {
            for (Filter filter : filters) {
                Class<?> testClass = classDefinition.getTestClass();
                switch (filter.getType()) {
                    case EXCLUDE_CLASS:
                    case EXCLUDE_TAGGED_CLASS: {
                        if (filter.matches(testClass)) {
                            workingClassDefinitionSet.remove(classDefinition);
                        }
                        break;
                    }
                    case INCLUDE_CLASS:
                    case INCLUDE_TAGGED_CLASS: {
                        if (filter.matches(testClass)) {
                            workingClassDefinitionSet.add(classDefinition);
                        }
                        break;
                    }
                    default: {
                        // INTENTIONALLY BLANK
                    }
                }
            }
        });

        classDefinitions.clear();
        classDefinitions.addAll(workingClassDefinitionSet);
    }
}
