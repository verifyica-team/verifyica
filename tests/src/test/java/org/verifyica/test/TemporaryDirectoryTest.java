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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.TemporaryDirectory;
import org.verifyica.api.Verifyica;

public class TemporaryDirectoryTest {

    private static final String TEMPORARY_DIRECTORY_DEFAULT_PREFIX = "temporary.directory.default.prefix";
    private static final String TEMPORARY_DIRECTORY_CUSTOM_PREFIX = "temporary.directory.custom.prefix";
    private static final String TEMPORARY_DIRECTORY_CUSTOM_PREFIX_3 = "temporary.directory.custom.prefix.2";

    @Verifyica.ArgumentSupplier(parallelism = 4)
    public static Object arguments() {
        Collection<String> collection = new ArrayList<>();

        collection.add("test-1");
        collection.add("test-2");
        collection.add("test-3");
        collection.add("test-4");

        return collection;
    }

    @Verifyica.Test
    public void createTemporaryDirectory1(ArgumentContext argumentContext) throws Throwable {
        TemporaryDirectory temporaryDirectory = new TemporaryDirectory();
        argumentContext.map().put(TEMPORARY_DIRECTORY_DEFAULT_PREFIX, temporaryDirectory);

        System.out.printf(
                "argument [%s] temporary directory [%s]%n", argumentContext.testArgumentPayload(), temporaryDirectory);

        assertThat(temporaryDirectory.path().toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).exists();
        assertThat(temporaryDirectory.path()).isSameAs(temporaryDirectory.toPath());
    }

    @Verifyica.Test
    public void createTemporaryDirectory2(ArgumentContext argumentContext) throws Throwable {
        TemporaryDirectory temporaryDirectory = new TemporaryDirectory("foo-");
        argumentContext.map().put(TEMPORARY_DIRECTORY_CUSTOM_PREFIX, temporaryDirectory);

        System.out.printf(
                "argument [%s] temporary directory [%s]%n", argumentContext.testArgumentPayload(), temporaryDirectory);

        assertThat(temporaryDirectory.path().toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).exists();
        assertThat(temporaryDirectory.path()).isSameAs(temporaryDirectory.toPath());
    }

    @Verifyica.Test
    public void createTemporaryFile(ArgumentContext argumentContext) throws Throwable {
        TemporaryDirectory temporaryDirectory = new TemporaryDirectory();
        argumentContext.map().put(TEMPORARY_DIRECTORY_CUSTOM_PREFIX_3, temporaryDirectory);

        System.out.printf(
                "argument [%s] temporary directory [%s]%n", argumentContext.testArgumentPayload(), temporaryDirectory);

        assertThat(temporaryDirectory.path().toFile()).exists();
        assertThat(temporaryDirectory.toPath().toFile()).exists();
        assertThat(temporaryDirectory.path()).isSameAs(temporaryDirectory.toPath());

        File temporaryFile = temporaryDirectory.newFile();

        assertThat(temporaryFile).exists();

        File temporaryFile2 = temporaryDirectory.newFile("test1.txt");

        assertThat(temporaryFile2).exists();

        File temporaryFile3 = new File(temporaryDirectory.toPath().toAbsolutePath() + "/" + "test2.txt");
        temporaryFile3.createNewFile();

        assertThat(temporaryFile3).exists();
    }

    @Verifyica.Conclude
    public void conclude() throws Throwable {
        // Assert that no temporary directories exist since they were scoped
        // to the ArgumentContext which gets cleaned up after testing the argument
        assertThat(noFileWithPrefixExists("temp-")).isTrue();
        assertThat(noFileWithPrefixExists("foo-")).isTrue();
        assertThat(noFileWithPrefixExists("test1.txt")).isTrue();
        assertThat(noFileWithPrefixExists("test2.txt")).isTrue();
    }

    private static boolean noFileWithPrefixExists(String prefix) throws Throwable {
        Path temporaryDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        try (Stream<Path> files = Files.list(temporaryDirectory)) {
            return files.noneMatch(file -> file.getFileName().toString().startsWith(prefix));
        }
    }
}
