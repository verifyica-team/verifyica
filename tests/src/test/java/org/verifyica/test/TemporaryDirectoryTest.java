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

package org.verifyica.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.verifyica.api.ArgumentContext;
import org.verifyica.api.Verifyica;
import org.verifyica.api.util.TemporaryDirectory;

public class TemporaryDirectoryTest {

    private static final String TEMPORARY_DIRECTORY_KEY = "temporaryDirectory";

    private static final String TEMPORARY_FILE_KEY = "temporaryFile";

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
    public void createTemporaryDirectory(ArgumentContext argumentContext) throws IOException {
        TemporaryDirectory temporaryDirectory = TemporaryDirectory.newDirectory();

        argumentContext.getMap().put(TEMPORARY_DIRECTORY_KEY, temporaryDirectory);

        System.out.printf(
                "argument [%s] temporary directory [%s]%n",
                argumentContext.getArgument().getPayload(), temporaryDirectory);

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
    }

    @Verifyica.Test
    public void createTemporaryFile(ArgumentContext argumentContext) throws IOException {
        TemporaryDirectory temporaryDirectory = argumentContext.getMap().getAs(TEMPORARY_DIRECTORY_KEY);

        System.out.printf(
                "argument [%s] temporary directory [%s]%n",
                argumentContext.getArgument().getPayload(), temporaryDirectory);

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();

        File temporaryFile = temporaryDirectory.newFile();

        System.out.printf(
                "argument [%s] temporary file [%s]%n",
                argumentContext.getArgument().getPayload(), temporaryFile);

        assertThat(temporaryFile).exists();

        argumentContext.getMap().put(TEMPORARY_FILE_KEY, temporaryFile);
    }

    @Verifyica.AfterAll
    public void afterAll(ArgumentContext argumentContext) throws IOException {
        File temporaryFile = argumentContext.getMap().removeAs(TEMPORARY_FILE_KEY);

        assertThat(temporaryFile).exists();
        assertThat(temporaryFile).canRead();
        assertThat(temporaryFile).canWrite();
        assertThat(temporaryFile).isExecutable();
        assertThat(temporaryFile).isFile();

        TemporaryDirectory temporaryDirectory = argumentContext.getMap().removeAs(TEMPORARY_DIRECTORY_KEY);

        assertThat(temporaryDirectory.toPath()).exists();
        assertThat(temporaryDirectory.toFile()).exists();
        assertThat(temporaryDirectory.toFile()).canRead();
        assertThat(temporaryDirectory.toFile()).canWrite();
        assertThat(temporaryDirectory.toPath()).isExecutable();
        assertThat(temporaryDirectory.toFile()).isExecutable();
        assertThat(temporaryDirectory.toPath()).isDirectory();
        assertThat(temporaryDirectory.toFile()).isDirectory();

        temporaryDirectory.close();

        assertThat(temporaryFile).doesNotExist();
        assertThat(temporaryDirectory.toPath()).doesNotExist();
        assertThat(temporaryDirectory.toFile()).doesNotExist();
    }
}
