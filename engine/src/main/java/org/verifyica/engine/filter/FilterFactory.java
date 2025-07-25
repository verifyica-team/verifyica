/*
 * Copyright (C) Verifyica project authors and contributors
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

package org.verifyica.engine.filter;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.exception.EngineConfigurationException;
import org.yaml.snakeyaml.Yaml;

/** Class to implement FilterFactory */
@SuppressWarnings("unchecked")
public class FilterFactory {

    private static final Map<String, Filter.Type> filterTypeMap;

    static {
        filterTypeMap = new HashMap<>();
        filterTypeMap.put("IncludeClass", Filter.Type.INCLUDE_CLASS);
        filterTypeMap.put("ExcludeClass", Filter.Type.EXCLUDE_CLASS);
        filterTypeMap.put("IncludeTaggedClass", Filter.Type.INCLUDE_TAGGED_CLASS);
        filterTypeMap.put("ExcludeTaggedClass", Filter.Type.EXCLUDE_TAGGED_CLASS);
    }

    /**
     * Constructor
     */
    private FilterFactory() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to load Filters
     *
     * @return a List of Filters
     */
    public static List<Filter> loadFilters() {
        String filtersFilename = null;

        try {
            List<Filter> filters = new ArrayList<>();

            filtersFilename = ConcreteConfiguration.getInstance()
                    .getProperties()
                    .getProperty(Constants.ENGINE_FILTER_DEFINITIONS_FILENAME, null);

            if (filtersFilename != null && !filtersFilename.trim().isEmpty()) {
                List<Object> objects = new Yaml().load(loadContents(new File(filtersFilename)));

                for (Object object : objects) {
                    Map<Object, Object> filterMap = (Map<Object, Object>) object;
                    String type = (String) filterMap.get("type");
                    boolean enabled = Boolean.TRUE.equals(filterMap.get("enabled"));

                    if (enabled) {
                        Filter.Type decodedType = filterTypeMap.getOrDefault(type, Filter.Type.UNKNOWN);
                        switch (decodedType) {
                            case INCLUDE_CLASS: {
                                String classRegex = (String) filterMap.get("classRegex");
                                filters.add(IncludeClassFilter.create(classRegex));
                                break;
                            }
                            case EXCLUDE_CLASS: {
                                String classRegex = (String) filterMap.get("classRegex");
                                filters.add(ExcludeClassFilter.create(classRegex));
                                break;
                            }
                            case INCLUDE_TAGGED_CLASS: {
                                String classTagRegex = (String) filterMap.get("classTagRegex");
                                filters.add(IncludeTaggedClassFilter.create(classTagRegex));
                                break;
                            }
                            case EXCLUDE_TAGGED_CLASS: {
                                String classTagRegex = (String) filterMap.get("classTagRegex");
                                filters.add(ExcludeTaggedClassFilter.create(classTagRegex));
                            }
                            default: {
                                throw new EngineConfigurationException(format("Invalid filter type [%s]", type));
                            }
                        }
                    }
                }
            }

            return filters;
        } catch (EngineConfigurationException e) {
            throw e;
        } catch (IOException e) {
            throw new EngineConfigurationException(
                    format("Exception loading filter definition file [%s]", filtersFilename), e);
        } catch (Throwable t) {
            throw new EngineConfigurationException(format("Invalid filter definition file [%s]", filtersFilename), t);
        }
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
}
