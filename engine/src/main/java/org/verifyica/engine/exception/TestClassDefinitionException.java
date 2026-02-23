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

/**
 * TestClassDefinitionException is an exception for test class definition errors.
 */
public class TestClassDefinitionException extends EngineException {

    /**
     * Constructs a new TestClassDefinitionException with the specified message.
     *
     * @param message the detail message
     */
    public TestClassDefinitionException(String message) {
        super(message);
    }

    /**
     * Constructs a new TestClassDefinitionException with the specified message and cause.
     *
     * @param message the detail message
     * @param throwable the cause of this exception
     */
    public TestClassDefinitionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new TestClassDefinitionException with the specified cause.
     *
     * @param throwable the cause of this exception
     */
    public TestClassDefinitionException(Throwable throwable) {
        super(throwable);
    }
}
