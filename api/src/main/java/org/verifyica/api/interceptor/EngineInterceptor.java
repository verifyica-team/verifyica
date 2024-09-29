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

package org.verifyica.api.interceptor;

/** Interface to implement EngineInterceptor */
public interface EngineInterceptor {

    /**
     * Engine onInitialize callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void onInitialize(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }

    /**
     * Engine onDestroy callback
     *
     * @param engineInterceptorContext engineInterceptorContext
     * @throws Throwable Throwable
     */
    default void onDestroy(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        // INTENTIONALLY BLANK
    }
}
