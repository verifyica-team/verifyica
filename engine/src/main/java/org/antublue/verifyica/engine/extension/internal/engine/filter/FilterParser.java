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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class FilterParser {

    private FilterParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a List of Filters from a file
     *
     * @param yamlFile yamlFile
     * @return a List of Filters
     * @throws Throwable Throwable
     */
    public static List<Filter> parse(File yamlFile) throws Throwable {
        return parse(loadContents(yamlFile));
    }

    public static List<Filter> parse(String yamlString) throws Throwable {
        List<Filter> filters = new ArrayList<>();

        List<Object> objects = new Yaml().load(yamlString);

        for (Object object : objects) {
            Map<Object, Object> filterMap = (Map<Object, Object>) object;
            String type = (String) filterMap.get("type");

            boolean enabled = Boolean.TRUE.equals(filterMap.get("enabled"));

            if (enabled) {
                if (type.equals("GlobalClassFilter")) {
                    parseClassGlobal(filterMap, filters);
                } else if (type.equals("SpecificClassFilter")) {
                    parseClassSpecific(filterMap, filters);
                }
            }
        }

        return filters;
    }

    private static void parseClassGlobal(Map<Object, Object> filterMap, List<Filter> filters)
            throws Throwable {
        Map<Object, Object> nameMap = (Map<Object, Object>) filterMap.get("name");

        String includeNameRegex = (String) nameMap.get("include");
        String excludeNameRegex = (String) nameMap.get("exclude");

        Map<Object, Object> tagMap = (Map<Object, Object>) filterMap.get("tag");

        String includeTagRegex = (String) tagMap.get("include");
        String excludeTagRegex = (String) tagMap.get("exclude");

        filters.add(
                new GlobalClassFilter(
                        includeNameRegex, excludeNameRegex, includeTagRegex, excludeTagRegex));
    }

    private static void parseClassSpecific(Map<Object, Object> filterMap, List<Filter> filters)
            throws Throwable {
        String className = (String) filterMap.get("className");

        Map<Object, Object> methodMap = (Map<Object, Object>) filterMap.get("method");

        Map<Object, Object> nameMap = (Map<Object, Object>) methodMap.get("name");

        String includeNameRegex = (String) nameMap.get("include");
        String excludeNameRegex = (String) nameMap.get("exclude");

        Map<Object, Object> tagMap = (Map<Object, Object>) methodMap.get("tag");

        String includeTagRegex = (String) tagMap.get("include");
        String excludeTagRegex = (String) tagMap.get("exclude");

        filters.add(
                new SpecificClassFilter(
                        className,
                        includeNameRegex,
                        excludeNameRegex,
                        includeTagRegex,
                        excludeTagRegex));
    }

    private static String loadContents(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
