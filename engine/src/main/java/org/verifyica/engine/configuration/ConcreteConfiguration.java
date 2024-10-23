/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.engine.configuration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.OrderedProperties;
import org.verifyica.engine.exception.EngineConfigurationException;

/** Class to implement ConcreteConfiguration */
public class ConcreteConfiguration implements Configuration {

    private static final String VERIFYICA_CONFIGURATION_TRACE = "VERIFYICA_CONFIGURATION_TRACE";

    private static final String VERIFYICA_PROPERTIES_FILENAME = "verifyica.properties";

    private static final boolean IS_TRACE_ENABLED =
            Constants.TRUE.equals(System.getenv().get(VERIFYICA_CONFIGURATION_TRACE));

    private static final String CLASS_NAME = ConcreteConfiguration.class.getName();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private final Properties properties;
    private Path propertiesPath;

    /** Constructor */
    private ConcreteConfiguration() {
        properties = load();
    }

    @Override
    public Optional<Path> propertiesPath() {
        return Optional.ofNullable(propertiesPath);
    }

    @Override
    public Properties properties() {
        return properties;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static ConcreteConfiguration getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final ConcreteConfiguration SINGLETON = new ConcreteConfiguration();
    }

    /**
     * Method to load configuration Properties
     */
    private Properties load() {
        if (IS_TRACE_ENABLED) {
            trace("load()");
        }

        Properties properties = new OrderedProperties();

        try {
            // Get the properties file from a system property
            Optional<File> optional = Optional.ofNullable(System.getProperties().get(VERIFYICA_PROPERTIES_FILENAME))
                    .map(value -> new File(value.toString()).getAbsoluteFile());

            // If a system property didn't exist, scan from the
            // current directory to the root directory
            if (!optional.isPresent()) {
                optional = find(Paths.get("."), VERIFYICA_PROPERTIES_FILENAME);
            }

            if (optional.isPresent()) {
                if (IS_TRACE_ENABLED) {
                    trace("loading [" + optional.get().getAbsolutePath() + "]");
                }

                propertiesPath = optional.get().toPath().toAbsolutePath();

                try (Reader reader = Files.newBufferedReader(optional.get().toPath(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }

                if (IS_TRACE_ENABLED) {
                    trace("loaded [" + optional.get().getAbsolutePath() + "]");
                }
            } else {
                if (IS_TRACE_ENABLED) {
                    trace("no configuration properties file found");
                }
            }
        } catch (IOException e) {
            throw new EngineConfigurationException("Exception loading configuration properties", e);
        }

        if (IS_TRACE_ENABLED) {
            trace("configuration properties ...");
            properties.forEach((key, value) -> trace("configuration property [" + key + "] = [" + value + "]"));
        }

        return properties;
    }

    /**
     * Method to find a properties file, searching the working directory, then parent directories
     * toward the root
     *
     * @param path path
     * @param filename filename
     * @return an Optional containing a File if found, else an empty Optional
     */
    private static Optional<File> find(Path path, String filename) {
        Path currentPath = path.toAbsolutePath().normalize();

        while (true) {
            if (IS_TRACE_ENABLED) {
                String currentPathString = currentPath.toString();
                if (!currentPathString.endsWith("/")) {
                    currentPathString += "/";
                }

                trace("searching path [" + currentPathString + "]");
            }

            File file = new File(currentPath.toAbsolutePath() + File.separator + filename);
            if (file.exists() && file.isFile() && file.canRead()) {
                if (IS_TRACE_ENABLED) {
                    trace("found [" + file.getAbsoluteFile() + "]");
                }
                return Optional.of(file);
            }

            currentPath = currentPath.getParent();
            if (currentPath == null) {
                break;
            }

            currentPath = currentPath.toAbsolutePath().normalize();
        }

        return Optional.empty();
    }

    /**
     * Method to log a TRACE message
     *
     * @param message message
     */
    private static void trace(String message) {
        if (IS_TRACE_ENABLED) {
            System.out.println(LocalDateTime.now().format(DATE_TIME_FORMATTER)
                    + " | "
                    + Thread.currentThread().getName()
                    + " | "
                    + "TRACE"
                    + " | "
                    + CLASS_NAME
                    + " | "
                    + message
                    + " ");
        }
    }
}
