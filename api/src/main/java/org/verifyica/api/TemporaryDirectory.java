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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

/** Class to implement TemporaryDirectory */
public class TemporaryDirectory implements AutoCloseable {

    private static final String DEFAULT_PREFIX = "temp-";

    private final Path temporaryDirectory;

    /**
     * Constructor
     *
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory() throws IOException {
        this(DEFAULT_PREFIX);
    }

    /**
     * Constructor
     *
     * @param prefix prefix
     * @throws IOException IOException if the temporary directory can't be created
     */
    public TemporaryDirectory(String prefix) throws IOException {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix is null");
        }

        if (prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("prefix is blank");
        }

        temporaryDirectory = Files.createTempDirectory(prefix.trim() + UUID.randomUUID());
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    public Path path() {
        return temporaryDirectory;
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    public Path getPath() {
        return path();
    }

    @Override
    public void close() throws IOException {
        deleteRecursively(temporaryDirectory);
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
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
