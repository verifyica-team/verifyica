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

package org.verifyica.engine.support;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.common.Precondition;

/** Class to implement TagSupport */
public class TagSupport {

    /** Constructor */
    private TagSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to get a Set of tags for a Class
     *
     * @param clazz clazz
     * @return a Set of tags
     */
    public static Set<String> getTags(Class<?> clazz) {
        Precondition.notNull(clazz, "clazz is null");

        Set<String> tags = new HashSet<>();

        Verifyica.Tag annotation = clazz.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String tag = annotation.value();
            if (tag != null && !tag.trim().isEmpty()) {
                tags.add(tag.trim());
            }
        }

        Verifyica.Tags tagsAnnotation = clazz.getAnnotation(Verifyica.Tags.class);
        if (tagsAnnotation != null) {
            Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
            if (tagAnnotations != null) {
                for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                    String tag = tagAnnotation.value();
                    if (tag != null && !tag.trim().isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
            }
        }

        return tags;
    }

    /**
     * Method to get a Set of tags for a Method
     *
     * @param method method
     * @return a Set of tags
     */
    public static Set<String> getTags(Method method) {
        Precondition.notNull(method, "method is null");

        Set<String> tags = new HashSet<>();

        Verifyica.Tag annotation = method.getAnnotation(Verifyica.Tag.class);
        if (annotation != null) {
            String tag = annotation.value();
            if (tag != null && !tag.trim().isEmpty()) {
                tags.add(tag.trim());
            }
        }

        Verifyica.Tags tagsAnnotation = method.getAnnotation(Verifyica.Tags.class);
        if (tagsAnnotation != null) {
            Verifyica.Tag[] tagAnnotations = tagsAnnotation.value();
            if (tagAnnotations != null) {
                for (Verifyica.Tag tagAnnotation : tagAnnotations) {
                    String tag = tagAnnotation.value();
                    if (tag != null && !tag.trim().isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
            }
        }

        return tags;
    }
}
