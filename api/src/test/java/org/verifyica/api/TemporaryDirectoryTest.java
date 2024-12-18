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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
public class TemporaryDirectoryTest {

    @Test
    @Order(1)
    public void testCreateTemporaryDirectory() throws IOException {
        TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

        assertThat(temporaryDirectory.path().toFile()).exists();
        assertThat(temporaryDirectory.getPath().toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();

        File file = temporaryDirectory.newFile();

        assertThat(file).exists();
        assertThat(file).isFile();
    }

    @Test
    @Order(2)
    public void testCreateTemporaryDirectoryAndFile() throws IOException {
        TemporaryDirectory temporaryDirectory = new TemporaryDirectory();

        assertThat(temporaryDirectory.path().toFile()).exists();
        assertThat(temporaryDirectory.getPath().toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();
    }
}
