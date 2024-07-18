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
import org.junit.platform.commons.util.Preconditions;

/** Class to implement EngineInterceptorAdapter */
public class EngineInterceptorAdapter implements EngineInterceptor {

    private final EngineExtension engineExtension;

    /**
     * Constructor
     *
     * @param engineExtension engineExtension
     */
    public EngineInterceptorAdapter(EngineExtension engineExtension) {
        Preconditions.notNull(engineExtension, "engineExtension is null");

        this.engineExtension = engineExtension;
    }

    @Override
    public void intercept(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        switch (engineInterceptorContext.getLifeCycle()) {
            case INITIALIZE:
                {
                    engineExtension.initialize(engineInterceptorContext.getEngineContext());
                    break;
                }
            case DESTROY:
                {
                    engineExtension.destroy(engineInterceptorContext.getEngineContext());
                    break;
                }
        }
    }
}
