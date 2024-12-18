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

package org.verifyica.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/** Class to implement TemporaryDirectory */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class TemporaryDirectory implements AutoCloseable {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private static final String DEFAULT_PREFIX = "verifyica-temp-";

    private Path temporaryDirectory;

    /** Enum to implement CleanupMode */
    public enum CleanupMode {
        /** Always delete */
        ALWAYS,
        /** Never delete */
        NEVER
    }

    /**
     * Constructor
     *
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory() throws IOException {
        initialize(DEFAULT_PREFIX, CleanupMode.ALWAYS);
    }

    /**
     * Constructor
     *
     * @param cleanupMode cleanupMode
     *
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory(CleanupMode cleanupMode) throws IOException {
        if (cleanupMode == null) {
            throw new IllegalArgumentException("cleanupMode is null");
        }

        initialize(DEFAULT_PREFIX, cleanupMode);
    }

    /**
     * Constructor
     *
     * @param prefix prefix
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory(String prefix) throws IOException {
        initialize(prefix, CleanupMode.ALWAYS);
    }

    /**
     * Constructor
     *
     * @param prefix prefix
     * @param cleanupMode cleanupMode
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory(String prefix, CleanupMode cleanupMode) throws IOException {
        initialize(prefix, cleanupMode);
    }

    /**
     * Method to initialize the temporary directory
     *
     * @param prefix prefix
     * @param cleanupMode cleanupMode
     * @throws IOException IOException
     */
    private void initialize(String prefix, CleanupMode cleanupMode) throws IOException {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix is null");
        }

        if (prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("prefix is blank");
        }

        if (cleanupMode == null) {
            throw new IllegalArgumentException("cleanupMode is null");
        }

        temporaryDirectory = Files.createTempDirectory(prefix.trim() + UUID.randomUUID());

        if (cleanupMode == CleanupMode.ALWAYS) {
            registerShutdownHook(temporaryDirectory);
        }
    }

    /**
     * Get the temporary directory File
     *
     * @return the temporary directory File
     */
    public File file() {
        return new File(temporaryDirectory.toString());
    }

    /**
     * Get the temporary directory File
     *
     * @return the temporary directory File
     */
    public File toFile() {
        return file();
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    public Path path() {
        return getPath();
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    public Path toPath() {
        return temporaryDirectory;
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    @Deprecated
    public Path getPath() {
        return temporaryDirectory;
    }

    @Override
    public void close() throws IOException {
        delete();
    }

    /**
     * Method to delete the temporary directory
     *
     * @throws IOException IOException
     */
    public void delete() throws IOException {
        deleteRecursively(temporaryDirectory);
    }

    /**
     * Method to create a new file in the temporary directory
     *
     * @return the new file
     * @throws IOException IOException
     */
    public File newFile() throws IOException {
        return newFile(UUID.randomUUID().toString());
    }

    /**
     * Method to create a new file in the temporary directory
     *
     * @param name name
     * @return the new file
     * @throws IOException IOException
     */
    public File newFile(String name) throws IOException {
        File file = new File(temporaryDirectory.toFile(), name);
        file.createNewFile();
        return file;
    }

    /**
     * Method to recursively delete the temporary directory and all subdirectories/files
     *
     * @param path path
     * @throws IOException IOException
     */
    private void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes basicFileAttributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Method to register a shutdown hook to delete the temporary directory
     *
     * @param path path
     */
    private void registerShutdownHook(Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteRecursively(path);
            } catch (IOException e) {
                // INTENTIONALLY BLANK
            }
        }));
    }
}
