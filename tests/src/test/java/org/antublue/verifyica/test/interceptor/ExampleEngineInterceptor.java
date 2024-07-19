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

package org.antublue.verifyica.test.interceptor;

import static java.lang.String.format;

import java.util.UUID;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.interceptor.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.EngineInterceptorContext;

/** Class to implement ExampleEngineInterceptor */
@Verifyica.Order(order = 0)
public class ExampleEngineInterceptor implements EngineInterceptor {

    public static final String KEY = ExampleEngineInterceptor.class.getName() + ".key";
    public static final String VALUE = UUID.randomUUID().toString();

    @Override
    public void intercept(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        System.out.println(
                format(
                        "%s intercept(%s)",
                        getClass().getName(), engineInterceptorContext.getLifeCycle()));

        switch (engineInterceptorContext.getLifeCycle()) {
            case INITIALIZE:
                {
                    engineInterceptorContext.getEngineContext().getStore().put(KEY, VALUE);
                    break;
                }
            case DESTROY:
                {
                    engineInterceptorContext.getEngineContext().getStore().remove(KEY);
                    break;
                }
            default:
                {
                    // DO NOTHING
                }
        }
    }
}
