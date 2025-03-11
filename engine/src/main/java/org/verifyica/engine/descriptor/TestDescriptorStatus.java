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

package org.verifyica.engine.descriptor;

import java.util.Objects;

/** Class to implement TestDescriptorStatus */
public class TestDescriptorStatus {

    private static final TestDescriptorStatus PASSED = new TestDescriptorStatus(Type.PASSED);
    private static final TestDescriptorStatus SKIPPED = new TestDescriptorStatus(Type.SKIPPED);

    /** InvocationResult Type */
    public enum Type {
        /** Passed */
        PASSED,
        /** Failed */
        FAILED,
        /** Skipped */
        SKIPPED
    }

    private final Type type;
    private final Throwable throwable;

    /**
     * Constructor
     *
     * @param type type
     */
    private TestDescriptorStatus(Type type) {
        this(type, null);
    }

    /**
     * Constructor
     *
     * @param type type
     * @param throwable throwable
     */
    private TestDescriptorStatus(Type type, Throwable throwable) {
        this.type = type;
        this.throwable = throwable;
    }

    /**
     * Method to get the invocation result type
     *
     * @return the invocation result type
     */
    public Type getType() {
        return type;
    }

    /**
     * Method to get the invocation Throwable. May be null in some scenarios.
     *
     * @return the invocation Throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Method to return if the result is success
     *
     * @return true if the result is success, else false
     */
    public boolean isSuccess() {
        return type == Type.PASSED;
    }

    /**
     * Method to return if the result is failure
     *
     * @return true if the result is failure, else false
     */
    public boolean isFailure() {
        return type == Type.FAILED;
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
     * Method to return if the result is skipped with a Throwable
     *
     * @return true if the result is skipped with a Throwable, else false
     */
    public boolean isSkippedWithThrowable() {
        return type == Type.SKIPPED && throwable != null;
    }

    @Override
    public String toString() {
        return "InvocationResult{" + "type=" + type + ", throwable=" + throwable + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestDescriptorStatus that = (TestDescriptorStatus) o;
        return type == that.type && Objects.equals(throwable, that.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, throwable);
    }

    /**
     * Method to create successful invocation result
     *
     * @return an InvocationResult
     */
    public static TestDescriptorStatus passed() {
        return PASSED;
    }

    /**
     * Method to create a skipped invocation result
     *
     * @return an InvocationResult
     */
    public static TestDescriptorStatus skipped() {
        return SKIPPED;
    }

    /**
     * Method to create a skipped invocation result
     *
     * @param throwable throwable
     * @return an InvocationResult
     */
    public static TestDescriptorStatus skipped(Throwable throwable) {
        return new TestDescriptorStatus(Type.SKIPPED, throwable);
    }

    /**
     * Method to create a exception invocation result
     *
     * @param throwable throwable
     * @return an InvocationResult
     */
    public static TestDescriptorStatus failed(Throwable throwable) {
        return new TestDescriptorStatus(Type.FAILED, throwable);
    }
}
