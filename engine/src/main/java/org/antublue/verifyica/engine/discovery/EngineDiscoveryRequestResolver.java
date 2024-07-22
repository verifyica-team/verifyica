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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.antublue.verifyica.api.Argument;
import org.antublue.verifyica.api.Verifyica;
import org.antublue.verifyica.engine.configuration.Constants;
import org.antublue.verifyica.engine.context.DefaultEngineContext;
import org.antublue.verifyica.engine.context.DefaultEngineDiscoveryInterceptorContext;
import org.antublue.verifyica.engine.descriptor.ArgumentTestDescriptor;
import org.antublue.verifyica.engine.descriptor.ClassTestDescriptor;
import org.antublue.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.antublue.verifyica.engine.exception.EngineException;
import org.antublue.verifyica.engine.exception.UncheckedClassNotFoundException;
import org.antublue.verifyica.engine.interceptor.EngineInterceptorManager;
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

    private static final DefaultEngineContext DEFAULT_ENGINE_CONTEXT =
            DefaultEngineContext.getInstance();

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
        Map<Class<?>, Set<Integer>> classArgumentIndexMap = new TreeMap<>(getClassComparator());

        try {
            resolveClasspathRootSelectors(engineDiscoveryRequest, classMethodMap);
            resolvePackageSelectors(engineDiscoveryRequest, classMethodMap);
            resolveClassSelectors(engineDiscoveryRequest, classMethodMap);
            resolveMethodSelectors(engineDiscoveryRequest, classMethodMap);
            resolveUniqueIdSelectors(engineDiscoveryRequest, classMethodMap, classArgumentIndexMap);

            filterClassesByName(classMethodMap);
            filterClassesByTag(classMethodMap);
            filterMethodsByName(classMethodMap);
            filterMethodsByTag(classMethodMap);

            List<Class<?>> classes = new ArrayList<>(classMethodMap.keySet());
            classes.addAll(classArgumentIndexMap.keySet());

            DefaultEngineDiscoveryInterceptorContext defaultEngineDiscoveryInterceptorContext =
                    new DefaultEngineDiscoveryInterceptorContext(
                            DefaultEngineContext.getInstance(), classes);

            EngineInterceptorManager.getInstance()
                    .discovery(defaultEngineDiscoveryInterceptorContext);

            classMethodMap.entrySet().removeIf(entry -> !classes.contains(entry.getKey()));
            classArgumentIndexMap.entrySet().removeIf(entry -> !classes.contains(entry.getKey()));

            for (Class<?> testClass : classMethodMap.keySet()) {
                Method argumentSupplierMethod = getArgumentSupplierMethod(testClass);
                Verifyica.ArgumentSupplier annotation =
                        argumentSupplierMethod.getAnnotation(Verifyica.ArgumentSupplier.class);
                int parallelism = Math.max(annotation.parallelism(), 1);

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
                                parallelism);

                engineDescriptor.addChild(classTestDescriptor);

                List<Argument<?>> arguments = getArguments(testClass);

                int argumentIndex = 0;
                for (Argument<?> argument : arguments) {
                    Set<Integer> argumentIndexSet = classArgumentIndexMap.get(testClass);
                    if (argumentIndexSet != null && !argumentIndexSet.contains(argumentIndex)) {
                        argumentIndex++;
                        continue;
                    }

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

        engineDiscoveryRequest
                .getSelectorsByType(ClasspathRootSelector.class)
                .forEach(
                        classpathRootSelector -> {
                            List<Class<?>> testClasses =
                                    ClassPathSupport.findClasses(
                                            classpathRootSelector.getClasspathRoot(),
                                            Predicates.TEST_CLASS);

                            List<? extends ClassNameFilter> classNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(ClassNameFilter.class);

                            Predicate<String> classNamePredicate =
                                    composeFilters(classNameFilters).toPredicate();

                            List<? extends PackageNameFilter> packageNameFilters =
                                    engineDiscoveryRequest.getFiltersByType(
                                            PackageNameFilter.class);

                            Predicate<String> packageNamePredicate =
                                    composeFilters(packageNameFilters).toPredicate();

                            testClasses.forEach(
                                    testClass -> {
                                        if (classNamePredicate.test(testClass.getName())
                                                && packageNamePredicate.test(
                                                        testClass.getPackage().getName())) {
                                            classMethodMap
                                                    .computeIfAbsent(
                                                            testClass,
                                                            method ->
                                                                    new TreeSet<>(
                                                                            getMethodComparator()))
                                                    .addAll(
                                                            MethodSupport.findMethods(
                                                                    testClass,
                                                                    Predicates.TEST_METHOD,
                                                                    HierarchyTraversalMode
                                                                            .BOTTOM_UP));
                                        }
                                    });
                        });
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

        engineDiscoveryRequest
                .getSelectorsByType(PackageSelector.class)
                .forEach(
                        packageSelector -> {
                            String packageName = packageSelector.getPackageName();

                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("packageName [%s]", packageName);
                            }

                            List<Class<?>> testClasses =
                                    ClassPathSupport.findClasses(
                                            packageName, Predicates.TEST_CLASS);

                            testClasses.forEach(
                                    testClass ->
                                            classMethodMap
                                                    .computeIfAbsent(
                                                            testClass,
                                                            method ->
                                                                    new TreeSet<>(
                                                                            getMethodComparator()))
                                                    .addAll(
                                                            MethodSupport.findMethods(
                                                                    testClass,
                                                                    Predicates.TEST_METHOD,
                                                                    HierarchyTraversalMode
                                                                            .BOTTOM_UP)));
                        });
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

        engineDiscoveryRequest
                .getSelectorsByType(ClassSelector.class)
                .forEach(
                        classSelector -> {
                            Class<?> testClass = classSelector.getJavaClass();

                            if (Predicates.TEST_CLASS.test(testClass)) {
                                classMethodMap
                                        .computeIfAbsent(
                                                testClass,
                                                method -> new TreeSet<>(getMethodComparator()))
                                        .addAll(
                                                MethodSupport.findMethods(
                                                        testClass,
                                                        Predicates.TEST_METHOD,
                                                        HierarchyTraversalMode.BOTTOM_UP));
                            }
                        });
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

        engineDiscoveryRequest
                .getSelectorsByType(MethodSelector.class)
                .forEach(
                        methodSelector -> {
                            Class<?> testClass = methodSelector.getJavaClass();
                            Method testMethod = methodSelector.getJavaMethod();

                            if (Predicates.TEST_CLASS.test(testClass)
                                    && Predicates.TEST_METHOD.test(testMethod)) {
                                classMethodMap
                                        .computeIfAbsent(
                                                testClass,
                                                method -> new TreeSet<>(getMethodComparator()))
                                        .add(testMethod);
                            }
                        });
    }

    /**
     * Method to resolve UniqueIdSelectors
     *
     * @param engineDiscoveryRequest engineDiscoveryRequest
     * @param classMethodMap classMethodMap
     * @param argumentIndexMap argumentIndexMap
     */
    private static void resolveUniqueIdSelectors(
            EngineDiscoveryRequest engineDiscoveryRequest,
            Map<Class<?>, Set<Method>> classMethodMap,
            Map<Class<?>, Set<Integer>> argumentIndexMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("resolveUniqueIdSelectors()");
        }

        engineDiscoveryRequest
                .getSelectorsByType(UniqueIdSelector.class)
                .forEach(
                        uniqueIdSelector -> {
                            UniqueId uniqueId = uniqueIdSelector.getUniqueId();
                            List<UniqueId.Segment> segments = uniqueId.getSegments();

                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("uniqueId [%s]", uniqueId);
                            }

                            // Specific argument selected
                            if (segments.size() == 3) {
                                UniqueId.Segment classSegment = segments.get(1);
                                UniqueId.Segment argumentSegment = segments.get(2);

                                Class<?> testClass = null;

                                try {
                                    testClass =
                                            Thread.currentThread()
                                                    .getContextClassLoader()
                                                    .loadClass(classSegment.getValue());
                                } catch (ClassNotFoundException e) {
                                    UncheckedClassNotFoundException.propagate(e);
                                }

                                classMethodMap
                                        .computeIfAbsent(
                                                testClass,
                                                method -> new TreeSet<>(getMethodComparator()))
                                        .addAll(
                                                MethodSupport.findMethods(
                                                        testClass,
                                                        Predicates.TEST_METHOD,
                                                        HierarchyTraversalMode.BOTTOM_UP));

                                argumentIndexMap
                                        .computeIfAbsent(testClass, clazz -> new LinkedHashSet<>())
                                        .add(Integer.parseInt(argumentSegment.getValue()));
                            } else {
                                segments.forEach(
                                        segment -> {
                                            String segmentType = segment.getType();

                                            if (segmentType.equals(
                                                    ClassTestDescriptor.class.getName())) {
                                                String javaClassName = segment.getValue();

                                                Class<?> testClass = null;

                                                try {
                                                    testClass =
                                                            Thread.currentThread()
                                                                    .getContextClassLoader()
                                                                    .loadClass(javaClassName);
                                                } catch (ClassNotFoundException e) {
                                                    UncheckedClassNotFoundException.propagate(e);
                                                }

                                                classMethodMap
                                                        .computeIfAbsent(
                                                                testClass,
                                                                method ->
                                                                        new TreeSet<>(
                                                                                getMethodComparator()))
                                                        .addAll(
                                                                MethodSupport.findMethods(
                                                                        testClass,
                                                                        Predicates.TEST_METHOD,
                                                                        HierarchyTraversalMode
                                                                                .BOTTOM_UP));
                                            }
                                        });
                            }
                        });
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

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_CLASS_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("%s [%s]", Constants.TEST_CLASS_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
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
                        });

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_CLASS_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("%s [%s]", Constants.TEST_CLASS_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
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
                        });
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

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_CLASS_TAG_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]", Constants.TEST_CLASS_TAG_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
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
                                            LOGGER.trace(
                                                    "removing testClass [%s]", clazz.getName());
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        });

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_CLASS_TAG_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        " %s [%s]", Constants.TEST_CLASS_TAG_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            Iterator<Class<?>> iterator = classMethodMap.keySet().iterator();
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

    /**
     * Method to filter methods by method name
     *
     * @param classMethodMap classMethodMap
     */
    private static void filterMethodsByName(Map<Class<?>, Set<Method>> classMethodMap) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("filterMethodsByName()");
        }

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_METHOD_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        " %s [%s]", Constants.TEST_METHOD_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            for (Map.Entry<Class<?>, Set<Method>> classSetEntry :
                                    classMethodMap.entrySet()) {
                                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                                while (iterator.hasNext()) {
                                    Method testMethod = iterator.next();
                                    matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                                    if (!matcher.find()) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(
                                                    "removing testClass [%s] testMethod [%s]",
                                                    testMethod.getClass().getName(),
                                                    testMethod.getName());
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        });

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_METHOD_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        " %s [%s]", Constants.TEST_METHOD_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            for (Map.Entry<Class<?>, Set<Method>> classSetEntry :
                                    classMethodMap.entrySet()) {
                                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                                while (iterator.hasNext()) {
                                    Method testMethod = iterator.next();
                                    matcher.reset(DisplayNameSupport.getDisplayName(testMethod));
                                    if (matcher.find()) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(
                                                    "removing testClass [%s] testMethod [%s]",
                                                    testMethod.getClass().getName(),
                                                    testMethod.getName());
                                        }
                                        iterator.remove();
                                    }
                                }
                            }
                        });
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

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_METHOD_TAG_INCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]", Constants.TEST_METHOD_TAG_INCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            for (Map.Entry<Class<?>, Set<Method>> classSetEntry :
                                    classMethodMap.entrySet()) {
                                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                                while (iterator.hasNext()) {
                                    Method testMethod = iterator.next();
                                    String tag = TagSupport.getTag(testMethod);
                                    if (tag == null) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(
                                                    "removing testClass [%s] testMethod [%s]",
                                                    testMethod.getClass().getName(),
                                                    testMethod.getName());
                                        }
                                        iterator.remove();
                                    } else {
                                        matcher.reset(tag);
                                        if (!matcher.find()) {
                                            if (LOGGER.isTraceEnabled()) {
                                                LOGGER.trace(
                                                        "removing testClass [%s] testMethod [%s]",
                                                        testMethod.getClass().getName(),
                                                        testMethod.getName());
                                            }
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        });

        Optional.ofNullable(
                        DEFAULT_ENGINE_CONTEXT
                                .getConfiguration()
                                .get(Constants.TEST_METHOD_TAG_EXCLUDE_REGEX))
                .ifPresent(
                        value -> {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(
                                        "%s [%s]", Constants.TEST_METHOD_TAG_EXCLUDE_REGEX, value);
                            }

                            Pattern pattern = Pattern.compile(value);
                            Matcher matcher = pattern.matcher("");

                            for (Map.Entry<Class<?>, Set<Method>> classSetEntry :
                                    classMethodMap.entrySet()) {
                                Iterator<Method> iterator = classSetEntry.getValue().iterator();
                                while (iterator.hasNext()) {
                                    Method testMethod = iterator.next();
                                    String tag = TagSupport.getTag(testMethod);
                                    if (tag != null) {
                                        matcher.reset(tag);
                                        if (matcher.find()) {
                                            if (LOGGER.isTraceEnabled()) {
                                                LOGGER.trace(
                                                        "removing testClass [%s] testMethod"
                                                                + " [%s]",
                                                        testMethod.getClass().getName(),
                                                        testMethod.getName());
                                            }
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        });
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
        testDescriptor
                .getChildren()
                .forEach((Consumer<TestDescriptor>) EngineDiscoveryRequestResolver::prune);

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
}
