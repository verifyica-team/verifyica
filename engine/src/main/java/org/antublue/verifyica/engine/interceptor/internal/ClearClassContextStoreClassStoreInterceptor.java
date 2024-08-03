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

package org.antublue.verifyica.engine.interceptor.internal.engine;

import org.antublue.verifyica.api.Store;
import org.antublue.verifyica.api.interceptor.ClassInterceptor;
import org.antublue.verifyica.api.interceptor.ClassInterceptorContext;
import org.antublue.verifyica.api.interceptor.engine.EngineInterceptor;
import org.antublue.verifyica.engine.interceptor.internal.InternalClassInterceptor;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;

/** Class to implement ClearClassContextStoreEngineInterceptor */
public class ClearClassContextStoreEngineInterceptor implements ClassInterceptor {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClearClassContextStoreEngineInterceptor.class);

    /** Constructor */
    public ClearClassContextStoreEngineInterceptor() {
        // INTENTIONALLY BLANK
    }

    @Override
    public void onDestroy(ClassInterceptorContext classInterceptorContext) {
        LOGGER.trace("onDestroy()");

        Store store = classInterceptorContext.getClassContext().getStore();

        for (Object key : store.keySet()) {
            Object value = store.getOptional(key);
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }
            }
        }

        store.clear();
    }
}
