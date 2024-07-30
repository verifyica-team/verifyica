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

package org.antublue.verifyica.engine.extension.internal.engine.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

@SuppressWarnings("PMD.UnusedLocalVariable")
public class GlobalClassFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalClassFilter.class);

    private final String includeNameRegex;
    private final String excludeNameRegex;
    private final String includeTagRegex;
    private final String excludeTagRegex;

    /**
     * Constructor
     *
     * @param includeNameRegex includeNameRegex
     * @param excludeNameRegex excludeNameRegex
     * @param includeTagRegex includeTagRegex
     * @param excludeTagRegex excludeTagRegex
     */
    public GlobalClassFilter(
            String includeNameRegex,
            String excludeNameRegex,
            String includeTagRegex,
            String excludeTagRegex) {
        this.includeNameRegex = cleanInclude(includeNameRegex);
        this.excludeNameRegex = cleanExclude(excludeNameRegex);
        this.includeTagRegex = cleanInclude(includeTagRegex);
        this.excludeTagRegex = cleanExclude(excludeTagRegex);

        LOGGER.trace(this);
    }

    @Override
    public Type getType() {
        return Type.GLOBAL_CLASS_FILTER;
    }

    /**
     * Method to filter test classes
     *
     * @param testClasses testClasses
     */
    public void filterClasses(List<Class<?>> testClasses) {
        LOGGER.trace("filterClasses()");

        Pattern includeNameRegexPattern = Pattern.compile(includeNameRegex);
        Pattern includeTagRegexPattern = Pattern.compile(includeTagRegex);
        Pattern excludeNameRegexPattern = Pattern.compile(excludeNameRegex);
        Pattern excludeTagRegexPattern = Pattern.compile(excludeTagRegex);

        Predicate<Class<?>> includeNamePredicate =
                clazz -> includeNameRegexPattern.matcher(clazz.getName()).find();
        Predicate<Class<?>> includeTagPredicate =
                clazz -> includeTagRegexPattern.matcher(clazz.getName()).find();

        Predicate<Class<?>> excludeNamePredicate =
                clazz -> excludeNameRegexPattern.matcher(clazz.getName()).find();
        Predicate<Class<?>> excludeTagPredicate =
                clazz -> excludeTagRegexPattern.matcher(clazz.getName()).find();

        for (Class<?> testClass : new ArrayList<>(testClasses)) {
            if (!includeNamePredicate.test(testClass) || !includeTagPredicate.test(testClass)) {
                testClasses.remove(testClass);
            }

            /*
            if (excludeNamePredicate.test(testClass) || excludeTagPredicate.test(testClass)) {
                testClasses.remove(testClass);
            }
            */
        }
    }

    @Override
    public String toString() {
        return "GlobalClassFilter{"
                + "includeNameRegex='"
                + includeNameRegex
                + '\''
                + ", excludeNameRegex='"
                + excludeNameRegex
                + '\''
                + ", includeTagRegex='"
                + includeTagRegex
                + '\''
                + ", excludeTagRegex='"
                + excludeTagRegex
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalClassFilter that = (GlobalClassFilter) o;
        return Objects.equals(includeNameRegex, that.includeNameRegex)
                && Objects.equals(excludeNameRegex, that.excludeNameRegex)
                && Objects.equals(includeTagRegex, that.includeTagRegex)
                && Objects.equals(excludeTagRegex, that.excludeTagRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeNameRegex, excludeNameRegex, includeTagRegex, excludeTagRegex);
    }

    private String cleanInclude(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return ".*";
        } else {
            return regex.trim();
        }
    }

    private String cleanExclude(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return "a^";
        } else {
            return regex.trim();
        }
    }
}
