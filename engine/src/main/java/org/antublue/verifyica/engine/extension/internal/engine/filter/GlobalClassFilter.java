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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.support.TagSupport;

public class GlobalClassFilter implements Filter {

    private final String includeNameRegex;
    private final String excludeNameRegex;
    private final String includeTagRegex;
    private final String excludeTagRegex;

    public GlobalClassFilter(
            String includeNameRegex,
            String excludeNameRegex,
            String includeTagRegex,
            String excludeTagRegex) {
        if (includeNameRegex == null || includeNameRegex.trim().isEmpty()) {
            this.includeNameRegex = ".*";
        } else {
            this.includeNameRegex = includeNameRegex.trim();
        }

        if (excludeNameRegex == null || excludeNameRegex.trim().isEmpty()) {
            this.excludeNameRegex = "a^";
        } else {
            this.excludeNameRegex = excludeNameRegex.trim();
        }

        if (includeTagRegex == null || includeTagRegex.trim().isEmpty()) {
            this.includeTagRegex = ".*";
        } else {
            this.includeTagRegex = includeTagRegex.trim();
        }

        if (excludeTagRegex == null || excludeTagRegex.trim().isEmpty()) {
            this.excludeTagRegex = "a^";
        } else {
            this.excludeTagRegex = excludeTagRegex.trim();
        }
    }

    @Override
    public Type getType() {
        return Type.GLOBAL_CLASS_FILTER;
    }

    public void process(List<Class<?>> testClasses) {
        Set<Class<?>> testClassSet = new LinkedHashSet<>();

        Iterator<Class<?>> testClassesIterator = testClasses.iterator();

        Pattern includeNamePattern = Pattern.compile(includeNameRegex);
        Pattern excludeNamePattern = Pattern.compile(excludeNameRegex);
        Pattern includeTagPattern = Pattern.compile(includeTagRegex);
        Pattern excludeTagPattern = Pattern.compile(excludeTagRegex);

        while (testClassesIterator.hasNext()) {
            Class<?> testClass = testClassesIterator.next();

            Matcher includeNameMatcher = includeNamePattern.matcher(testClass.getName());
            if (!includeNameMatcher.find()) {
                testClassesIterator.remove();
                continue;
            }

            Matcher excludeNameMatcher = excludeNamePattern.matcher(testClass.getName());
            if (excludeNameMatcher.find()) {
                testClassesIterator.remove();
                continue;
            }

            testClassSet.add(testClass);
        }

        testClassesIterator = testClasses.iterator();
        while (testClassesIterator.hasNext()) {
            Class<?> testClass = testClassesIterator.next();

            List<String> testClassTags = TagSupport.getTags(testClass);

            if (testClassTags.isEmpty() && !includeTagRegex.equals(".*")) {
                testClassesIterator.remove();
                continue;
            } else {
                for (String testClassTag : testClassTags) {
                    Matcher includeTagMatcher = includeTagPattern.matcher(testClassTag);
                    if (!includeTagMatcher.find()) {
                        testClassesIterator.remove();
                        continue;
                    }

                    Matcher excludeTagMatcher = excludeTagPattern.matcher(testClassTag);
                    if (excludeTagMatcher.find()) {
                        testClassesIterator.remove();
                        continue;
                    }
                }
            }

            testClassSet.add(testClass);
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
}
