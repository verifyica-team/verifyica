/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.engine.interceptor;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.api.EngineInterceptor;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.StackTracePrinter;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.ObjectSupport;
import org.verifyica.engine.support.OrderSupport;

/** Class to implement EngineInterceptorRegistry */
public class EngineInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInterceptorRegistry.class);

    private final Configuration configuration;
    private final ReadWriteLock readWriteLock;
    private final List<EngineInterceptor> engineInterceptors;

    /**
     * Constructor
     *
     * @param configuration configuration
     */
    public EngineInterceptorRegistry(Configuration configuration) {
        this.configuration = configuration;
        this.readWriteLock = new ReentrantReadWriteLock(true);
        this.engineInterceptors = new ArrayList<>();
    }

    /**
     * Method to initialize engine interceptors
     *
     * @param engineContext engineContext
     */
    public void initialize(EngineContext engineContext) {
        readWriteLock.writeLock().lock();

        try {
            List<Class<?>> autowiredEngineInterceptors = new ArrayList<>(
                    ClassSupport.findAllClasses(InterceptorPredicates.AUTOWIRED_ENGINE_INTERCEPTOR_CLASS));

            filter(autowiredEngineInterceptors);

            OrderSupport.orderClasses(autowiredEngineInterceptors);

            LOGGER.trace("autowired engine interceptor count [%d]", autowiredEngineInterceptors.size());

            for (Class<?> engineInterceptorClass : autowiredEngineInterceptors) {
                try {
                    LOGGER.trace("loading autowired engine interceptor [%s]", engineInterceptorClass.getName());

                    Object object = ObjectSupport.createObject(engineInterceptorClass);

                    engineInterceptors.add((EngineInterceptor) object);

                    LOGGER.trace("autowired class interceptor [%s] loaded", engineInterceptorClass.getName());
                } catch (EngineException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new EngineException(t);
                }
            }

            try {
                for (EngineInterceptor engineInterceptor : engineInterceptors) {
                    engineInterceptor.initialize(engineContext);
                }
            } catch (Throwable t) {
                throw new EngineException(t);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to get the list of engine interceptors
     *
     * @return a list of engine interceptors
     */
    public List<EngineInterceptor> getEngineInterceptors() {
        readWriteLock.readLock().lock();

        try {
            return new ArrayList<>(engineInterceptors);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Method to destroy engine interceptors
     *
     * @param engineContext engineContext
     */
    public void destroy(EngineContext engineContext) {
        for (EngineInterceptor engineInterceptor : engineInterceptors) {
            try {
                engineInterceptor.destroy(engineContext);
            } catch (Throwable t) {
                StackTracePrinter.printStackTrace(t, AnsiColor.TEXT_RED_BOLD, System.err);
            }
        }
    }

    /**
     * Method to filter ClassInterceptors
     *
     * @param classes classes
     */
    private void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        ofNullable(configuration
                        .getProperties()
                        .getProperty(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX))
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_EXCLUDE_REGEX, regex);

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher("");

                    Iterator<Class<?>> iterator = filteredClasses.iterator();
                    while (iterator.hasNext()) {
                        Class<?> clazz = iterator.next();
                        matcher.reset(clazz.getName());
                        if (matcher.find()) {
                            LOGGER.trace("removing class interceptor [%s]", clazz.getName());
                            iterator.remove();
                        }
                    }
                });

        ofNullable(configuration
                        .getProperties()
                        .getProperty(Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX))
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_ENGINE_INTERCEPTORS_INCLUDE_REGEX, regex);

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher("");

                    for (Class<?> clazz : classes) {
                        matcher.reset(clazz.getName());
                        if (matcher.find()) {
                            LOGGER.trace("adding class interceptor [%s]", clazz.getName());
                            filteredClasses.add(clazz);
                        }
                    }
                });

        classes.clear();
        classes.addAll(filteredClasses);
    }
}
