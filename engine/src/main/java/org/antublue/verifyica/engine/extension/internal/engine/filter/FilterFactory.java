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

/** Class to implement FilterFactory */
@SuppressWarnings("unchecked")
public class FilterFactory {

    /** Constructor */
    private FilterFactory() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to load Filters
     *
     * @return a List of Filters
     */
    public List<Filter> loadFilters() {
        try {
            List<Filter> filters = new ArrayList<>();

            String filtersFilename =
                    DefaultEngineContext.getInstance()
                            .getConfiguration()
                            .getOptional(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME)
                            .orElse(null);

            if (filtersFilename != null && !filtersFilename.trim().isEmpty()) {
                List<Object> objects = new Yaml().load(loadContents(new File(filtersFilename)));

                for (Object object : objects) {
                    Map<Object, Object> filterMap = (Map<Object, Object>) object;
                    String type = (String) filterMap.get("type");
                    boolean enabled = Boolean.TRUE.equals(filterMap.get("enabled"));
                    String classRegex = (String) filterMap.get("classRegex");
                    String methodRegex = (String) filterMap.get("methodRegex");

                    if (enabled) {
                        if (type.equals("Include")) {
                            filters.add(IncludeFilter.create(classRegex, methodRegex));
                        } else if (type.equals("Exclude")) {
                            filters.add(ExcludeFilter.create(classRegex, methodRegex));
                        } else {
                            throw new EngineConfigurationException(
                                    format("Invalid filter definition type [%s]", type));
                        }
                    }
                }
            }

            return filters;
        } catch (EngineConfigurationException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineConfigurationException("Invalid filter definitions", t);
        }
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static FilterFactory getInstance() {
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
        private static final FilterFactory SINGLETON = new FilterFactory();
    }
}
