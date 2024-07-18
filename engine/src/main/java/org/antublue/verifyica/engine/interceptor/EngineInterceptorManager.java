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

package org.antublue.verifyica.engine.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.Configuration;
import org.antublue.verifyica.api.EngineExtension;
import org.antublue.verifyica.api.interceptor.EngineInterceptor;
import org.antublue.verifyica.api.interceptor.EngineInterceptorAdapter;
import org.antublue.verifyica.api.interceptor.EngineInterceptorContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.discovery.Predicates;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.ObjectSupport;
import org.antublue.verifyica.engine.support.OrderSupport;

/** Class to implement EngineInterceptorManager */
public class EngineInterceptorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineInterceptorManager.class);

    private static final Configuration CONFIGURATION = DefaultConfiguration.getInstance();

    private final List<EngineInterceptor> engineInterceptors;
    private boolean initialized;

    /** Constructor */
    private EngineInterceptorManager() {
        engineInterceptors = new ArrayList<>();
    }

    /** Method to load test engine interceptors */
    private synchronized void load() {
        if (!initialized) {
            Set<Class<?>> classSet = new HashSet<>();

            classSet.addAll(ClassPathSupport.findClasses(Predicates.ENGINE_INTERCEPTOR_CLASS));
            classSet.addAll(ClassPathSupport.findClasses(Predicates.ENGINE_EXTENSION_CLASS));

            List<Class<?>> classes = new ArrayList<>(classSet);

            filter(classes);
            OrderSupport.order(classes);

            for (Class<?> clazz : classes) {
                try {
                    Object object = ObjectSupport.createObject(clazz);
                    if (object instanceof EngineInterceptor) {
                        engineInterceptors.add((EngineInterceptor) object);
                    } else {
                        engineInterceptors.add(
                                new EngineInterceptorAdapter((EngineExtension) object));
                    }
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
     * Method to invoke engine interceptors
     *
     * @param engineInterceptorContext engineInvocationContext
     * @throws Throwable Throwable
     */
    public void initialize(EngineInterceptorContext engineInterceptorContext) throws Throwable {
        load();

        for (EngineInterceptor engineInterceptor : engineInterceptors) {
            engineInterceptor.intercept(engineInterceptorContext);
        }
    }

    /**
     * Method to invoke all engine interceptors (capturing any Throwable exceptions)
     *
     * @param engineInterceptorContext engineInvocationContext
     * @return a List of Throwables
     */
    public List<Throwable> destroy(EngineInterceptorContext engineInterceptorContext) {
        load();

        List<Throwable> throwables = new ArrayList<>();

        List<EngineInterceptor> engineInterceptors = new ArrayList<>(this.engineInterceptors);
        Collections.reverse(engineInterceptors);

        for (EngineInterceptor engineInterceptor : engineInterceptors) {
            try {
                engineInterceptor.intercept(engineInterceptorContext);
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
    public static EngineInterceptorManager getInstance() {
        return SingletonHolder.SINGLETON;
    }

    private static void filter(List<Class<?>> classes) {
        Optional.ofNullable(CONFIGURATION.getProperty(Constants.ENGINE_INTERCEPTORS_INCLUDE_REGEX))
                .ifPresent(
                        regex -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_INTERCEPTORS_INCLUDE_REGEX, regex);
                            }

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (!matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace(
                                                "removing engine interceptor/extension [%s]",
                                                clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });

        Optional.ofNullable(CONFIGURATION.getProperty(Constants.ENGINE_INTERCEPTORS_EXCLUDE_REGEX))
                .ifPresent(
                        regex -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_INTERCEPTORS_EXCLUDE_REGEX, regex);
                            }

                            Pattern pattern = Pattern.compile(regex);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classes.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace(
                                                "removing engine interceptor/extension [%s]",
                                                clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });
    }

    /** Class to hold the singleton instance */
    private static class SingletonHolder {

        /** The singleton instance */
        private static final EngineInterceptorManager SINGLETON = new EngineInterceptorManager();
    }
}
