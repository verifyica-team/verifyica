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

package org.antublue.verifyica.engine.extension.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.engine.EngineExtension;
import org.antublue.verifyica.api.engine.EngineExtensionContext;
import org.antublue.verifyica.api.engine.ExtensionResult;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.extension.InternalEngineExtension;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.TagSupport;

/** Class to implement FilterTestClassesEngineExtension */
@InternalEngineExtension
@Verifyica.Order(order = 0)
public class FilterTestClassesEngineExtension implements EngineExtension {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FilterTestClassesEngineExtension.class);

    public ExtensionResult onTestClassDiscovery(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("onTestClassDiscovery()");
        }

        filterTestClassesByName(engineExtensionContext, testClasses);
        filterTestClassesByTag(engineExtensionContext, testClasses);

        if (LOGGER.isTraceEnabled()) {
            // Print all test classes that were discovered
            testClasses.forEach(testClass -> LOGGER.trace("test class [%s]", testClass.getName()));
        }

        return ExtensionResult.PROCEED;
    }

    /**
     * Method to filter test classes by class name
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClasses testClasses
     */
    private void filterTestClassesByName(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestClassesByName()");
        }

        Optional.ofNullable(
                        engineExtensionContext
                                .getEngineContext()
                                .getConfiguration()
                                .get(Constants.ENGINE_TEST_CLASS_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_TEST_CLASS_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = testClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (!matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });

        Optional.ofNullable(
                        engineExtensionContext
                                .getEngineContext()
                                .getConfiguration()
                                .get(Constants.ENGINE_TEST_CLASS_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_TEST_CLASS_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = testClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                matcher.reset(clazz.getName());
                                if (matcher.find()) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                                    }
                                    iterator.remove();
                                }
                            }
                        });
    }

    /**
     * Method to filter test classes by tag
     *
     * @param engineExtensionContext engineExtensionContext
     * @param testClasses testClasses
     */
    private void filterTestClassesByTag(
            EngineExtensionContext engineExtensionContext, List<Class<?>> testClasses) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterTestClassesByTag()");
        }

        Optional.ofNullable(
                        engineExtensionContext
                                .getEngineContext()
                                .getConfiguration()
                                .get(Constants.ENGINE_TEST_CLASS_TAG_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]",
                                        Constants.ENGINE_TEST_CLASS_TAG_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = testClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                String tag = TagSupport.getTag(clazz);
                                if (tag == null) {
                                    if (LOGGER.isTraceEnabled()) {
                                        LOGGER.trace("removing testClass [%s]", clazz.getName());
                                    }
                                    iterator.remove();
                                } else {
                                    matcher.reset(tag);
                                    if (!matcher.find()) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(
                                                    "removing testClass [%s]", clazz.getName());
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        });

        Optional.ofNullable(
                        engineExtensionContext
                                .getEngineContext()
                                .getConfiguration()
                                .get(Constants.ENGINE_TEST_CLASS_TAG_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        " %s [%s]",
                                        Constants.ENGINE_TEST_CLASS_TAG_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = testClasses.iterator();
                            while (iterator.hasNext()) {
                                Class<?> clazz = iterator.next();
                                String tag = TagSupport.getTag(clazz);
                                if (tag != null) {
                                    matcher.reset(tag);
                                    if (matcher.find()) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(
                                                    "removing testClass [%s]", clazz.getName());
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        });
    }
}
