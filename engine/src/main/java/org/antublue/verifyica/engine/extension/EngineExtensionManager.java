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

package org.antublue.verifyica.engine.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antublue.verifyica.api.EngineContext;
import org.antublue.verifyica.api.EngineExtension;
import org.antublue.verifyica.engine.context.ConcreteEngineContext;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement EngineExtensionManager */
public class EngineExtensionManager {

    private static final EngineContext ENGINE_CONTEXT = ConcreteEngineContext.getInstance();

    private final List<EngineExtension> engineExtensions;
    private boolean initialized;

    /** Constructor */
    private EngineExtensionManager() {
        engineExtensions = new ArrayList<>();
    }

    /** Method to load test engine extensions */
    private synchronized void load() {
        if (!initialized) {
            List<Class<?>> classes =
                    ClassPathSupport.findClasses(Predicates.TEST_ENGINE_EXTENSION_CLASS);

            OrderSupport.order(classes);

            for (Class<?> clazz : classes) {
                try {
                    engineExtensions.add(ObjectSupport.createObject(clazz));
                } catch (EngineException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new EngineException(t);
                }
            }

            initialized = true;
        }
    }

    /**
     * Method to initialize engine extensions
     *
     * @throws Throwable Throwable
     */
    public void initialize() throws Throwable {
        load();

        for (EngineExtension engineExtension : engineExtensions) {
            engineExtension.initialize(ENGINE_CONTEXT);
        }
    }

    /**
     * Method to conclude engine extensions
     *
     * @return a List of Throwables
     */
    public List<Throwable> destroy() {
        load();

        List<Throwable> throwables = new ArrayList<>();

        List<EngineExtension> engineExtensions = new ArrayList<>(this.engineExtensions);
        Collections.reverse(engineExtensions);

        for (EngineExtension engineExtension : engineExtensions) {
            try {
                engineExtension.destroy(ENGINE_CONTEXT);
            } catch (Throwable t) {
                throwables.add(t);
            }
        }

        return throwables;
    }

    /**
     * Method to get a singleton instance
     *
     * @return the singleton instance
     */
    public static EngineExtensionManager getInstance() {
        return SingletonHolder.SINGLETON;
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final EngineExtensionManager SINGLETON = new EngineExtensionManager();
    }
}
