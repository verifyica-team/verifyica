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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.support.TagSupport;

public class SpecificClassFilter implements Filter {

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
        return Type.SPECIFIC_CLASS_FILTER;
    }

    public void process(Class<?> testClass, List<Method> testMethods) {
        if (!testClassName.equals(testClass.getName())) {
            return;
        }

        Pattern includeNamePattern = Pattern.compile(includeNameRegex);
        Pattern excludeNamePattern = Pattern.compile(excludeNameRegex);
        Pattern includeTagPattern = Pattern.compile(includeTagRegex);
        Pattern excludeTagPattern = Pattern.compile(excludeTagRegex);

        Set<Method> keepTestMethods = new LinkedHashSet<>();

        for (Method testMethod : testMethods) {
            boolean keep = false;
            Matcher includeNameMatcher = includeNamePattern.matcher(testMethod.getName());

            if (includeNameMatcher.find()) {
                keep = true;
            } else {
                List<String> testClassTags = TagSupport.getTags(testMethod);
                for (String testClassTag : testClassTags) {
                    Matcher includeTagMatcher = includeTagPattern.matcher(testClassTag);
                    if (includeTagMatcher.find()) {
                        keep = true;
                    }
                }
            }

            if (keep) {
                keepTestMethods.add(testMethod);
            }
        }

        for (Method testMethod : testMethods) {
            boolean keep = true;
            Matcher excludeNameMatcher = excludeNamePattern.matcher(testMethod.getName());

            if (excludeNameMatcher.find()) {
                keep = false;
            } else {
                List<String> testClassTags = TagSupport.getTags(testMethod);
                for (String testClassTag : testClassTags) {
                    Matcher excludeTagMatcher = excludeTagPattern.matcher(testClassTag);
                    if (excludeTagMatcher.find()) {
                        keep = false;
                    }
                }
            }

            if (!keep) {
                keepTestMethods.remove(testMethod);
            }
        }

        testMethods.clear();
        testMethods.addAll(keepTestMethods);
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
}
