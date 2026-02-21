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
import java.util.List;
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
     * Filters the provided list of {@link ClassDefinition} instances in-place based on the
     * include and exclude filters loaded from {@link FilterFactory}.
     *
     * <p>Filtering is applied in two stages:
     * <ol>
     *   <li><b>Include stage:</b> If any {@code INCLUDE_CLASS} or {@code INCLUDE_TAGGED_CLASS}
     *       filters are present, only class definitions whose test class matches at least one
     *       include filter are retained. If no include filters are present, all class definitions
     *       pass this stage.</li>
     *   <li><b>Exclude stage:</b> Any class definitions whose test class matches at least one
     *       {@code EXCLUDE_CLASS} or {@code EXCLUDE_TAGGED_CLASS} filter are removed. If no
     *       exclude filters are present, no class definitions are removed at this stage.</li>
     * </ol>
     *
     * <p>Insertion order of the original list is preserved in the filtered result.
     *
     * <p>If no filters of either type are present, the list is left unchanged and no
     * further processing occurs.
     *
     * @param classDefinitions the mutable list of {@link ClassDefinition} instances to filter;
     *                         modified in-place to contain only the definitions that survive
     *                         all filter stages
     */
    public static void filter(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("filter()");

        List<Filter> filters = FilterFactory.loadFilters();

        List<Filter> includeFilters = new ArrayList<>();
        List<Filter> excludeFilters = new ArrayList<>();
        partitionFilters(filters, includeFilters, excludeFilters);

        // Fast path: no filters loaded, nothing to do
        if (includeFilters.isEmpty() && excludeFilters.isEmpty()) {
            return;
        }

        List<ClassDefinition> result = new ArrayList<>(classDefinitions.size());

        for (ClassDefinition cd : classDefinitions) {
            Class<?> testClass = cd.getTestClass();

            if (isIncluded(testClass, includeFilters) && !isExcluded(testClass, excludeFilters)) {
                result.add(cd);
            }
        }

        classDefinitions.clear();
        classDefinitions.addAll(result);
    }

    /**
     * Partitions the provided list of {@link Filter} instances into separate include and
     * exclude lists based on their {@link Filter.Type}.
     *
     * <p>Filters of type {@code INCLUDE_CLASS} or {@code INCLUDE_TAGGED_CLASS} are added to
     * {@code includeFilters}. Filters of type {@code EXCLUDE_CLASS} or
     * {@code EXCLUDE_TAGGED_CLASS} are added to {@code excludeFilters}. Filters of any other
     * type are ignored.
     *
     * @param filters        the full list of loaded {@link Filter} instances to partition
     * @param includeFilters the list to populate with include-type filters; must be mutable
     * @param excludeFilters the list to populate with exclude-type filters; must be mutable
     */
    private static void partitionFilters(
            List<Filter> filters, List<Filter> includeFilters, List<Filter> excludeFilters) {
        for (Filter filter : filters) {
            switch (filter.getType()) {
                case INCLUDE_CLASS:
                case INCLUDE_TAGGED_CLASS:
                    includeFilters.add(filter);
                    break;
                case EXCLUDE_CLASS:
                case EXCLUDE_TAGGED_CLASS:
                    excludeFilters.add(filter);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Determines whether the given class passes the include stage.
     *
     * <p>If no include filters are present, the class is considered included by default.
     * Otherwise, the class must match at least one include filter to be retained. Evaluation
     * short-circuits on the first match.
     *
     * @param testClass      the class to evaluate against the include filters
     * @param includeFilters the list of active include filters; may be empty
     * @return {@code true} if the class should be included, {@code false} otherwise
     */
    private static boolean isIncluded(Class<?> testClass, List<Filter> includeFilters) {
        if (includeFilters.isEmpty()) {
            return true;
        }

        for (Filter filter : includeFilters) {
            if (filter.matches(testClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether the given class is excluded by any exclude filter.
     *
     * <p>If no exclude filters are present, the class is never excluded. Otherwise, the
     * class is excluded if it matches at least one exclude filter. Evaluation short-circuits
     * on the first match.
     *
     * @param testClass      the class to evaluate against the exclude filters
     * @param excludeFilters the list of active exclude filters; may be empty
     * @return {@code true} if the class should be excluded, {@code false} otherwise
     */
    private static boolean isExcluded(Class<?> testClass, List<Filter> excludeFilters) {
        for (Filter filter : excludeFilters) {
            if (filter.matches(testClass)) {
                return true;
            }
        }

        return false;
    }
}
