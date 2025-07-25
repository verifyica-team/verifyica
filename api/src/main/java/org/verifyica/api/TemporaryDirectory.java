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

package org.verifyica.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class to implement TemporaryDirectory
 */
@SuppressWarnings("PMD.EmptyCatchBlock")
public class TemporaryDirectory implements AutoCloseable {

    private static final String DEFAULT_DIRECTORY_PREFIX = "verifyica-tmp-";

    private static final Set<PosixFilePermission> DEFAULT_POSIX_FILE_PERMISSIONS =
            PosixFilePermissions.fromString("rwx------");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Set<PosixFilePermission> posixFilePermissions;
    private final Path path;

    /**
     * Constructor
     *
     * @param posixFilePermissions permissions to set for the temporary directory
     * @throws IOException IOException if the temporary directory can't be created
     */
    private TemporaryDirectory(Set<PosixFilePermission> posixFilePermissions) throws IOException {
        this.path = Files.createTempDirectory(DEFAULT_DIRECTORY_PREFIX);
        this.posixFilePermissions = new HashSet<>(posixFilePermissions);

        Files.setPosixFilePermissions(path, posixFilePermissions);

        registerShutdownHook(path);
    }

    /**
     * Get the temporary directory File
     *
     * @return the temporary directory File
     */
    public File toFile() {
        return path.toFile();
    }

    /**
     * Get the temporary directory Path
     *
     * @return the temporary directory Path
     */
    public Path toPath() {
        return path;
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
        deleteRecursively(path);
    }

    /**
     * Method to create a new file in the temporary directory, using the permissions set for the temporary directory
     *
     * @return the new file
     * @throws IOException IOException
     */
    public File newFile() throws IOException {
        return newFile(posixFilePermissions);
    }

    /**
     * Method to create a new file in the temporary directory with specified permissions
     *
     * @param posixFilePermissions the permissions to set for the new file
     * @return the new file
     * @throws IOException If an I/O error occurs
     */
    public File newFile(Set<PosixFilePermission> posixFilePermissions) throws IOException {
        if (posixFilePermissions == null) {
            throw new IllegalArgumentException("posixFilePermissions is null");
        }

        if (posixFilePermissions.isEmpty()) {
            throw new IllegalArgumentException("posixFilePermissions is empty");
        }

        File file;

        do {
            file = new File(path.toFile(), generateRandomId());
        } while (!file.createNewFile());

        Files.setPosixFilePermissions(file.toPath(), posixFilePermissions);

        return file;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TemporaryDirectory that = (TemporaryDirectory) object;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
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
     * Method to create a new temporary directory with default permissions
     *
     * @return a new TemporaryDirectory
     * @throws IOException If an I/O error occurs
     */
    public static TemporaryDirectory newDirectory() throws IOException {
        return newDirectory(DEFAULT_POSIX_FILE_PERMISSIONS);
    }

    /**
     * Method to create a new temporary directory with specified POSIX file permissions
     *
     * @param posixFilePermissions the POSIX file permissions to set for the temporary directory
     * @return a new TemporaryDirectory
     * @throws IOException If an I/O error occurs
     */
    public static TemporaryDirectory newDirectory(Set<PosixFilePermission> posixFilePermissions) throws IOException {
        if (posixFilePermissions == null) {
            throw new IllegalArgumentException("posixFilePermissions is null");
        }

        if (posixFilePermissions.isEmpty()) {
            throw new IllegalArgumentException("posixFilePermissions is empty");
        }

        return new TemporaryDirectory(posixFilePermissions);
    }

    /**
     * Method to generate a random id
     *
     * @return a random id
     */
    private static String generateRandomId() {
        return String.valueOf(Long.toUnsignedString(SECURE_RANDOM.nextLong()));
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
                // INTENTIONALLY EMPTY
            }
        }));
    }
}
