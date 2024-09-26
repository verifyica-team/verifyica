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

package org.verifyica.engine.invocation;

import java.util.function.Consumer;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.verifyica.engine.descriptor.InvocableTestDescriptor;

/** Class to implement SkipInvocation */
public class SkipInvocation implements Invocation {

    private final InvocableTestDescriptor invocableTestDescriptor;
    private final InvocationContext invocationContext;
    private final EngineExecutionListener engineExecutionListener;

    /**
     * Constructor
     *
     * @param testDescriptor testDescriptor
     * @param invocationContext invocationContext
     */
    public SkipInvocation(TestDescriptor testDescriptor, InvocationContext invocationContext) {
        this.invocableTestDescriptor = (InvocableTestDescriptor) testDescriptor;
        this.invocationContext = invocationContext;
        this.engineExecutionListener = invocationContext.get(EngineExecutionListener.class);
    }

    @Override
    public void invoke() {
        engineExecutionListener.executionStarted(invocableTestDescriptor);

        invocableTestDescriptor
                .getChildren()
                .forEach(
                        (Consumer<TestDescriptor>)
                                testDescriptor ->
                                        new SkipInvocation(testDescriptor, invocationContext)
                                                .invoke());

        engineExecutionListener.executionSkipped(invocableTestDescriptor, "Skipped");

        invocableTestDescriptor.setInvocationResult(InvocationResult.skipped());
    }
}
