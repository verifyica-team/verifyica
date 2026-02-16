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
import org.junit.jupiter.api.Test;

public class TemporaryDirectoryTest {

    @Test
    public void testCreateTemporaryDirectoryAndFile() throws IOException, InterruptedException {
        String permissions = "rwx------";

        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toPath()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();
        assertThat(getLinuxPermissionString(temporaryDirectory.toPath())).isEqualTo(permissions);
        assertThat(getLinuxPermissionString(temporaryDirectory.toFile())).isEqualTo(permissions);

        File temporaryFile = temporaryDirectory.newFile();

        assertThat(temporaryFile).exists();
        assertThat(temporaryFile).isFile();
        assertThat(temporaryFile).canRead();
        assertThat(temporaryFile).canWrite();

        assertThat(getLinuxPermissionString(temporaryFile)).isEqualTo(permissions);
    }

    @Test
    public void testCreateTemporaryDirectoryAndFileWithPermissions() throws IOException, InterruptedException {
        String permissions = "rwxr-xr-x";

        Set<PosixFilePermission> posixFilePermissionsSet = PosixFilePermissions.fromString(permissions);

        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory(posixFilePermissionsSet);

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
    public void testClose() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        Path path = temporaryDirectory.toPath();

        assertThat(path).exists();

        temporaryDirectory.close();

        assertThat(path).doesNotExist();
    }

    @Test
    public void testDelete() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        Path path = temporaryDirectory.toPath();

        assertThat(path).exists();

        temporaryDirectory.delete();

        assertThat(path).doesNotExist();
    }

    @Test
    public void testDeleteWithContents() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        Path path = temporaryDirectory.toPath();

        // Create nested directory structure
        File file1 = temporaryDirectory.newFile();
        File file2 = temporaryDirectory.newFile();

        // Create subdirectory with file
        Path subDir = path.resolve("subdir");
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
    public void testEqualsAndHashCode() throws IOException {
        TemporaryDirectory tempDir1 = TemporaryDirectory.newDirectory();
        TemporaryDirectory tempDir2 = TemporaryDirectory.newDirectory();

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
            TemporaryDirectory tempDir3 = TemporaryDirectory.newDirectory();
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
    public void testToStringReturnsPath() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThat(temporaryDirectory.toString())
                    .isEqualTo(temporaryDirectory.toPath().toString());
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    public void testNewDirectoryWithNullPermissions() {
        assertThatThrownBy(() -> TemporaryDirectory.newDirectory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("posixFilePermissions is null");
    }

    @Test
    public void testNewDirectoryWithEmptyPermissions() {
        assertThatThrownBy(() -> TemporaryDirectory.newDirectory(new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("posixFilePermissions is empty");
    }

    @Test
    public void testNewFileWithNullPermissions() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThatThrownBy(() -> temporaryDirectory.newFile(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("posixFilePermissions is null");
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    public void testNewFileWithEmptyPermissions() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            assertThatThrownBy(() -> temporaryDirectory.newFile(new HashSet<>()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("posixFilePermissions is empty");
        } finally {
            temporaryDirectory.delete();
        }
    }

    @Test
    public void testNewFileWithDefaultPermissions() throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();
        try {
            File file = temporaryDirectory.newFile();

            assertThat(file).exists();
            assertThat(file).isFile();
            assertThat(file.canRead()).isTrue();
            assertThat(file.canWrite()).isTrue();
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
