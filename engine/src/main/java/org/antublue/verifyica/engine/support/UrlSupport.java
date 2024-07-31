/*
 * Copyright (C) 2024 The Verifyica project authors
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

package org.antublue.verifyica.engine.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/** Class to implement UrlSupport */
public class UrlSupport {

    /** Constructor */
    private UrlSupport() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to create an InputStream from a URL
     *
     * @param url url
     * @return an InputStream for the URL
     * @throws IOException IOException
     */
    public static InputStream createInputStream(URL url) throws IOException {
        ArgumentSupport.notNull(url, "url is null");

        URLConnection connection = url.openConnection();
        connection.connect();
        return connection.getInputStream();
    }

    /**
     * Method to create a BufferedReader from a URL
     *
     * @param url url
     * @return a BufferedReader for the URL
     * @throws IOException IOException
     */
    public static BufferedReader createBufferedReader(URL url) throws IOException {
        ArgumentSupport.notNull(url, "url is null");

        return new BufferedReader(
                new InputStreamReader(createInputStream(url), StandardCharsets.UTF_8));
    }
}
