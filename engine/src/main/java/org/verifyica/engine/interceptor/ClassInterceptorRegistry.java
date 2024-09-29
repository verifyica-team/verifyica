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

package org.verifyica.engine.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.verifyica.api.interceptor.ClassInterceptor;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.ObjectSupport;
import org.verifyica.engine.support.OrderSupport;

public class ClassInterceptorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInterceptorRegistry.class);

    private final ReentrantReadWriteLock reentrantReadWriteLock;
    private final List<ClassInterceptor> classInterceptors;

    public ClassInterceptorRegistry() {
        reentrantReadWriteLock = new ReentrantReadWriteLock(true);
        classInterceptors = new ArrayList<>();
    }

    public void initialize() {
        reentrantReadWriteLock.writeLock().lock();
        try {
            LOGGER.trace("initialize()");
            LOGGER.trace("loading autowired class interceptors");

            List<Class<?>> autowiredClassInterceptors = new ArrayList<>(
                    ClassSupport.findAllClasses(InterceptorPredicates.AUTOWIRED_CLASS_INTERCEPTOR_CLASS));

            filter(autowiredClassInterceptors);

            OrderSupport.orderClasses(autowiredClassInterceptors);

            LOGGER.trace("autowired class interceptor count [%d]", autowiredClassInterceptors.size());

            for (Class<?> classInterceptorClass : autowiredClassInterceptors) {
                try {
                    LOGGER.trace("loading autowired class interceptor [%s]", classInterceptorClass.getName());

                    Object object = ObjectSupport.createObject(classInterceptorClass);

                    classInterceptors.add((ClassInterceptor) object);

                    LOGGER.trace("autowired class interceptor [%s] loaded", classInterceptorClass.getName());
                } catch (EngineException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new EngineException(t);
                }
            }
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public List<ClassInterceptor> getClassInterceptors(Class<?> testClass) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            return new ArrayList<>(classInterceptors);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to filter ClassInterceptors
     *
     * @param classes classes
     */
    private static void filter(List<Class<?>> classes) {
        Set<Class<?>> filteredClasses = new LinkedHashSet<>(classes);

        ConcreteConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX)
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_EXCLUDE_REGEX, regex);

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

        ConcreteConfiguration.getInstance()
                .getOptional(Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX)
                .ifPresent(regex -> {
                    LOGGER.trace("%s [%s]", Constants.ENGINE_AUTOWIRED_CLASS_INTERCEPTORS_INCLUDE_REGEX, regex);

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
