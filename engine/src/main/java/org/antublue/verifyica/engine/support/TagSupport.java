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

package org.antublue.verifyica.engine.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.antublue.verifyica.api.Verifyica;

/** Class to implement TagSupport */
public class TagSupport {

    /** Constructor */
    private TagSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a List of tags for a Class
     *
     * @param clazz clazz
     * @return a List of tags
     */
    public static List<String> getTags(Class<?> clazz) {
        ArgumentSupport.notNull(clazz, "clazz is null");

        Set<String> tags = new HashSet<>();

        getTag(clazz).ifPresent(tags::add);

        Verifyica.Tags tagsAnnotation = clazz.getAnnotation(Verifyica.Tags.class);
        if (tagsAnnotation != null) {
            Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
            if (tagAnnotations != null) {
                for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                    String tag = tagAnnotation.tag();
                    if (tag != null && !tag.trim().isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
            }
        }

        return new ArrayList<>(tags);
    }

    /**
     * Method to get a List of tags for a Method
     *
     * @param method method
     * @return a List of tags
     */
    public static List<String> getTags(Method method) {
        ArgumentSupport.notNull(method, "method is null");

        Set<String> tags = new HashSet<>();

        getTag(method).ifPresent(tags::add);

        Verifyica.Tags tagsAnnotation = method.getAnnotation(Verifyica.Tags.class);
        if (tagsAnnotation != null) {
            Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
            if (tagAnnotations != null) {
                for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                    String tag = tagAnnotation.tag();
                    if (tag != null && !tag.trim().isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
            }
        }

        return new ArrayList<>(tags);
    }

    /**
     * Method to get a tag for a Method
     *
     * @param method method
     * @return a tag
     */
    private static Optional<String> getTag(Method method) {
        Verifyica.Tag annotation = method.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                return Optional.of(tag.trim());
            }
        }

        return Optional.empty();
    }

    /**
     * Method to get a tag for a Class
     *
     * @param clazz clazz
     * @return a tag
     */
    private static Optional<String> getTag(Class<?> clazz) {
        Verifyica.Tag annotation = clazz.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String tag = annotation.tag();
            if (tag != null && !tag.trim().isEmpty()) {
                return Optional.of(tag.trim());
            }
        }

        return Optional.empty();
    }
}
