/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.api.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TemporaryDirectory Tests")
public class TemporaryDirectoryTest {

    @Test
    @DisplayName("Should create temporary directory and file")
    public void testCreateTemporaryDirectoryAndFile() throws IOException, InterruptedException {
        final String permissions = "rwx------";

        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toPath()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();
        assertThat(getLinuxPermissionString(temporaryDirectory.toPath())).isEqualTo(permissions);
        assertThat(getLinuxPermissionString(temporaryDirectory.toFile())).isEqualTo(permissions);

        final File temporaryFile = temporaryDirectory.newFile();

        assertThat(temporaryFile).exists();
        assertThat(temporaryFile).isFile();
        assertThat(temporaryFile).canRead();
        assertThat(temporaryFile).canWrite();

        assertThat(getLinuxPermissionString(temporaryFile)).isEqualTo(permissions);
    }

    @Test
    @DisplayName("Should create temporary directory and file with custom permissions")
    public void testCreateTemporaryDirectoryAndFileWithPermissions() throws IOException, InterruptedException {
        String permissions = "rwxr-xr-x";

        Set<PosixFilePermission> posixFilePermissionsSet = PosixFilePermissions.fromString(permissions);

        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory(posixFilePermissionsSet);

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toPath()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();
        assertThat(getLinuxPermissionString(temporaryDirectory.toPath())).isEqualTo(permissions);
        assertThat(getLinuxPermissionString(temporaryDirectory.toPath().toFile()))
                .isEqualTo(permissions);

        File temporaryFile = temporaryDirectory.newFile();

        assertThat(temporaryFile).exists();
        assertThat(temporaryFile).isFile();
        assertThat(temporaryFile).canRead();
        assertThat(temporaryFile).canWrite();

        assertThat(getLinuxPermissionString(temporaryFile)).isEqualTo(permissions);

        permissions = "r--r--r--";

        posixFilePermissionsSet = PosixFilePermissions.fromString(permissions);

        temporaryFile = temporaryDirectory.newFile(posixFilePermissionsSet);

        assertThat(temporaryFile).exists();
        assertThat(temporaryFile).isFile();
        assertThat(temporaryFile).canRead();
        assertThat(temporaryFile.canWrite()).isFalse();

        assertThat(getLinuxPermissionString(temporaryFile)).isEqualTo(permissions);
    }

    @Test
    @DisplayName("Should close and delete temporary directory")
    public void testClose() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        final Path path = temporaryDirectory.toPath();

        assertThat(path).exists();

        temporaryDirectory.close();

        assertThat(path).doesNotExist();
    }

    @Test
    @DisplayName("Should delete temporary directory")
    public void testDelete() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        final Path path = temporaryDirectory.toPath();

        assertThat(path).exists();

        temporaryDirectory.delete();

        assertThat(path).doesNotExist();
    }

    @Test
    @DisplayName("Should delete temporary directory with contents")
    public void testDeleteWithContents() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        final Path path = temporaryDirectory.toPath();

        // Create nested directory structure
        final File file1 = temporaryDirectory.newFile();
        final File file2 = temporaryDirectory.newFile();

        // Create subdirectory with file
        final Path subDir = path.resolve("subdir");
        Files.createDirectory(subDir);
        Files.createFile(subDir.resolve("nestedFile.txt"));

        assertThat(path).exists();
        assertThat(file1).exists();
        assertThat(file2).exists();
        assertThat(subDir).exists();

        temporaryDirectory.delete();

        assertThat(path).doesNotExist();
        assertThat(file1).doesNotExist();
        assertThat(file2).doesNotExist();
        assertThat(subDir).doesNotExist();
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    public void testEqualsAndHashCode() throws IOException {
        final TemporaryDirectory tempDir1 = TemporaryDirectory.newDirectory();
        final TemporaryDirectory tempDir2 = TemporaryDirectory.newDirectory();

        try {
            // Same object
            assertThat(tempDir1).isEqualTo(tempDir1);

            // Different objects with different paths
            assertThat(tempDir1).isNotEqualTo(tempDir2);
            assertThat(tempDir1.hashCode()).isNotEqualTo(tempDir2.hashCode());

            // Null check
            assertThat(tempDir1).isNotEqualTo(null);

            // Different type
            assertThat(tempDir1).isNotEqualTo("not a directory");

            // Different path
            final TemporaryDirectory tempDir3 = TemporaryDirectory.newDirectory();
            try {
                assertThat(tempDir1.toPath()).isNotEqualTo(tempDir3.toPath());
                assertThat(tempDir1).isNotEqualTo(tempDir3);
            } finally {
                tempDir3.delete();
            }
        } finally {
            tempDir1.delete();
            tempDir2.delete();
        }
    }

    @Test
    @DisplayName("Should return path string from toString()")
    public void testToStringReturnsPath() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThat(temporaryDirectory.toString())
                    .isEqualTo(temporaryDirectory.toPath().toString());
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    @DisplayName("Should throw exception for null permissions when creating directory")
    public void testNewDirectoryWithNullPermissions() {
        assertThatThrownBy(() -> TemporaryDirectory.newDirectory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("posixFilePermissions is null");
    }

    @Test
    @DisplayName("Should throw exception for empty permissions when creating directory")
    public void testNewDirectoryWithEmptyPermissions() {
        assertThatThrownBy(() -> TemporaryDirectory.newDirectory(new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("posixFilePermissions is empty");
    }

    @Test
    @DisplayName("Should throw exception for null permissions when creating file")
    public void testNewFileWithNullPermissions() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThatThrownBy(() -> temporaryDirectory.newFile(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("posixFilePermissions is null");
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    @DisplayName("Should throw exception for empty permissions when creating file")
    public void testNewFileWithEmptyPermissions() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThatThrownBy(() -> temporaryDirectory.newFile(new HashSet<>()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("posixFilePermissions is empty");
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    @DisplayName("Should create file with default permissions")
    public void testNewFileWithDefaultPermissions() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            final File file = temporaryDirectory.newFile();

            assertThat(file).exists();
            assertThat(file).isFile();
            assertThat(file.canRead()).isTrue();
            assertThat(file.canWrite()).isTrue();
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    @DisplayName("Should create multiple unique files in same directory")
    public void testCreateMultipleUniqueFiles() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            final File file1 = temporaryDirectory.newFile();
            final File file2 = temporaryDirectory.newFile();
            final File file3 = temporaryDirectory.newFile();

            assertThat(file1).exists();
            assertThat(file2).exists();
            assertThat(file3).exists();

            // Verify all files are different
            assertThat(file1).isNotEqualTo(file2);
            assertThat(file1).isNotEqualTo(file3);
            assertThat(file2).isNotEqualTo(file3);

            // Verify all paths are different
            assertThat(file1.toPath()).isNotEqualTo(file2.toPath());
            assertThat(file1.toPath()).isNotEqualTo(file3.toPath());
            assertThat(file2.toPath()).isNotEqualTo(file3.toPath());
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    @DisplayName("Should work with try-with-resources")
    public void testTryWithResources() throws IOException {
        final Path path;

        try (final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory()) {
            path = temporaryDirectory.toPath();
            assertThat(path).exists();

            // Create a file inside the directory
            final File file = temporaryDirectory.newFile();
            assertThat(file).exists();
        }

        // After try-with-resources, directory should be deleted
        assertThat(path).doesNotExist();
    }

    @Test
    @DisplayName("Should handle deeply nested directory structure")
    public void testDeeplyNestedDirectoryStructure() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            final Path root = temporaryDirectory.toPath();

            // Create deeply nested structure
            Path current = root;
            for (int i = 0; i < 5; i++) {
                current = current.resolve("level" + i);
                Files.createDirectory(current);
                Files.createFile(current.resolve("file" + i + ".txt"));
            }

            // Verify structure exists
            assertThat(root).exists();

            // Verify each level exists
            Path verifyPath = root;
            for (int i = 0; i < 5; i++) {
                verifyPath = verifyPath.resolve("level" + i);
                assertThat(verifyPath).exists();
                assertThat(verifyPath.resolve("file" + i + ".txt")).exists();
            }

            // Delete should clean up everything
            temporaryDirectory.delete();
            assertThat(root).doesNotExist();
        } finally {
            // Cleanup if test fails before delete
            if (temporaryDirectory.toPath().toFile().exists()) {
                temporaryDirectory.delete();
            }
        }
    }

    @Test
    @DisplayName("Should delete directory with many files")
    public void testDeleteWithManyFiles() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            final int fileCount = 100;
            final Set<File> files = new java.util.HashSet<>();

            for (int i = 0; i < fileCount; i++) {
                files.add(temporaryDirectory.newFile());
            }

            // Verify all files exist
            for (final File file : files) {
                assertThat(file).exists();
            }

            temporaryDirectory.delete();

            // Verify all files and directory are deleted
            assertThat(temporaryDirectory.toPath()).doesNotExist();
            for (final File file : files) {
                assertThat(file).doesNotExist();
            }
        } finally {
            // Cleanup if test fails before delete
            if (temporaryDirectory.toPath().toFile().exists()) {
                temporaryDirectory.delete();
            }
        }
    }

    @Test
    @DisplayName("Should create files with different permissions in same directory")
    public void testCreateFilesWithDifferentPermissions() throws IOException {
        final TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            final Set<PosixFilePermission> readWritePermissions = PosixFilePermissions.fromString("rw-------");
            final Set<PosixFilePermission> readOnlyPermissions = PosixFilePermissions.fromString("r--------");
            final Set<PosixFilePermission> readWriteExecutePermissions = PosixFilePermissions.fromString("rwx------");

            final File file1 = temporaryDirectory.newFile(readWritePermissions);
            final File file2 = temporaryDirectory.newFile(readOnlyPermissions);
            final File file3 = temporaryDirectory.newFile(readWriteExecutePermissions);

            assertThat(file1).exists();
            assertThat(file2).exists();
            assertThat(file3).exists();

            assertThat(getLinuxPermissionString(file1)).isEqualTo("rw-------");
            assertThat(getLinuxPermissionString(file2)).isEqualTo("r--------");
            assertThat(getLinuxPermissionString(file3)).isEqualTo("rwx------");
        } finally {
            temporaryDirectory.delete();
        }
    }

    private static String getLinuxPermissionString(File file) throws IOException {
        return getLinuxPermissionString(file.toPath());
    }

    private static String getLinuxPermissionString(Path path) throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);

        StringBuilder stringBuilder = new StringBuilder(9);

        stringBuilder.append(perms.contains(PosixFilePermission.OWNER_READ) ? "r" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.OWNER_WRITE) ? "w" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.OWNER_EXECUTE) ? "x" : "-");

        stringBuilder.append(perms.contains(PosixFilePermission.GROUP_READ) ? "r" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.GROUP_WRITE) ? "w" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.GROUP_EXECUTE) ? "x" : "-");

        stringBuilder.append(perms.contains(PosixFilePermission.OTHERS_READ) ? "r" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.OTHERS_WRITE) ? "w" : "-");
        stringBuilder.append(perms.contains(PosixFilePermission.OTHERS_EXECUTE) ? "x" : "-");

        return stringBuilder.toString();
    }
}
