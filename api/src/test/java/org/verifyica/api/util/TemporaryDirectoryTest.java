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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
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
