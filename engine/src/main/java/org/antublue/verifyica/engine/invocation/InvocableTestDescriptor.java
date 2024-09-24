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

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/** Class to implement InvocableTestDescriptor */
public abstract class InvocableTestDescriptor extends AbstractTestDescriptor {

    private InvocationResult invocationResult;

    /**
     * Constructor
     *
     * @param uniqueId uniqueId
     * @param displayName displayName
     */
    protected InvocableTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    /**
     * Method to test execution
     *
     * @param invocationContext invocationContext
     */
    public abstract void test(InvocationContext invocationContext);

    /**
     * Method to skip execution
     *
     * @param invocationContext invocationContext
     */
    public abstract void skip(InvocationContext invocationContext);

    /**
     * Method to set the invocation result
     *
     * @param invocationResult invocationResult
     */
    protected void setInvocationResult(InvocationResult invocationResult) {
        this.invocationResult = invocationResult;
    }

    /**
     * Method to get the invocation result
     *
     * @return an InvocationResult
     */
    public InvocationResult getInvocationResult() {
        return invocationResult;
    }
}
