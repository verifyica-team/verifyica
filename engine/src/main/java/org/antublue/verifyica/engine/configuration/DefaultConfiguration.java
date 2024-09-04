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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.engine.common.Precondition;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;

/** Class to implement DefaultConfiguration */
public class DefaultConfiguration implements Configuration {

    private static final String VERIFYICA_CONFIGURATION_TRACE = "VERIFYICA_CONFIGURATION_TRACE";

    private static final String VERIFYICA_PROPERTIES_FILENAME = "verifyica.properties";

    private static final boolean IS_TRACE_ENABLED =
            Constants.TRUE.equals(System.getenv().get(VERIFYICA_CONFIGURATION_TRACE));

    private static final String CLASS_NAME = DefaultConfiguration.class.getName();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private Path propertiesFilename;
    private final Map<String, String> map;
    private final ReadWriteLock readWriteLock;

    /** Constructor */
    private DefaultConfiguration() {
        map = new TreeMap<>();
        readWriteLock = new ReentrantReadWriteLock(true);

        loadConfiguration();
    }

    /**
     * Constructor
     *
     * @param map map
     */
    private DefaultConfiguration(TreeMap<String, String> map) {
        this.map = map;
        readWriteLock = new ReentrantReadWriteLock(true);
    }

    @Override
    public Optional<Path> getPropertiesFilename() {
        return propertiesFilename != null ? Optional.of(propertiesFilename) : Optional.empty();
    }

    @Override
    public Optional<String> put(String key, String value) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().writeLock().lock();
        try {
            return Optional.ofNullable(map.put(key.trim(), value));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public String get(String key) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().readLock().lock();
        try {
            return map.get(key.trim());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Optional<String> getOptional(String key) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().readLock().lock();
        try {
            return Optional.ofNullable(map.get(key.trim()));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Optional<String> computeIfAbsent(String key, Function<String, String> transformer) {
        Precondition.notBlank(key, "key is null", "key is blank");
        Precondition.notNull(transformer, "transformer is null");

        getReadWriteLock().writeLock().lock();
        try {
            return Optional.ofNullable(map.computeIfAbsent(key.trim(), transformer));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(String key) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().readLock().lock();
        try {
            return map.containsKey(key.trim());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public String remove(String key) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().writeLock().lock();
        try {
            return map.remove(key.trim());
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Optional<String> removeOptional(String key) {
        Precondition.notBlank(key, "key is null", "key is blank");

        getReadWriteLock().writeLock().lock();
        try {
            return Optional.ofNullable(map.remove(key.trim()));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public int size() {
        getReadWriteLock().readLock().lock();
        try {
            return map.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Configuration clear() {
        getReadWriteLock().writeLock().lock();
        try {
            map.clear();
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Set<String> keySet() {
        getReadWriteLock().readLock().lock();
        try {
            return new TreeSet<>(map.keySet());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        getReadWriteLock().readLock().lock();
        try {
            return new TreeSet<>(map.entrySet());
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public Configuration merge(Map<String, String> map) {
        Precondition.notNull(map, "map is null");

        getReadWriteLock().writeLock().lock();
        try {
            map.entrySet().stream()
                    .filter(
                            entry ->
                                    entry.getKey() != null
                                            && !entry.getKey().trim().isEmpty()
                                            && entry.getValue() != null
                                            && !entry.getValue().trim().isEmpty())
                    .forEach(entry -> map.put(entry.getKey().trim(), entry.getValue().trim()));
            return this;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Configuration merge(Configuration configuration) {
        Precondition.notNull(configuration, "configuration is null");

        getReadWriteLock().writeLock().lock();
        try {
            configuration.getReadWriteLock().readLock().lock();
            try {
                configuration
                        .keySet()
                        .forEach(
                                key ->
                                        configuration
                                                .getOptional(key)
                                                .ifPresent(value -> map.put(key, value)));
                return this;
            } finally {
                configuration.getReadWriteLock().readLock().unlock();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Configuration replace(Map<String, String> map) {
        Precondition.notNull(map, "map is null");

        getReadWriteLock().writeLock().lock();
        try {
            clear();
            return merge(map);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Configuration replace(Configuration configuration) {
        Precondition.notNull(configuration, "configuration is null");

        getReadWriteLock().writeLock().lock();
        try {
            configuration.getReadWriteLock().readLock().lock();
            try {
                clear();
                return merge(configuration);
            } finally {
                configuration.getReadWriteLock().readLock().unlock();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public Configuration duplicate() {
        getReadWriteLock().readLock().lock();
        try {
            return new DefaultConfiguration(new TreeMap<>(map));
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "DefaultConfiguration{" + "map=" + map + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultConfiguration that = (DefaultConfiguration) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static DefaultConfiguration getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final DefaultConfiguration SINGLETON = new DefaultConfiguration();
    }

    /** Method to load configuration */
    private void loadConfiguration() {
        if (IS_TRACE_ENABLED) {
            trace("loadConfiguration()");
        }

        try {
            // Get the properties file from a system property
            Optional<File> optional =
                    Optional.ofNullable(System.getProperties().get(VERIFYICA_PROPERTIES_FILENAME))
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

                propertiesFilename = optional.get().toPath().toAbsolutePath();

                Properties properties = new Properties();

                try (Reader reader =
                        Files.newBufferedReader(optional.get().toPath(), StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }

                if (IS_TRACE_ENABLED) {
                    trace("loaded [" + optional.get().getAbsolutePath() + "]");
                }

                properties.forEach((key, value) -> map.put((String) key, (String) value));
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
            map.keySet()
                    .forEach(
                            (key) ->
                                    trace(
                                            "configuration property ["
                                                    + key
                                                    + "] = ["
                                                    + map.get(key)
                                                    + "]"));
        }
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
            System.out.println(
                    LocalDateTime.now().format(DATE_TIME_FORMATTER)
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
