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

package org.antublue.verifyica.api.interceptor;

import org.antublue.verifyica.api.EngineExtension;

/** Class to implement EngineInterceptorAdapter */
public class EngineInterceptorAdapter implements EngineInterceptor {

    private final EngineExtension engineExtension;

    /**
     * Constructor
     *
     * @param engineExtension engineExtension
     */
    public EngineInterceptorAdapter(EngineExtension engineExtension) {
        notNull(engineExtension, "engineExtension is null");

        this.engineExtension = engineExtension;
    }

    /**
     * Method to intercept engine initialization
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @return the InterceptorResult
     * @throws Throwable Throwable
     */
    public InterceptorResult interceptInitialize(EngineInterceptorContext engineInterceptorContext)
            throws Throwable {
        engineExtension.initialize(engineInterceptorContext.getEngineContext());
        return InterceptorResult.PROCEED;
    }

    /**
     * Method to intercept engine destruction
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @return the InterceptorResult
     * @throws Throwable Throwable
     */
    public void interceptDestroy(EngineInterceptorContext engineInterceptorContext)
            throws Throwable {
        engineExtension.destroy(engineInterceptorContext.getEngineContext());
    }

    /**
     * Checks if an Object is not null, throwing an IllegalArgumentException is the Object is null.
     *
     * @param object object
     * @param message message
     */
    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
