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

package org.verifyica.engine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UrlSupport Tests")
public class UrlSupportTest {

    @Test
    @DisplayName("Should throw exception for null URL in createInputStream")
    public void shouldThrowExceptionForNullUrlInCreateInputStream() {
        assertThatThrownBy(() -> UrlSupport.createInputStream(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url is null");
    }

    @Test
    @DisplayName("Should throw exception for null URL in createBufferedReader")
    public void shouldThrowExceptionForNullUrlInCreateBufferedReader() {
        assertThatThrownBy(() -> UrlSupport.createBufferedReader(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url is null");
    }

    @Test
    @DisplayName("Should create input stream from valid URL")
    public void shouldCreateInputStreamFromValidUrl() throws IOException {
        // Use a known resource URL
        URL url = getClass().getResource("/");

        if (url != null) {
            try (InputStream is = UrlSupport.createInputStream(url)) {
                assertThat(is).isNotNull();
            }
        }
    }

    @Test
    @DisplayName("Should create buffered reader from valid URL")
    public void shouldCreateBufferedReaderFromValidUrl() throws IOException {
        // Use a known resource URL
        URL url = getClass().getResource("/");

        if (url != null) {
            try (BufferedReader reader = UrlSupport.createBufferedReader(url)) {
                assertThat(reader).isNotNull();
            }
        }
    }
}
