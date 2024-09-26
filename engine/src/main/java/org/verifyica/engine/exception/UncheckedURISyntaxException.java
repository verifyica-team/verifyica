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

package org.verifyica.engine.exception;

import java.net.URISyntaxException;

/** Class to implement UncheckedURISyntaxException */
public class UncheckedURISyntaxException extends RuntimeException {

    /**
     * Constructor
     *
     * @param message message
     * @param uriSyntaxException uriSyntaxException
     */
    public UncheckedURISyntaxException(String message, URISyntaxException uriSyntaxException) {
        super(message, uriSyntaxException);
    }

    /** Method to propagate (throw) the exception */
    public void throwUnchecked() {
        throw this;
    }

    /**
     * Method to wrap and propagate (throw) the exception
     *
     * @param uriSyntaxException uriSyntaxException
     */
    public static void throwUnchecked(URISyntaxException uriSyntaxException) {
        new UncheckedURISyntaxException(uriSyntaxException.getMessage(), uriSyntaxException).throwUnchecked();
    }
}
