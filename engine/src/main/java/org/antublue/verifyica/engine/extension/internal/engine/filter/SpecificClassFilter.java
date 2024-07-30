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

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.TagSupport;

public class SpecificClassFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalClassFilter.class);

    private final String testClassName;
    private final String includeNameRegex;
    private final String excludeNameRegex;
    private final String includeTagRegex;
    private final String excludeTagRegex;

    public SpecificClassFilter(
            String testClassName,
            String includeNameRegex,
            String excludeNameRegex,
            String includeTagRegex,
            String excludeTagRegex) {
        this.testClassName = testClassName;
        this.includeNameRegex = cleanInclude(includeNameRegex);
        this.excludeNameRegex = cleanExclude(excludeNameRegex);
        this.includeTagRegex = cleanInclude(includeTagRegex);
        this.excludeTagRegex = cleanExclude(excludeTagRegex);

        LOGGER.trace(this);
    }

    @Override
    public Type getType() {
        return Type.SPECIFIC_CLASS_FILTER;
    }

    public void filterTestMethods(Class<?> testClass, List<Method> testMethods) {
        LOGGER.trace("filterTestMethods() testClass [%s]", testClass.getName());

        if (!testClassName.equals(testClass.getName())) {
            return;
        }

        Set<Method> includeMethodSet = new LinkedHashSet<>();
        Set<Method> excludeMethodSet = new LinkedHashSet<>();

        if (includeNameRegex != null) {
            Pattern pattern = Pattern.compile(includeNameRegex);
            testMethods.forEach(
                    method -> {
                        if (pattern.matcher(method.getName()).find()) {
                            includeMethodSet.add(method);
                        }
                    });
        }

        LOGGER.trace(
                "phase 1 - includeMethodSet size [%d] excludeMethodSet size [%d]",
                includeMethodSet.size(), excludeMethodSet.size());

        if (excludeNameRegex != null) {
            Pattern pattern = Pattern.compile(excludeNameRegex);
            testMethods.forEach(
                    method -> {
                        if (pattern.matcher(method.getName()).find()) {
                            excludeMethodSet.add(method);
                        }
                    });
        }

        LOGGER.trace(
                "phase 2 - includeMethodSet size [%d] excludeMethodSet size [%d]",
                includeMethodSet.size(), excludeMethodSet.size());

        if (includeTagRegex != null) {
            Pattern pattern = Pattern.compile(includeTagRegex);
            testMethods.forEach(
                    method ->
                            TagSupport.getTags(method)
                                    .forEach(
                                            tag -> {
                                                if (pattern.matcher(tag).find()) {
                                                    includeMethodSet.add(method);
                                                }
                                            }));
        }

        LOGGER.trace(
                "phase 3 - includeMethodSet size [%d] excludeMethodSet size [%d]",
                includeMethodSet.size(), excludeMethodSet.size());

        if (excludeTagRegex != null) {
            Pattern pattern = Pattern.compile(excludeTagRegex);
            testMethods.forEach(
                    method ->
                            TagSupport.getTags(method)
                                    .forEach(
                                            tag -> {
                                                if (pattern.matcher(tag).find()) {
                                                    excludeMethodSet.add(method);
                                                }
                                            }));
        }

        LOGGER.trace(
                "phase 4 - includeMethodSet size [%d] excludeMethodSet size [%d]",
                includeMethodSet.size(), excludeMethodSet.size());

        testMethods.removeIf(
                method -> !includeMethodSet.contains(method) || excludeMethodSet.contains(method));
    }

    @Override
    public String toString() {
        return "SpecificClassFilter{"
                + "testClassName="
                + testClassName
                + ", includeNameRegex='"
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
        SpecificClassFilter that = (SpecificClassFilter) o;
        return Objects.equals(testClassName, that.testClassName)
                && Objects.equals(includeNameRegex, that.includeNameRegex)
                && Objects.equals(excludeNameRegex, that.excludeNameRegex)
                && Objects.equals(includeTagRegex, that.includeTagRegex)
                && Objects.equals(excludeTagRegex, that.excludeTagRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                testClassName,
                includeNameRegex,
                excludeNameRegex,
                includeTagRegex,
                excludeTagRegex);
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
