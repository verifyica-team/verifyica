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

package org.verifyica.engine.exception;

import java.net.URISyntaxException;

/**
 * UncheckedURISyntaxException is an unchecked wrapper for URISyntaxException.
 */
public class UncheckedURISyntaxException extends RuntimeException {

    /**
     * Constructs a new UncheckedURISyntaxException with the specified message and cause.
     *
     * @param message the detail message
     * @param uriSyntaxException the cause of this exception
     */
    public UncheckedURISyntaxException(String message, URISyntaxException uriSyntaxException) {
        super(message, uriSyntaxException);
    }

    /**
     * Propagates (throws) this exception as an unchecked exception.
     */
    public void throwUnchecked() {
        throw this;
    }

    /**
     * Wraps and propagates (throws) the specified URISyntaxException as an unchecked exception.
     *
     * @param uriSyntaxException the URISyntaxException to wrap and throw
     */
    public static void throwUnchecked(URISyntaxException uriSyntaxException) {
        new UncheckedURISyntaxException(uriSyntaxException.getMessage(), uriSyntaxException).throwUnchecked();
    }
}
