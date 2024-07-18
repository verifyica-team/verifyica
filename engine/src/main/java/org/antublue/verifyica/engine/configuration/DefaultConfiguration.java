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

package org.antublue.verifyica.engine.configuration;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.engine.Version;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

/** Class to implement DefaultConfiguration */
public class DefaultConfiguration implements Configuration {

    private static final String VERIFYICA_CONFIGURATION_TRACE = "VERIFYICA_CONFIGURATION_TRACE";

    private static final String VERIFYICA_PROPERTIES_FILENAME = "verifyica.properties";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private boolean IS_TRACE_ENABLED;

    private final Map<String, String> map;
    private final ImmutableConfiguration immutableConfiguration;
    private final ConfigurationParameters configurationParameters;

    /** Constructor */
    public DefaultConfiguration() {
        map = Collections.synchronizedMap(new TreeMap<>());
        immutableConfiguration = new ImmutableConfiguration(this);
        configurationParameters = new DefaultConfigurationParameters(this);

        load();
    }

    /**
     * Method to put a Configuration key / value
     *
     * @param key key
     * @param value value
     * @return this
     */
    public DefaultConfiguration setProperty(String key, String value) {
        Preconditions.notNull(key, "key is null");
        Preconditions.condition(!key.trim().isEmpty(), "key is empty");

        map.put(key, value);

        return this;
    }

    @Override
    public String getProperty(String key) {
        Preconditions.notNull(key, "key is null");
        Preconditions.condition(!key.trim().isEmpty(), "key is empty");

        return map.get(key);
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /**
     * Method to get an immutable version
     *
     * @return an immutable version
     */
    public Configuration asImmutable() {
        return immutableConfiguration;
    }

    /**
     * Method to get a ConfigurationParameters
     *
     * @return an immutable version
     */
    public ConfigurationParameters asConfigurationParameters() {
        return configurationParameters;
    }

    /**
     * Method to get the singleton instance
     *
     * @return the singleton instance
     */
    public static DefaultConfiguration getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Method to load configuration */
    private void load() {
        try {
            // Get the properties file from a system property
            Optional<File> optional =
                    Optional.ofNullable(System.getProperties().get(VERIFYICA_PROPERTIES_FILENAME))
                            .map(value -> new File(value.toString()).getAbsoluteFile());

            // If a system property didn't exist, scan the current directory back to the root
            // directory
            if (!optional.isPresent()) {
                optional = find(Paths.get("."), VERIFYICA_PROPERTIES_FILENAME);
            }

            if (optional.isPresent()) {
                if (IS_TRACE_ENABLED) {
                    trace(
                            VERIFYICA_CONFIGURATION_TRACE
                                    + " ["
                                    + optional.get().getAbsolutePath()
                                    + "]");
                }

                Properties properties = new Properties();

                try (Reader reader =
                        Files.newBufferedReader(optional.get().toPath(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }

                properties.forEach((key, value) -> map.put((String) key, (String) value));
                map.put(VERIFYICA_CONFIGURATION_TRACE, optional.get().getAbsolutePath());
            }
        } catch (IOException e) {
            throw new EngineConfigurationException("Exception loading properties", e);
        }

        map.put(Constants.VERSION, Version.version());

        if (Constants.TRUE.equals(System.getenv().get(VERIFYICA_CONFIGURATION_TRACE))) {
            IS_TRACE_ENABLED = true;
        }

        if (IS_TRACE_ENABLED) {
            map.forEach((key, value) -> trace(key + " = [" + value + "]"));
        }
    }

    /**
     * Method to find a properties file, searching the working directory, then parent directories
     * toward the root
     *
     * @param path path
     * @param filename filename
     * @return a optional containing a File
     */
    private static Optional<File> find(Path path, String filename) {
        Path currentPath = path.toAbsolutePath().normalize();

        while (true) {
            File file = new File(currentPath.toAbsolutePath() + File.separator + filename);
            if (file.exists() && file.isFile() && file.canRead()) {
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
     * Method to load a TRACE message
     *
     * @param message message
     */
    private void trace(String message) {
        if (IS_TRACE_ENABLED) {
            String dateTime;

            synchronized (SIMPLE_DATE_FORMAT) {
                dateTime = SIMPLE_DATE_FORMAT.format(new Date());
            }

            System.out.println(
                    dateTime
                            + " | "
                            + Thread.currentThread().getName()
                            + " | "
                            + "TRACE"
                            + " | "
                            + DefaultConfiguration.class.getName()
                            + " | "
                            + message
                            + " ");
        }
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final DefaultConfiguration SINGLETON = new DefaultConfiguration();
    }
}
