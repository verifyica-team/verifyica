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

package org.verifyica.engine.support;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;

/**
 * Class to implement TagSupport
 */
public class TagSupport {

    // Caches for tags to avoid repeated annotation lookups.
    // Using WeakHashMap to avoid classloader leaks.
    private static final Map<Class<?>, Set<String>> CLASS_TAGS_CACHE = new WeakHashMap<>();
    private static final Map<Method, Set<String>> METHOD_TAGS_CACHE = new WeakHashMap<>();

    /**
     * Constructor
     */
    private TagSupport() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to get a Set of tags for a Class
     *
     * <p>Performance note: Results are cached per class to avoid repeated annotation lookups.</p>
     *
     * @param clazz clazz
     * @return a Set of tags
     */
    public static Set<String> getTags(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        synchronized (CLASS_TAGS_CACHE) {
            Set<String> cached = CLASS_TAGS_CACHE.get(clazz);
            if (cached != null) {
                return cached;
            }

            Set<String> tags = new HashSet<>();

            Verifyica.Tag annotation = clazz.getAnnotation(Verifyica.Tag.class);
            if (annotation != null) {
                String tag = annotation.value();
                if (tag != null) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        tags.add(trimmed);
                    }
                }
            }

            Verifyica.Tags tagsAnnotation = clazz.getAnnotation(Verifyica.Tags.class);
            if (tagsAnnotation != null) {
                Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
                if (tagAnnotations != null) {
                    for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                        String tag = tagAnnotation.value();
                        if (tag != null) {
                            String trimmed = tag.trim();
                            if (!trimmed.isEmpty()) {
                                tags.add(trimmed);
                            }
                        }
                    }
                }
            }

            CLASS_TAGS_CACHE.put(clazz, tags);
            return tags;
        }
    }

    /**
     * Method to get a Set of tags for a Method
     *
     * <p>Performance note: Results are cached per method to avoid repeated annotation lookups.</p>
     *
     * @param method method
     * @return a Set of tags
     */
    public static Set<String> getTags(Method method) {
        Precondition.notNull(method, "method is null");

        synchronized (METHOD_TAGS_CACHE) {
            Set<String> cached = METHOD_TAGS_CACHE.get(method);
            if (cached != null) {
                return cached;
            }

            Set<String> tags = new HashSet<>();

            Verifyica.Tag annotation = method.getAnnotation(Verifyica.Tag.class);
            if (annotation != null) {
                String tag = annotation.value();
                if (tag != null) {
                    String trimmed = tag.trim();
                    if (!trimmed.isEmpty()) {
                        tags.add(trimmed);
                    }
                }
            }

            Verifyica.Tags tagsAnnotation = method.getAnnotation(Verifyica.Tags.class);
            if (tagsAnnotation != null) {
                Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
                if (tagAnnotations != null) {
                    for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                        String tag = tagAnnotation.value();
                        if (tag != null) {
                            String trimmed = tag.trim();
                            if (!trimmed.isEmpty()) {
                                tags.add(trimmed);
                            }
                        }
                    }
                }
            }

            METHOD_TAGS_CACHE.put(method, tags);
            return tags;
        }
    }
}
