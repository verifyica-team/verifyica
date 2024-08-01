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

package org.antublue.verifyica.engine.interceptor.internal.engine.filter;

import java.lang.reflect.Method;
import org.antublue.verifyica.engine.support.TagSupport;

/** Class to implement IncludeTaggedClassFilter */
public class IncludeTaggedClassFilter extends AbstractFilter {

    /**
     * Constructor
     *
     * @param classNameRegex classNameRegex
     * @param methodNameRegex methodNameRegex
     */
    private IncludeTaggedClassFilter(String classNameRegex, String methodNameRegex) {
        super(classNameRegex, methodNameRegex);
    }

    @Override
    public Type getType() {
        return Type.INCLUDE_TAGGED_CLASS;
    }

    @Override
    public boolean matches(Class<?> testClass, Method testMethod) {
        for (String tag : TagSupport.getTags(testClass)) {
            if (getClassNamePattern().matcher(tag).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to create an IncludeFilter
     *
     * @param classRegex classRegex
     * @return an IncludeFilter
     */
    public static IncludeTaggedClassFilter create(String classRegex) {
        return new IncludeTaggedClassFilter(classRegex, "");
    }
}
