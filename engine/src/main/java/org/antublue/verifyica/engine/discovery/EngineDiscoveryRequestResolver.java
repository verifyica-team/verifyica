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

package org.antublue.verifyica.engine.discovery;

import static org.junit.platform.engine.Filter.composeFilters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.configuration.DefaultConfiguration;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.logger.Logger;
import org.antublue.verifyica.engine.logger.LoggerFactory;
import org.antublue.verifyica.engine.support.ClassPathSupport;
import org.antublue.verifyica.engine.support.DisplayNameSupport;
import org.antublue.verifyica.engine.support.MethodSupport;
import org.antublue.verifyica.engine.support.OrderSupport;
import org.antublue.verifyica.engine.support.TagSupport;
import org.antublue.verifyica.engine.util.StopWatch;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Class to implement EngineDiscoveryRequestResolver */
public class EngineDiscoveryRequestResolver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EngineDiscoveryRequestResolver.class);

    private static final DefaultConfiguration CONFIGURATION = DefaultConfiguration.getInstance();

    private static Comparator<Object> getClassComparator() {
        return Comparator.comparing(clazz -> OrderSupport.getOrder((Class<?>) clazz))
                .thenComparing(clazz -> DisplayNameSupport.getDisplayName((Class<?>) clazz));
    }

    private static Comparator<Object> getMethodComparator() {
        return Comparator.comparing(method -> OrderSupport.getOrder((Method) method))
                .thenComparing(method -> DisplayNameSupport.getDisplayName((Method) method));
    }

    /** Constructor */
    public EngineDiscoveryRequestResolver() {
        // DO NOTHING
    }

    /**
     * Method to resolve the engine discovery request, building an engine descriptor
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param engineDescriptor engineDescriptor
     */
    public void resolveSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveSelectors()");
        }

        StopWatch stopWatch = new StopWatch();
        Map<Class<?>, Set<Method>> classMethodMap = new TreeMap<>(getClassComparator());

        try {
            resolveClasspathRootSelectors(engineDiscoveryRequest, classMethodMap);
            resolvePackageSelectors(engineDiscoveryRequest, classMethodMap);
            resolveClassSelectors(engineDiscoveryRequest, classMethodMap);
            resolveMethodSelectors(engineDiscoveryRequest, classMethodMap);
            resolveUniqueIdSelectors(engineDiscoveryRequest, classMethodMap);

            filterClassesByName(classMethodMap);
            filterClassesByTag(classMethodMap);
            filterMethodsByName(classMethodMap);
            filterMethodsByTag(classMethodMap);

            for (Class<?> testClass : classMethodMap.keySet()) {
                Method argumentSupplierMethod = getArgumentSupplierMethod(testClass);
                Verifyica.ArgumentSupplier annotation =
                        argumentSupplierMethod.getAnnotation(Verifyica.ArgumentSupplier.class);
                int permits = annotation.parallelism();
                if (permits < 1) {
                    permits = 1;
                }

                UniqueId classTestDescriptorUniqueId =
                        engineDescriptor.getUniqueId().append("class", testClass.getName());

                ClassTestDescriptor classTestDescriptor =
                        new ClassTestDescriptor(
                                classTestDescriptorUniqueId,
                                DisplayNameSupport.getDisplayName(testClass),
                                testClass,
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.PREPARE_METHOD,
                                        HierarchyTraversalMode.TOP_DOWN),
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.CONCLUDE_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP),
                                permits);

                engineDescriptor.addChild(classTestDescriptor);

                List<Argument<?>> arguments = getArguments(testClass);

                int argumentIndex = 0;
                for (Argument<?> argument : arguments) {
                    UniqueId argumentTestDescriptorUniqueId =
                            classTestDescriptorUniqueId.append(
                                    "argument", String.valueOf(argumentIndex));

                    ArgumentTestDescriptor argumentTestDescriptor =
                            new ArgumentTestDescriptor(
                                    argumentTestDescriptorUniqueId,
                                    argument.getName(),
                                    testClass,
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.BEFORE_ALL_METHOD,
                                            HierarchyTraversalMode.TOP_DOWN),
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.AFTER_ALL_METHOD,
                                            HierarchyTraversalMode.BOTTOM_UP),
                                    argument);

                    classTestDescriptor.addChild(argumentTestDescriptor);

                    for (Method method : classMethodMap.get(testClass)) {
                        UniqueId testMethodDescriptorUniqueId =
                                argumentTestDescriptorUniqueId.append("test", method.getName());

                        TestMethodTestDescriptor testMethodTestDescriptor =
                                new TestMethodTestDescriptor(
                                        testMethodDescriptorUniqueId,
                                        DisplayNameSupport.getDisplayName(method),
                                        testClass,
                                        MethodSupport.findMethods(
                                                testClass,
                                                Predicates.BEFORE_EACH_METHOD,
                                                HierarchyTraversalMode.TOP_DOWN),
                                        method,
                                        MethodSupport.findMethods(
                                                testClass,
                                                Predicates.AFTER_EACH_METHOD,
                                                HierarchyTraversalMode.BOTTOM_UP));

                        argumentTestDescriptor.addChild(testMethodTestDescriptor);
                    }

                    argumentIndex++;
                }
            }

            prune(engineDescriptor);
            shuffle(engineDescriptor);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        } finally {
            stopWatch.stop();
            LOGGER.trace("resolveSelectors() %d ms", stopWatch.elapsedTime().toMillis());
        }
    }

    /**
     * Method to resolve ClassPathSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveClasspathRootSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveClasspathRootSelectors()");
        }

        List<ClasspathRootSelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);

        for (ClasspathRootSelector classpathRootSelector : discoverySelectors) {
            List<Class<?>> testClasses =
                    ClassPathSupport.findClasses(
                            classpathRootSelector.getClasspathRoot(), Predicates.TEST_CLASS);

            List<? extends ClassNameFilter> classNameFilters =
                    engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

            Predicate<String> classNamePredicate = composeFilters(classNameFilters).toPredicate();

            List<? extends PackageNameFilter> packageNameFilters =
                    engineDiscoveryRequest.getFiltersByType(PackageNameFilter.class);

            Predicate<String> packageNamePredicate =
                    composeFilters(packageNameFilters).toPredicate();

            for (Class<?> testClass : testClasses) {
                if (classNamePredicate.test(testClass.getName())
                        && packageNamePredicate.test(testClass.getPackage().getName())) {
                    classMethodMap
                            .computeIfAbsent(
                                    testClass, method -> new TreeSet<>(getMethodComparator()))
                            .addAll(
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.TEST_METHOD,
                                            HierarchyTraversalMode.BOTTOM_UP));
                }
            }
        }
    }

    /**
     * Method to resolve PackageSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolvePackageSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolvePackageSelectors()");
        }

        List<PackageSelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);

        for (PackageSelector packageSelector : discoverySelectors) {
            String packageName = packageSelector.getPackageName();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("packageName [%s]", packageName);
            }

            List<Class<?>> testClasses =
                    ClassPathSupport.findClasses(packageName, Predicates.TEST_CLASS);

            for (Class<?> testClass : testClasses) {
                classMethodMap
                        .computeIfAbsent(testClass, method -> new TreeSet<>(getMethodComparator()))
                        .addAll(
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.TEST_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP));
            }
        }
    }

    /**
     * Method to resolve ClassSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveClassSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveClassSelectors()");
        }

        List<ClassSelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);

        for (ClassSelector classSelector : discoverySelectors) {
            Class<?> testClass = classSelector.getJavaClass();

            if (Predicates.TEST_CLASS.test(testClass)) {
                classMethodMap
                        .computeIfAbsent(testClass, method -> new TreeSet<>(getMethodComparator()))
                        .addAll(
                                MethodSupport.findMethods(
                                        testClass,
                                        Predicates.TEST_METHOD,
                                        HierarchyTraversalMode.BOTTOM_UP));
            }
        }
    }

    /**
     * Method to resolve MethodSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveMethodSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveMethodSelectors()");
        }

        List<MethodSelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);

        for (MethodSelector methodSelector : discoverySelectors) {
            Class<?> testClass = methodSelector.getJavaClass();
            Method testMethod = methodSelector.getJavaMethod();

            if (Predicates.TEST_CLASS.test(testClass) && Predicates.TEST_METHOD.test(testMethod)) {
                classMethodMap
                        .computeIfAbsent(testClass, method -> new TreeSet<>(getMethodComparator()))
                        .add(testMethod);
            }
        }
    }

    /**
     * Method to resolve UniqueIdSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     */
    private static void resolveUniqueIdSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap)
            throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveUniqueIdSelectors()");
        }

        List<UniqueIdSelector> discoverySelectors =
                engineDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);

        for (UniqueIdSelector uniqueIdSelector : discoverySelectors) {
            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
            List<UniqueId.Segment> segments = uniqueId.getSegments();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("uniqueId [%s]", uniqueId);
            }

            for (UniqueId.Segment segment : segments) {
                String segmentType = segment.getType();

                if (segmentType.equals(ClassTestDescriptor.class.getName())) {
                    String javaClassName = segment.getValue();

                    Class<?> testClass =
                            Thread.currentThread().getContextClassLoader().loadClass(javaClassName);

                    classMethodMap
                            .computeIfAbsent(
                                    testClass, method -> new TreeSet<>(getMethodComparator()))
                            .addAll(
                                    MethodSupport.findMethods(
                                            testClass,
                                            Predicates.TEST_METHOD,
                                            HierarchyTraversalMode.BOTTOM_UP));
                }
            }
        }
    }

    /**
     * Method to the arguments for a class
     *
     * @param testClass testClass
     * @return a List of arguments
     * @throws Throwable Throwable
     */
    private static List<Argument<?>> getArguments(Class<?> testClass) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArguments() testClass [%s]", testClass.getName());
        }

        List<Argument<?>> testArguments = new ArrayList<>();

        Object object = getArgumentSupplierMethod(testClass).invoke(null, (Object[]) null);
        if (object == null) {
            return testArguments;
        } else if (object instanceof Argument<?>) {
            testArguments.add((Argument<?>) object);
            return testArguments;
        } else if (object instanceof Stream || object instanceof Iterable) {
            Iterator<?> iterator;

            if (object instanceof Stream) {
                Stream<?> stream = (Stream<?>) object;
                iterator = stream.iterator();
            } else {
                Iterable<?> iterable = (Iterable<?>) object;
                iterator = iterable.iterator();
            }

            long index = 0;
            while (iterator.hasNext()) {
                Object o = iterator.next();
                if (o instanceof Argument<?>) {
                    testArguments.add((Argument<?>) o);
                } else {
                    testArguments.add(Argument.of("argument[" + index + "]", o));
                }
                index++;
            }
        } else {
            testArguments.add(Argument.of("argument", object));
        }

        return testArguments;
    }

    /**
     * Method to get a class argument supplier method
     *
     * @param testClass testClass
     * @return the argument supplier method
     */
    private static Method getArgumentSupplierMethod(Class<?> testClass) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("getArgumentSupplierMethod() testClass [%s]", testClass.getName());
        }

        List<Method> methods =
                MethodSupport.findMethods(
                        testClass,
                        Predicates.ARGUMENT_SUPPLIER_METHOD,
                        HierarchyTraversalMode.BOTTOM_UP);

        return methods.get(0);
    }

    /**
     * Method to filter classes by class name
     *
     * @param classMethodMap classMethodMap
     */
    private void filterClassesByName(Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterClassesByName()");
        }

        Optional<String> optional =
                Optional.ofNullable(CONFIGURATION.getProperty(Constants.TEST_CLASS_INCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_CLASS_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = classMethodMap.keySet().iterator();
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
        }

        optional =
                Optional.ofNullable(CONFIGURATION.getProperty(Constants.TEST_CLASS_EXCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_CLASS_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = classMethodMap.keySet().iterator();
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
        }
    }

    /**
     * Method to filter classes by tags
     *
     * @param classMethodMap classMethodMap
     */
    private void filterClassesByTag(Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterClassesByTag()");
        }

        Optional<String> optional =
                Optional.ofNullable(
                        CONFIGURATION.getProperty(Constants.TEST_CLASS_TAG_INCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_CLASS_TAG_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = classMethodMap.keySet().iterator();
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
                            LOGGER.trace("removing testClass [%s]", clazz.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }

        optional =
                Optional.ofNullable(
                        CONFIGURATION.getProperty(Constants.TEST_CLASS_TAG_EXCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_CLASS_TAG_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            Iterator<Class<?>> iterator = classMethodMap.keySet().iterator();
            while (iterator.hasNext()) {
                Class<?> clazz = iterator.next();
                String tag = TagSupport.getTag(clazz);
                if (tag != null) {
                    matcher.reset(tag);
                    if (matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("removing testClass [%s]", clazz.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Method to filter methods by method name
     *
     * @param classMethodMap classMethodMap
     */
    private static void filterMethodsByName(Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterMethodsByName()");
        }

        Optional<String> optional =
                Optional.ofNullable(CONFIGURATION.getProperty(Constants.TEST_METHOD_INCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_METHOD_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            for (Map.Entry<Class<?>, Set<Method>> classSetEntry : classMethodMap.entrySet()) {
                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                while (iterator.hasNext()) {
                    Method testMethod = iterator.next();
                    matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                    if (!matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "removing testClass [%s] testMethod [%s]",
                                    testMethod.getClass().getName(), testMethod.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }

        optional =
                Optional.ofNullable(CONFIGURATION.getProperty(Constants.TEST_METHOD_EXCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(" %s [%s]", Constants.TEST_METHOD_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            for (Map.Entry<Class<?>, Set<Method>> classSetEntry : classMethodMap.entrySet()) {
                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                while (iterator.hasNext()) {
                    Method testMethod = iterator.next();
                    matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                    if (matcher.find()) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "removing testClass [%s] testMethod [%s]",
                                    testMethod.getClass().getName(), testMethod.getName());
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Method to filter methods by tags
     *
     * @param classMethodMap classMethodMap
     */
    private static void filterMethodsByTag(Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterMethodsByTag()");
        }

        Optional<String> optional =
                Optional.ofNullable(
                        CONFIGURATION.getProperty(Constants.TEST_METHOD_TAG_INCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_METHOD_TAG_INCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            for (Map.Entry<Class<?>, Set<Method>> classSetEntry : classMethodMap.entrySet()) {
                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                while (iterator.hasNext()) {
                    Method testMethod = iterator.next();
                    String tag = TagSupport.getTag(testMethod);
                    if (tag == null) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace(
                                    "removing testClass [%s] testMethod [%s]",
                                    testMethod.getClass().getName(), testMethod.getName());
                        }
                        iterator.remove();
                    } else {
                        matcher.reset(tag);
                        if (!matcher.find()) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "removing testClass [%s] testMethod [%s]",
                                        testMethod.getClass().getName(), testMethod.getName());
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        }

        optional =
                Optional.ofNullable(
                        CONFIGURATION.getProperty(Constants.TEST_METHOD_TAG_EXCLUDE_REGEX));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("%s [%s]", Constants.TEST_METHOD_TAG_EXCLUDE_REGEX, optional.get());
            }

            Pattern pattern = Pattern.compile(optional.get());
            Matcher matcher = pattern.matcher("");

            for (Map.Entry<Class<?>, Set<Method>> classSetEntry : classMethodMap.entrySet()) {
                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                while (iterator.hasNext()) {
                    Method testMethod = iterator.next();
                    String tag = TagSupport.getTag(testMethod);
                    if (tag != null) {
                        matcher.reset(tag);
                        if (matcher.find()) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "removing testClass [%s] testMethod [%s]",
                                        testMethod.getClass().getName(), testMethod.getName());
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to prune a test descriptor depth first
     *
     * @param testDescriptor testDescriptor
     */
    private static void prune(TestDescriptor testDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("prune()");
        }

        recursivePrune(testDescriptor);
    }

    /**
     * Method to prune a test descriptor depth first
     *
     * @param testDescriptor testDescriptor
     */
    private static void recursivePrune(TestDescriptor testDescriptor) {
        Set<? extends TestDescriptor> children = new LinkedHashSet<>(testDescriptor.getChildren());
        for (TestDescriptor child : children) {
            prune(child);
        }

        if (testDescriptor.isRoot()) {
            return;
        }

        if (testDescriptor.isContainer() && testDescriptor.getChildren().isEmpty()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("pruned testDescriptor [%s]", testDescriptor);
            }
            testDescriptor.removeFromHierarchy();
        }
    }

    /**
     * Method to shuffle or sort an engine descriptor's children
     *
     * @param engineDescriptor engineDescriptor
     */
    private static void shuffle(EngineDescriptor engineDescriptor) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("shuffle()");
        }

        Optional<String> optional =
                Optional.ofNullable(CONFIGURATION.getProperty(Constants.TEST_CLASS_SHUFFLE));
        if (optional.isPresent()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("shuffling enabled");
            }

            /*
            engineDescriptor.getChildren() returns an
            unmodifiable list, so we have to create a copy
            of the list, remove all children from the engineDescriptor,
            shuffle our copy of the list, then add or list
            back to the engineDescriptor
            */

            List<TestDescriptor> testDescriptors = new ArrayList<>(engineDescriptor.getChildren());
            testDescriptors.forEach(engineDescriptor::removeChild);

            Collections.shuffle(testDescriptors);

            testDescriptors.forEach(engineDescriptor::addChild);
        }
    }
}
