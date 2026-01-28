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

package org.verifyica.examples.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to load resources
 */
public final class Resource {

    /**
     * Constructor
     */
    private Resource() {
        // utility class
    }

    /**
     * Method to load a resource from the classpath as a list of strings
     *
     * @param clazz                the class to derive the package from
     * @param relativeResourceName the relative resource name
     * @return the list of strings
     * @throws IOException when an I/O error occurs
     */
    public static List<String> load(Class<?> clazz, String relativeResourceName) throws IOException {
        String packageName = clazz.getPackage().getName().replace('.', '/');

        String qualifiedResourceName;
        if (relativeResourceName.startsWith("/")) {
            qualifiedResourceName = packageName + relativeResourceName;
        } else {
            qualifiedResourceName = packageName + "/" + relativeResourceName;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(qualifiedResourceName);
        if (inputStream == null) {
            throw new IOException("Classpath resource not found: " + qualifiedResourceName);
        }

        try (BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return bufferedReader
                    .lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.startsWith("#"))
                    .collect(Collectors.toList());
        }
    }
}
