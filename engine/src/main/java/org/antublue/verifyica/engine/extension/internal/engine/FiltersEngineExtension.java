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

package org.antublue.verifyica.engine.extension.internal.engine;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.api.extension.engine.EngineExtensionContext;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.exception.EngineConfigurationException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.TagSupport;

@Verifyica.Order(order = 0)
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class FiltersEngineExtension implements InternalEngineExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(FiltersEngineExtension.class);

    private final List<ClassMethodTuplePredicate> includeClassMethodTuplePredicates;
    private final List<ClassMethodTuplePredicate> excludeClassMethodTuplePredicates;

    private final List<ClassMethodTagTuplePredicate> includeClassMethodTagTuplePredicates;
    private final List<ClassMethodTagTuplePredicate> excludeClassMethodTagTuplePredicates;

    /** Constructor */
    public FiltersEngineExtension() {
        includeClassMethodTuplePredicates = new ArrayList<>();
        excludeClassMethodTuplePredicates = new ArrayList<>();
        includeClassMethodTagTuplePredicates = new ArrayList<>();
        excludeClassMethodTagTuplePredicates = new ArrayList<>();

        loadFilterDefinitions();
    }

    @Override
    public Map<Class<?>, Set<Method>> onTestDiscovery(
            EngineExtensionContext engineExtensionContext,
            Map<Class<?>, Set<Method>> testClassMethodMap)
            throws Throwable {
        LOGGER.trace("onTestDiscovery()");

        Iterator<Class<?>> testClassesIterator = testClassMethodMap.keySet().iterator();
        while (testClassesIterator.hasNext()) {
            Class<?> testClass = testClassesIterator.next();
            Iterator<Method> testMethodsIterator = testClassMethodMap.get(testClass).iterator();
            while (testMethodsIterator.hasNext()) {
                Method testMethod = testMethodsIterator.next();

                if (!testClassMethodTupleIncludePredicates(testClass, testMethod)) {
                    testMethodsIterator.remove();
                    continue;
                }

                if (testClassMethodTupleExcludePredicates(testClass, testMethod)) {
                    testMethodsIterator.remove();
                    continue;
                }

                /*
                if (!testClassMethodTagTupleIncludePredicates(testClass, testMethod)) {
                    testMethodsIterator.remove();
                    continue;
                }

                if (!testClassMethodTagTupleExcludePredicates(testClass, testMethod)) {
                    testMethodsIterator.remove();
                }
                */
            }

            if (testClassMethodMap.containsKey(testClass)
                    && testClassMethodMap.get(testClass).isEmpty()) {
                testClassesIterator.remove();
            }
        }

        if (LOGGER.isTraceEnabled()) {
            // Print all test classes that were discovered
            testClassMethodMap.forEach(
                    (key, value) -> {
                        LOGGER.trace("test class [%s]", key.getName());
                        value.forEach(
                                testMethod ->
                                        LOGGER.trace("  test method [%s]", testMethod.getName()));
                    });
        }

        return testClassMethodMap;
    }

    /** Method to load filter definitions */
    private void loadFilterDefinitions() {
        LOGGER.trace("loading filter definitions");

        String filtersFilename =
                DefaultEngineContext.getInstance()
                        .getConfiguration()
                        .get(Constants.ENGINE_FILTERS_FILENAME);

        if (filtersFilename != null && !filtersFilename.trim().isEmpty()) {
            try (BufferedReader bufferedReader =
                    new BufferedReader(new FileReader(filtersFilename))) {
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (!line.startsWith("#")) {
                        String[] tokens = splitQuotedString(line);
                        if (tokens.length == 3) {
                            if ("".equals(tokens[1])) {
                                tokens[1] = ".*";
                            }
                            if ("".equals(tokens[2])) {
                                tokens[2] = ".*";
                            }

                            LOGGER.trace(
                                    "filter entry [%s] [%s] [%s]", tokens[0], tokens[1], tokens[2]);

                            if ("includeClass".equals(tokens[0])) {
                                includeClassMethodTuplePredicates.add(
                                        new ClassMethodTuplePredicate(tokens[1], tokens[2]));
                            } else if ("excludeClass".equals(tokens[0])) {
                                excludeClassMethodTuplePredicates.add(
                                        new ClassMethodTuplePredicate(tokens[1], tokens[2]));
                            } else if ("includeTag".equals(tokens[0])) {
                                includeClassMethodTagTuplePredicates.add(
                                        new ClassMethodTagTuplePredicate(tokens[1], tokens[2]));
                            } else if ("excludeTag".equals(tokens[0])) {
                                excludeClassMethodTagTuplePredicates.add(
                                        new ClassMethodTagTuplePredicate(tokens[1], tokens[2]));
                            } else {
                                throw new EngineConfigurationException(
                                        format("Invalid filter definition [%s]", line));
                            }
                        } else {
                            throw new EngineConfigurationException(
                                    format("Invalid filter definition [%s]", line));
                        }
                    }
                }
            } catch (EngineConfigurationException e) {
                throw e;
            } catch (Throwable t) {
                throw new EngineConfigurationException(
                        format("Exception loading filters. filename [%s]", filtersFilename), t);
            }
        }
    }

    private boolean testClassMethodTupleIncludePredicates(Class<?> clazz, Method method) {
        if (includeClassMethodTuplePredicates.isEmpty()) {
            return true;
        }

        int count = 0;

        for (ClassMethodTuplePredicate predicate : includeClassMethodTuplePredicates) {
            if (predicate.test(new ClassMethodTuple(clazz, method))) {
                count++;
            }
        }

        return count > 0;
    }

    private boolean testClassMethodTupleExcludePredicates(Class<?> clazz, Method method) {
        if (excludeClassMethodTuplePredicates.isEmpty()) {
            return false;
        }

        int count = 0;

        for (ClassMethodTuplePredicate predicate : excludeClassMethodTuplePredicates) {
            if (predicate.test(new ClassMethodTuple(clazz, method))) {
                count++;
            }
        }

        return count > 0;
    }

    private boolean testClassMethodTagTupleIncludePredicates(Class<?> clazz, Method method) {
        if (includeClassMethodTagTuplePredicates.isEmpty()) {
            return false;
        }

        String classTag = TagSupport.getTag(clazz);
        if (classTag == null) {
            return true;
        }

        String methodTag = TagSupport.getTag(method);
        if (methodTag == null) {
            return true;
        }

        int count = 0;

        for (ClassMethodTagTuplePredicate predicate : includeClassMethodTagTuplePredicates) {
            if (predicate.test(new ClassMethodTagTuple(classTag, methodTag))) {
                count++;
            }
        }

        return count > 0;
    }

    private boolean testClassMethodTagTupleExcludePredicates(Class<?> clazz, Method method) {
        if (excludeClassMethodTagTuplePredicates.isEmpty()) {
            return true;
        }

        String classTag = TagSupport.getTag(clazz);
        if (classTag == null) {
            return false;
        }

        String methodTag = TagSupport.getTag(method);
        if (methodTag == null) {
            return true;
        }

        int count = 0;

        for (ClassMethodTagTuplePredicate predicate : excludeClassMethodTagTuplePredicates) {
            if (predicate.test(new ClassMethodTagTuple(classTag, methodTag))) {
                count++;
            }
        }

        return count > 0;
    }

    private static String[] splitQuotedString(String string) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\S+");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }
            result.add(token);
        }

        return result.toArray(new String[0]);
    }

    private static class ClassMethodTuple {

        public final Class<?> clazz;
        public final Method method;

        public ClassMethodTuple(Class<?> clazz, Method method) {
            this.clazz = clazz;
            this.method = method;
        }
    }

    private static class ClassMethodTuplePredicate implements Predicate<ClassMethodTuple> {

        private final Matcher classMatcher;
        private final Matcher methodMatcher;

        public ClassMethodTuplePredicate(String classRegex, String methodRegex) {
            this.classMatcher = Pattern.compile(classRegex).matcher("");
            this.methodMatcher = Pattern.compile(methodRegex).matcher("");
        }

        @Override
        public boolean test(ClassMethodTuple classMethodTuple) {
            classMatcher.reset(classMethodTuple.clazz.getName());
            methodMatcher.reset(classMethodTuple.method.getName());

            return classMatcher.find() && methodMatcher.find();
        }
    }

    private static class ClassMethodTagTuple {

        public final String classTag;
        public final String methodTag;

        public ClassMethodTagTuple(String classTag, String methodTag) {
            this.classTag = classTag;
            this.methodTag = methodTag;
        }
    }

    private static class ClassMethodTagTuplePredicate implements Predicate<ClassMethodTagTuple> {

        private final Matcher classTagMatcher;
        private final Matcher methodTagMatcher;

        public ClassMethodTagTuplePredicate(String classTagRegex, String methodTagRegex) {
            this.classTagMatcher = Pattern.compile(classTagRegex).matcher("");
            this.methodTagMatcher = Pattern.compile(methodTagRegex).matcher("");
        }

        @Override
        public boolean test(ClassMethodTagTuple classMethodTagTuple) {
            classTagMatcher.reset(classMethodTagTuple.classTag);
            methodTagMatcher.reset(classMethodTagTuple.methodTag);

            return classTagMatcher.find() && methodTagMatcher.find();
        }
    }
}
