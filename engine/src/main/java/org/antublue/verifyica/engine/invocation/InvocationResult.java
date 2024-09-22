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

package org.antublue.verifyica.engine.invocation;

/** Class to implement InvocationResult */
public class InvocationResult {

    /** InvocationResult Type */
    public enum Type {
        /** Success */
        SUCCESS,
        /** Failure */
        FAILURE,
        /** Skipped */
        SKIPPED
    }

    private final Type type;

    /**
     * Constructor
     *
     * @param type typee
     */
    private InvocationResult(Type type) {
        this.type = type;
    }

    /**
     * Method to return if the result is success
     *
     * @return true if the result is success, else false
     */
    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    /**
     * Method to return if the result is failure
     *
     * @return true if the result is failure, else false
     */
    public boolean isFailure() {
        return type == Type.FAILURE;
    }

    /**
     * Method to return if the result is skipped
     *
     * @return true if the result is skipped, else false
     */
    public boolean isSkipped() {
        return type == Type.SKIPPED;
    }

    /**
     * Method to create an InvocationResult
     *
     * @param type type
     * @return an InvocationResult
     */
    public static InvocationResult create(Type type) {
        return new InvocationResult(type);
    }
}
