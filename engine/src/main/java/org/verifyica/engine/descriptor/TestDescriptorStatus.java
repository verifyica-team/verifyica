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

package org.verifyica.engine.descriptor;

import java.util.Objects;

/**
 * Represents the status of a test descriptor execution.
 */
public class TestDescriptorStatus {

    private static final TestDescriptorStatus PASSED = new TestDescriptorStatus(Type.PASSED);
    private static final TestDescriptorStatus SKIPPED = new TestDescriptorStatus(Type.SKIPPED);

    /**
     * InvocationResult Type
     */
    public enum Type {

        /**
         * Passed result.
         */
        PASSED,

        /**
         * Failed result.
         */
        FAILED,

        /**
         * Skipped result.
         */
        SKIPPED
    }

    private final Type type;
    private final Throwable throwable;

    /**
     * Constructor
     *
     * @param type type
     */
    private TestDescriptorStatus(final Type type) {
        this(type, null);
    }

    /**
     * Constructor
     *
     * @param type type
     * @param throwable throwable
     */
    private TestDescriptorStatus(final Type type, final Throwable throwable) {
        this.type = type;
        this.throwable = throwable;
    }

    /**
     * Returns the invocation result type.
     *
     * @return the invocation result type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the invocation Throwable. May be null in some scenarios.
     *
     * @return the invocation Throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns true if the result is success.
     *
     * @return true if the result is success, otherwise false
     */
    public boolean isSuccess() {
        return type == Type.PASSED;
    }

    /**
     * Returns true if the result is failure.
     *
     * @return true if the result is failure, otherwise false
     */
    public boolean isFailure() {
        return type == Type.FAILED;
    }

    /**
     * Returns true if the result is skipped.
     *
     * @return true if the result is skipped, otherwise false
     */
    public boolean isSkipped() {
        return type == Type.SKIPPED;
    }

    /**
     * Returns true if the result is skipped with a Throwable.
     *
     * @return true if the result is skipped with a Throwable, otherwise false
     */
    public boolean isSkippedWithThrowable() {
        return type == Type.SKIPPED && throwable != null;
    }

    @Override
    public String toString() {
        return "InvocationResult{" + "type=" + type + ", throwable=" + throwable + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TestDescriptorStatus that = (TestDescriptorStatus) o;
        return type == that.type && Objects.equals(throwable, that.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, throwable);
    }

    /**
     * Creates a successful invocation result.
     *
     * @return an InvocationResult
     */
    public static TestDescriptorStatus passed() {
        return PASSED;
    }

    /**
     * Creates a skipped invocation result.
     *
     * @return an InvocationResult
     */
    public static TestDescriptorStatus skipped() {
        return SKIPPED;
    }

    /**
     * Creates a skipped invocation result.
     *
     * @param throwable throwable
     * @return an InvocationResult
     */
    public static TestDescriptorStatus skipped(final Throwable throwable) {
        return new TestDescriptorStatus(Type.SKIPPED, throwable);
    }

    /**
     * Creates an exception invocation result.
     *
     * @param throwable throwable
     * @return an InvocationResult
     */
    public static TestDescriptorStatus failed(final Throwable throwable) {
        return new TestDescriptorStatus(Type.FAILED, throwable);
    }
}
