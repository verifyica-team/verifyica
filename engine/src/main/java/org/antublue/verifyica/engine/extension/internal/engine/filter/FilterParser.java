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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class FilterParser {

    /** Constructor */
    private FilterParser() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to parse a List of Filters from a file
     *
     * @param yamlFile yamlFile
     * @return a List of Filters
     * @throws IOException IOException
     */
    public static List<Filter> parse(File yamlFile) throws IOException {
        return parse(loadContents(yamlFile));
    }

    /**
     * Method to parse a List of Filters from a String
     *
     * @param yamlString yamlString
     * @return a List of Filters
     */
    public static List<Filter> parse(String yamlString) {
        List<Filter> filters = new ArrayList<>();

        List<Object> objects = new Yaml().load(yamlString);
        for (Object object : objects) {
            Map<Object, Object> filterMap = (Map<Object, Object>) object;
            String type = (String) filterMap.get("type");
            boolean enabled = Boolean.TRUE.equals(filterMap.get("enabled"));
            String regex = (String) filterMap.get("regex");

            if (type != null) {
                type = type.trim();
            }

            if (enabled) {
                if (type.equals("IncludeClassNameFilter")) {
                    filters.add(new IncludeClassNameFilter(regex));
                } else if (type.equals("ExcludeClassNameFilter")) {
                    filters.add(new ExcludeClassNameFilter(regex));
                } else {
                    throw new EngineConfigurationException(
                            format("Invalid filter type [%s]", type));
                }
            }
        }

        return filters;
    }

    private static String loadContents(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
