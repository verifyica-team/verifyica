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

package org.verifyica.engine.descriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.verifyica.engine.invocation.Invocation;
import org.verifyica.engine.invocation.InvocationContext;
import org.verifyica.engine.invocation.InvocationResult;

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
     * Method to get invokable child test descriptor
     *
     * @return a List of invokable child test descriptors
     */
    protected List<InvocableTestDescriptor> getInvocableChildren() {
        List<InvocableTestDescriptor> invocableTestDescriptors = new ArrayList<>();

        getChildren().forEach((Consumer<TestDescriptor>) testDescriptor -> {
            if (testDescriptor instanceof InvocableTestDescriptor) {
                invocableTestDescriptors.add((InvocableTestDescriptor) testDescriptor);
            }
        });

        return invocableTestDescriptors;
    }

    /**
     * Method to test execution
     *
     * @param invocationContext invocationContext
     * @return an Invocation
     */
    public abstract Invocation getTestInvocation(InvocationContext invocationContext);

    /**
     * Method to skip execution
     *
     * @param invocationContext invocationContext
     * @return an Invocation
     */
    public abstract Invocation getSkipInvocation(InvocationContext invocationContext);

    /**
     * Method to set the invocation result
     *
     * @param invocationResult invocationResult
     */
    public void setInvocationResult(InvocationResult invocationResult) {
        this.invocationResult = invocationResult;
    }

    /**
     * Method to get the invocation result
     *
     * @return the InvocationResult
     */
    public InvocationResult getInvocationResult() {
        return invocationResult;
    }
}
