/*
 * Copyright 2024-present Verifyica project authors and contributors. All rights reserved.
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

/**
 * UncheckedClassNotFoundException is an unchecked wrapper for ClassNotFoundException.
 */
public class UncheckedClassNotFoundException extends RuntimeException {

    /**
     * Constructs a new UncheckedClassNotFoundException with the specified message and cause.
     *
     * @param message the detail message
     * @param classNotFoundException the cause of this exception
     */
    public UncheckedClassNotFoundException(String message, ClassNotFoundException classNotFoundException) {
        super(message, classNotFoundException);
    }

    /**
     * Propagates (throws) this exception as an unchecked exception.
     */
    public void throwUnchecked() {
        throw this;
    }

    /**
     * Wraps and propagates (throws) the specified ClassNotFoundException as an unchecked exception.
     *
     * @param classNotFoundException the ClassNotFoundException to wrap and throw
     */
    public static void throwUnchecked(ClassNotFoundException classNotFoundException) {
        new UncheckedClassNotFoundException(classNotFoundException.getMessage(), classNotFoundException)
                .throwUnchecked();
    }
}
