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
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("unchecked")
public class FilterDefinitionFactory {

    /** Constructor */
    private FilterDefinitionFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to load FilterDefinitions
     *
     * @return a List of FilterDefinitions
     */
    public List<FilterDefinition> loadFilterDefinitions() throws Throwable {
        List<FilterDefinition> filterDefinitions = new ArrayList<>();

        String filterDefinitionsFilename =
                DefaultEngineContext.getInstance()
                        .getConfiguration()
                        .getOptional(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME)
                        .orElse(null);

        if (filterDefinitionsFilename != null && !filterDefinitionsFilename.trim().isEmpty()) {
            List<Object> objects =
                    new Yaml().load(loadContents(new File(filterDefinitionsFilename)));
            for (Object object : objects) {
                Map<Object, Object> filterMap = (Map<Object, Object>) object;
                String type = (String) filterMap.get("type");
                boolean enabled = Boolean.TRUE.equals(filterMap.get("enabled"));
                String regex = (String) filterMap.get("regex");

                if (type == null) {
                    throw new EngineConfigurationException(
                            "Invalid filter definition, missing \"type\"");
                }

                if (regex == null) {
                    throw new EngineConfigurationException(
                            "Invalid filter definition, missing \"regex\"");
                }

                type = type.trim();
                if (type.isEmpty()) {
                    throw new EngineConfigurationException(
                            "Invalid filter definition, \"type\" is empty");
                }

                regex = regex.trim();
                if (regex.isEmpty()) {
                    throw new EngineConfigurationException(
                            "Invalid filter definition, \"regex\" is empty");
                }

                if (enabled) {
                    if (type.equals("IncludeClassName")) {
                        filterDefinitions.add(new IncludeClassNameFilterDefinition(regex));
                    } else if (type.equals("ExcludeClassName")) {
                        filterDefinitions.add(new ExcludeClassNameFilterDefinition(regex));
                    } else {
                        throw new EngineConfigurationException(
                                format("Invalid filter definition type [%s]", type));
                    }
                }
            }
        }

        return filterDefinitions;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static FilterDefinitionFactory getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /**
     * Method to load a File contents
     *
     * @param file file
     * @return the files contents
     * @throws IOException IOException
     */
    private static String loadContents(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final FilterDefinitionFactory SINGLETON = new FilterDefinitionFactory();
    }
}
