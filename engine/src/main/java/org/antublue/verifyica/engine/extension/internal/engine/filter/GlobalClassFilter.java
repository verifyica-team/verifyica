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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.antublue.verifyica.engine.support.TagSupport;

public class GlobalClassFilter implements Filter {

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
        this.includeNameRegex = clean(includeNameRegex);
        this.excludeNameRegex = clean(excludeNameRegex);
        this.includeTagRegex = clean(includeTagRegex);
        this.excludeTagRegex = clean(excludeTagRegex);
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
        Set<Class<?>> includeClassSet = new LinkedHashSet<>();
        Set<Class<?>> excludeClassSet = new LinkedHashSet<>();

        if (includeNameRegex != null) {
            Pattern pattern = Pattern.compile(includeNameRegex);
            testClasses.forEach(
                    clazz -> {
                        if (pattern.matcher(clazz.getName()).find()) {
                            includeClassSet.add(clazz);
                        }
                    });
        }

        if (excludeNameRegex != null) {
            Pattern pattern = Pattern.compile(excludeNameRegex);
            testClasses.forEach(
                    clazz -> {
                        if (pattern.matcher(clazz.getName()).find()) {
                            excludeClassSet.add(clazz);
                        }
                    });
        }

        if (includeTagRegex != null) {
            Pattern pattern = Pattern.compile(includeTagRegex);
            testClasses.forEach(
                    clazz ->
                            TagSupport.getTags(clazz)
                                    .forEach(
                                            tag -> {
                                                if (pattern.matcher(tag).find()) {
                                                    includeClassSet.add(clazz);
                                                }
                                            }));
        }

        if (excludeTagRegex != null) {
            Pattern pattern = Pattern.compile(excludeTagRegex);
            testClasses.forEach(
                    clazz ->
                            TagSupport.getTags(clazz)
                                    .forEach(
                                            tag -> {
                                                if (pattern.matcher(tag).find()) {
                                                    excludeClassSet.add(clazz);
                                                }
                                            }));
        }

        if (includeClassSet.isEmpty() && excludeClassSet.isEmpty()) {
            // INTENTIONALLY BLANK
        } else if (!includeClassSet.isEmpty() && excludeClassSet.isEmpty()) {
            testClasses.removeIf(clazz -> !includeClassSet.contains(clazz));
        } else if (includeClassSet.isEmpty()) {
            testClasses.removeIf(clazz -> excludeClassSet.contains(clazz));
        } else {
            includeClassSet.removeIf(clazz -> excludeClassSet.contains(clazz));
            testClasses.removeIf(clazz -> !includeClassSet.contains(clazz));
        }
    }

    private String clean(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return null;
        } else {
            return regex.trim();
        }
    }
}
