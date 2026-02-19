/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

package org.verifyica.engine.resolver;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.IterationSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.verifyica.api.Argument;
import org.verifyica.api.Named;
import org.verifyica.api.Verifyica;
import org.verifyica.engine.api.ClassDefinition;
import org.verifyica.engine.api.MethodDefinition;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.descriptor.TestArgumentTestDescriptor;
import org.verifyica.engine.descriptor.TestClassTestDescriptor;
import org.verifyica.engine.descriptor.TestMethodTestDescriptor;
import org.verifyica.engine.exception.EngineException;
import org.verifyica.engine.exception.TestClassDefinitionException;
import org.verifyica.engine.filter.ClassDefinitionFilter;
import org.verifyica.engine.logger.Logger;
import org.verifyica.engine.logger.LoggerFactory;
import org.verifyica.engine.support.ClassSupport;
import org.verifyica.engine.support.DisplayNameSupport;
import org.verifyica.engine.support.HierarchyTraversalMode;
import org.verifyica.engine.support.OrderSupport;
import org.verifyica.engine.support.TagSupport;

/**
 * Resolves an {@link EngineDiscoveryRequest} into a Verifyica engine descriptor hierarchy.
 *
 * <p>This resolver performs four primary steps:</p>
 *
 * <ol>
 *   <li>Collect discovered test classes and test methods from the JUnit Platform selectors.</li>
 *   <li>Resolve Verifyica test arguments via a required {@link Verifyica.ArgumentSupplier} method on each test class.</li>
 *   <li>Build intermediate {@link ClassDefinition} models (ordering, display names, tags, arguments, parallelism).</li>
 *   <li>Build {@link TestDescriptor} children for class → argument → method, and prune disabled test methods.</li>
 * </ol>
 *
 * Performance notes
 *
 * <p>Lifecycle methods (e.g. {@link Verifyica.BeforeEach}) are discovered via reflection. To avoid repeatedly
 * scanning the class hierarchy for every argument and every method, lifecycle methods are resolved once per
 * test class and cached during descriptor construction.</p>
 *
 * <p>This class is Java 8 compatible.</p>
 */
public class EngineDiscoveryRequestResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineDiscoveryRequestResolver.class);

    /**
     * Selector types supported by this resolver.
     *
     * <p>These are used only for trace logging and do not affect selection semantics; the actual selection work
     * is delegated to the selector resolver classes.</p>
     */
    private static final List<Class<? extends DiscoverySelector>> DISCOVERY_SELECTORS_CLASSES =
            Collections.unmodifiableList(Arrays.asList(
                    FileSelector.class,
                    DirectorySelector.class,
                    IterationSelector.class,
                    ClasspathResourceSelector.class,
                    ModuleSelector.class,
                    UriSelector.class,
                    ClasspathRootSelector.class,
                    PackageSelector.class,
                    ClassSelector.class,
                    MethodSelector.class,
                    UniqueIdSelector.class));

    /**
     * Creates a new resolver instance.
     */
    public EngineDiscoveryRequestResolver() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Resolves selectors from the provided {@link EngineDiscoveryRequest} and populates the supplied root
     * {@link TestDescriptor} with Verifyica children.
     *
     * <p>This method is the entry point used during discovery. It constructs a descriptor tree of:</p>
     *
     * <ul>
     *   <li>{@link TestClassTestDescriptor} per test class</li>
     *   <li>{@link TestArgumentTestDescriptor} per resolved argument for the class</li>
     *   <li>{@link TestMethodTestDescriptor} per test method, under each argument</li>
     * </ul>
     *
     * <p>Any test method annotated with {@link Verifyica.Disabled} is removed from the descriptor tree after
     * construction.</p>
     *
     * @param engineDiscoveryRequest the JUnit Platform discovery request
     * @param testDescriptor the root descriptor to attach children to
     * @throws EngineException if an error occurs during discovery/resolution
     */
    public void resolveSelectors(EngineDiscoveryRequest engineDiscoveryRequest, TestDescriptor testDescriptor) {
        LOGGER.trace("resolveSelectors()");

        Stopwatch stopwatch = new Stopwatch();

        Map<Class<?>, Set<Method>> testClassMethodSet = new HashMap<Class<?>, Set<Method>>(32);
        Map<Class<?>, List<Argument<?>>> testClassArgumentMap = new HashMap<Class<?>, List<Argument<?>>>(32);
        Map<Class<?>, Set<Integer>> testClassArgumentIndexMap = new HashMap<Class<?>, Set<Integer>>(32);

        try {
            traceSelectors(engineDiscoveryRequest);

            // Collect test classes/methods and (optionally) argument indices.
            new ClasspathRootSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodSet);
            new PackageSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodSet);
            new ClassSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodSet);
            new MethodSelectorResolver().resolve(engineDiscoveryRequest, testClassMethodSet);
            new UniqueIdSelectorResolver()
                    .resolve(engineDiscoveryRequest, testClassMethodSet, testClassArgumentIndexMap);

            resolveTestArguments(testClassMethodSet, testClassArgumentMap, testClassArgumentIndexMap);

            List<ClassDefinition> classDefinitions = buildClassDefinitions(testClassMethodSet, testClassArgumentMap);

            pruneClassDefinitions(classDefinitions);
            ClassDefinitionFilter.filter(classDefinitions);

            buildEngineDescriptor(classDefinitions, testDescriptor);
            pruneDisabledTestMethods(testDescriptor);
        } catch (EngineException e) {
            throw e;
        } catch (Throwable t) {
            throw new EngineException(t);
        } finally {
            stopwatch.stop();
            LOGGER.trace(
                    "resolveSelectors() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
        }
    }

    /**
     * Emits trace logging for all selectors present in the request.
     *
     * <p>This method is intentionally allocation-light (does not build intermediate lists beyond what the
     * request API returns). Selector identifiers are logged using their {@code toString()} representation.</p>
     *
     * @param engineDiscoveryRequest the discovery request
     */
    private static void traceSelectors(EngineDiscoveryRequest engineDiscoveryRequest) {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }

        for (Class<? extends DiscoverySelector> selectorClass : DISCOVERY_SELECTORS_CLASSES) {
            List<? extends DiscoverySelector> selectors = engineDiscoveryRequest.getSelectorsByType(selectorClass);
            for (DiscoverySelector selector : selectors) {
                String id = selector.toIdentifier().map(Object::toString).orElse("null");
                LOGGER.trace("discoverySelector [%s]", id);
            }
        }
    }

    /**
     * Builds {@link ClassDefinition} instances from the discovered test classes and methods.
     *
     * <p>This method applies Verifyica ordering rules for classes and methods, resolves display names and tags,
     * and determines argument parallelism from {@link Verifyica.ArgumentSupplier#parallelism()}.</p>
     *
     * @param testClassMethodSet mapping of test class → discovered test methods
     * @param testClassArgumentMap mapping of test class → resolved arguments
     * @return ordered list of {@link ClassDefinition}
     */
    private static List<ClassDefinition> buildClassDefinitions(
            Map<Class<?>, Set<Method>> testClassMethodSet, Map<Class<?>, List<Argument<?>>> testClassArgumentMap) {
        Set<Class<?>> orderedClasses =
                OrderSupport.orderClasses(new LinkedHashSet<Class<?>>(testClassMethodSet.keySet()));

        List<ClassDefinition> classDefinitions = new ArrayList<ClassDefinition>(orderedClasses.size());

        for (Class<?> testClass : orderedClasses) {
            List<Argument<?>> testArguments = testClassArgumentMap.get(testClass);
            Set<Method> testMethods = OrderSupport.orderMethods(testClassMethodSet.get(testClass));
            int testArgumentParallelism = getTestArgumentParallelism(testClass);

            String testClassDisplayName = DisplayNameSupport.getDisplayName(testClass);
            Set<String> testClassTags = TagSupport.getTags(testClass);

            List<MethodDefinition> testMethodDefinitions = new ArrayList<MethodDefinition>(testMethods.size());
            for (Method testMethod : testMethods) {
                String methodDisplayName = DisplayNameSupport.getDisplayName(testMethod);
                testMethodDefinitions.add(new ConcreteMethodDefinition(testMethod, methodDisplayName));
            }

            classDefinitions.add(new ConcreteClassDefinition(
                    testClass,
                    testClassDisplayName,
                    testClassTags,
                    testMethodDefinitions,
                    testArguments,
                    testArgumentParallelism));
        }

        return classDefinitions;
    }

    /**
     * Resolves argument lists for each discovered test class.
     *
     * <p>If the discovery request selected specific argument indices (via {@link UniqueIdSelectorResolver}),
     * this method filters the full argument list down to only those indices.</p>
     *
     * @param testClassMethodSet mapping of test class → discovered test methods
     * @param testClassArgumentMap output map to populate with resolved (and possibly filtered) arguments
     * @param argumentIndexMap optional mapping of test class → selected argument indices
     * @throws Throwable if the argument supplier invocation fails or returns an unsupported value
     */
    private static void resolveTestArguments(
            Map<Class<?>, Set<Method>> testClassMethodSet,
            Map<Class<?>, List<Argument<?>>> testClassArgumentMap,
            Map<Class<?>, Set<Integer>> argumentIndexMap)
            throws Throwable {
        LOGGER.trace("resolveTestArguments()");

        Stopwatch stopwatch = new Stopwatch();

        for (Class<?> testClass : testClassMethodSet.keySet()) {
            List<Argument<?>> allArguments = getTestArguments(testClass);
            Set<Integer> indices = argumentIndexMap.get(testClass);

            if (indices == null || indices.isEmpty()) {
                testClassArgumentMap.put(testClass, allArguments);
                continue;
            }

            List<Argument<?>> specific = new ArrayList<Argument<?>>(Math.min(allArguments.size(), indices.size()));
            for (int i = 0; i < allArguments.size(); i++) {
                if (indices.contains(i)) {
                    specific.add(allArguments.get(i));
                }
            }
            testClassArgumentMap.put(testClass, specific);
        }

        LOGGER.trace(
                "resolveTestArguments() elapsedTime [%d] ms",
                stopwatch.elapsed().toMillis());
    }

    /**
     * Invokes the {@link Verifyica.ArgumentSupplier} method for the given test class and converts the
     * returned value into a list of {@link Argument} instances.
     *
     * <p>The supplier return type is flexible and may be one of:</p>
     *
     * <ul>
     *   <li>{@link Argument} (single argument)</li>
     *   <li>{@link Named} (single value named via {@link Named#getName()})</li>
     *   <li>{@code Object[]} (each element becomes an argument)</li>
     *   <li>{@link Stream}, {@link Iterable}, {@link Iterator}, or {@link Enumeration} (each element becomes an argument)</li>
     *   <li>Any other object (treated as a single unnamed argument)</li>
     * </ul>
     *
     * <p>Elements that are not already {@link Argument} are wrapped with {@link Argument#of(String, Object)}.
     * Names are derived in the following order:</p>
     *
     * <ol>
     *   <li>{@link Named#getName()} if the element implements {@link Named}</li>
     *   <li>{@link Enum#name()} if the element is an enum constant</li>
     *   <li>{@code argument[i]} fallback using the element index</li>
     * </ol>
     *
     * @param testClass the test class
     * @return a list of arguments (possibly empty)
     * @throws Throwable if invocation fails or the supplier returns an unsupported array type
     */
    private static List<Argument<?>> getTestArguments(Class<?> testClass) throws Throwable {
        LOGGER.trace("getTestArguments() testClass [%s]", testClass.getName());

        Stopwatch stopwatch = new Stopwatch();
        List<Argument<?>> testArguments = new ArrayList<Argument<?>>();

        Method supplier = getArgumentSupplierMethod(testClass);
        Object supplied = supplier.invoke(null);

        if (supplied == null) {
            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        if (supplied instanceof Argument<?>) {
            testArguments.add((Argument<?>) supplied);
            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        if (supplied instanceof Named) {
            testArguments.add(Argument.of(((Named) supplied).getName(), supplied));
            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        // Arrays (Object[] only; primitive arrays are not supported by design)
        if (supplied.getClass().isArray()) {
            if (!(supplied instanceof Object[])) {
                throw new TestClassDefinitionException(format(
                        "Test class [%s] argument supplier returned a primitive array type [%s] which is not supported",
                        testClass.getName(), supplied.getClass().getName()));
            }

            Object[] objects = (Object[]) supplied;
            for (int i = 0; i < objects.length; i++) {
                testArguments.add(toArgument(objects[i], i));
            }

            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        // Stream / Iterable / Iterator / Enumeration
        if (supplied instanceof Stream) {
            @SuppressWarnings("unchecked")
            Stream<Object> stream = (Stream<Object>) supplied;
            try {
                Iterator<Object> it = stream.iterator();
                long idx = 0;
                while (it.hasNext()) {
                    testArguments.add(toArgument(it.next(), idx++));
                }
            } finally {
                // Stream is AutoCloseable
                stream.close();
            }

            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        Iterator<?> iterator = toIterator(supplied);
        if (iterator != null) {
            long idx = 0;
            while (iterator.hasNext()) {
                testArguments.add(toArgument(iterator.next(), idx++));
            }

            LOGGER.trace(
                    "getTestArguments() elapsedTime [%d] ms",
                    stopwatch.elapsed().toMillis());
            return testArguments;
        }

        // Fallback: single unnamed value
        testArguments.add(Argument.of("argument[0]", supplied));

        LOGGER.trace(
                "getTestArguments() elapsedTime [%d] ms", stopwatch.elapsed().toMillis());
        return testArguments;
    }

    /**
     * Converts supported container-like supplier results into an {@link Iterator}.
     *
     * <p>Supported types:</p>
     * <ul>
     *   <li>{@link Enumeration} (converted via {@link Collections#list(Enumeration)})</li>
     *   <li>{@link Iterator}</li>
     *   <li>{@link Iterable}</li>
     * </ul>
     *
     * @param supplied the supplier result object
     * @return an iterator if the object is supported; otherwise {@code null}
     */
    private static Iterator<?> toIterator(Object supplied) {
        if (supplied instanceof Enumeration) {
            return Collections.list((Enumeration<?>) supplied).iterator();
        }

        if (supplied instanceof Iterator) {
            return (Iterator<?>) supplied;
        }

        if (supplied instanceof Iterable) {
            return ((Iterable<?>) supplied).iterator();
        }

        return null;
    }

    /**
     * Wraps an arbitrary object as an {@link Argument} when needed.
     *
     * <p>If {@code o} is already an {@link Argument}, it is returned unchanged. Otherwise a name is derived
     * and {@link Argument#of(String, Object)} is used.</p>
     *
     * @param object the value to convert
     * @param index the argument index (used for fallback naming)
     * @return an {@link Argument} instance
     */
    private static Argument<?> toArgument(Object object, long index) {
        if (object instanceof Argument<?>) {
            return (Argument<?>) object;
        }

        String name;
        if (object instanceof Named) {
            name = ((Named) object).getName();
        } else if (object != null && object.getClass().isEnum()) {
            name = ((Enum<?>) object).name();
        } else {
            name = "argument[" + index + "]";
        }

        return Argument.of(name, object);
    }

    /**
     * Finds the single {@link Verifyica.ArgumentSupplier} method for the given test class.
     *
     * <p>The search is performed bottom-up in the hierarchy to favor subclass declarations. Only one supplier
     * method may be declared per class in the hierarchy. If none is found, a {@link TestClassDefinitionException}
     * is thrown.</p>
     *
     * @param testClass the test class
     * @return the discovered argument supplier method
     * @throws TestClassDefinitionException if none is found or multiple are declared in the same class
     */
    private static Method getArgumentSupplierMethod(Class<?> testClass) {
        LOGGER.trace("getArgumentSupplierMethod() testClass [%s]", testClass.getName());

        List<Method> methods = ClassSupport.findMethods(
                testClass, ResolverPredicates.ARGUMENT_SUPPLIER_METHOD, HierarchyTraversalMode.BOTTOM_UP);

        if (methods.isEmpty()) {
            throw new TestClassDefinitionException(format(
                    "Test class [%s] is missing a static method annotated with [@Verifyica.%s]",
                    testClass.getName(), Verifyica.ArgumentSupplier.class.getSimpleName()));
        }

        validateSingleMethodPerClass(Verifyica.ArgumentSupplier.class, methods);
        return methods.get(0);
    }

    /**
     * Removes {@link ClassDefinition} entries that cannot produce runnable tests.
     *
     * <p>Currently a class is removed if it has no resolved arguments or no resolved test methods.</p>
     *
     * @param classDefinitions the list of class definitions to prune (mutated in place)
     */
    private static void pruneClassDefinitions(List<ClassDefinition> classDefinitions) {
        LOGGER.trace("pruneClassDefinitions()");

        classDefinitions.removeIf(cd ->
                cd.getArguments().isEmpty() || cd.getTestMethodDefinitions().isEmpty());
    }

    /**
     * Removes {@link TestMethodTestDescriptor} nodes that represent disabled test methods.
     *
     * <p>This is performed after building the tree to keep descriptor construction straightforward.</p>
     *
     * @param testDescriptor the root descriptor
     */
    private static void pruneDisabledTestMethods(TestDescriptor testDescriptor) {
        LOGGER.trace("pruneDisabledTestMethods()");

        // Avoid building a separate stream/collector list; remove in a single pass.
        List<TestDescriptor> descendants = new ArrayList<TestDescriptor>(testDescriptor.getDescendants());
        for (TestDescriptor d : descendants) {
            if (d instanceof TestMethodTestDescriptor) {
                Method m = ((TestMethodTestDescriptor) d).getTestMethod();
                if (m.isAnnotationPresent(Verifyica.Disabled.class)) {
                    d.removeFromHierarchy();
                }
            }
        }
    }

    /**
     * Constructs the Verifyica descriptor hierarchy beneath the provided root descriptor.
     *
     * <p>For each {@link ClassDefinition}:</p>
     * <ul>
     *   <li>Creates a {@link TestClassTestDescriptor} under {@code testDescriptor}</li>
     *   <li>Creates a {@link TestArgumentTestDescriptor} per argument under the class descriptor</li>
     *   <li>Creates a {@link TestMethodTestDescriptor} per test method under each argument descriptor</li>
     * </ul>
     *
     * <p>Lifecycle methods are resolved once per class and reused across all arguments and methods.</p>
     *
     * @param classDefinitions ordered class definitions
     * @param testDescriptor the root descriptor to attach children to
     */
    private static void buildEngineDescriptor(List<ClassDefinition> classDefinitions, TestDescriptor testDescriptor) {
        LOGGER.trace("buildEngineDescriptor()");

        Stopwatch stopwatch = new Stopwatch();

        // Cache lifecycle method resolution per test class; this avoids repeated hierarchy scanning.
        Map<Class<?>, LifecycleMethods> lifecycleCache =
                new HashMap<Class<?>, LifecycleMethods>(Math.max(16, classDefinitions.size() * 2));

        for (ClassDefinition classDefinition : classDefinitions) {
            Class<?> testClass = classDefinition.getTestClass();
            UniqueId classUniqueId = testDescriptor.getUniqueId().append("class", testClass.getName());

            LifecycleMethods lifecycle = lifecycleCache.get(testClass);
            if (lifecycle == null) {
                lifecycle = new LifecycleMethods(testClass);
                lifecycleCache.put(testClass, lifecycle);
            }

            TestClassTestDescriptor testClassDescriptor = new TestClassTestDescriptor(
                    classUniqueId,
                    classDefinition.getDisplayName(),
                    classDefinition.getTags(),
                    testClass,
                    classDefinition.getArgumentParallelism(),
                    lifecycle.prepare,
                    lifecycle.conclude);

            testDescriptor.addChild(testClassDescriptor);

            int argumentIndex = 0;
            for (Argument<?> testArgument : classDefinition.getArguments()) {
                UniqueId argumentUniqueId = classUniqueId.append("argument", String.valueOf(argumentIndex));

                TestArgumentTestDescriptor testArgumentDescriptor = new TestArgumentTestDescriptor(
                        argumentUniqueId,
                        testArgument.getName(),
                        argumentIndex,
                        testArgument,
                        lifecycle.beforeAll,
                        lifecycle.afterAll);

                testClassDescriptor.addChild(testArgumentDescriptor);

                for (MethodDefinition testMethodDefinition : classDefinition.getTestMethodDefinitions()) {
                    Method testMethod = testMethodDefinition.getMethod();
                    UniqueId methodUniqueId = argumentUniqueId.append("method", testMethod.getName());

                    TestMethodTestDescriptor testMethodDescriptor = new TestMethodTestDescriptor(
                            methodUniqueId,
                            testMethodDefinition.getDisplayName(),
                            lifecycle.beforeEach,
                            testMethod,
                            lifecycle.afterEach);

                    testArgumentDescriptor.addChild(testMethodDescriptor);
                }

                argumentIndex++;
            }
        }

        LOGGER.trace(
                "buildEngineDescriptor() elapsedTime [%d] ms",
                stopwatch.elapsed().toMillis());
    }

    /**
     * Finds lifecycle methods using {@link ClassSupport#findMethods(Class, java.util.function.Predicate, HierarchyTraversalMode)}
     * and validates that only one method per declaring class matches the lifecycle annotation.
     *
     * @param testClass the test class
     * @param predicate predicate used to identify matching methods
     * @param mode hierarchy traversal mode
     * @param annotationClass the lifecycle annotation type (for error messages)
     * @return the discovered methods (possibly empty)
     * @throws TestClassDefinitionException if a declaring class contains multiple matching methods
     */
    private static List<Method> findAndValidate(
            Class<?> testClass,
            java.util.function.Predicate<Method> predicate,
            HierarchyTraversalMode mode,
            Class<?> annotationClass) {
        List<Method> methods = ClassSupport.findMethods(testClass, predicate, mode);
        validateSingleMethodPerClass(annotationClass, methods);
        return methods;
    }

    /**
     * Validates that for each declaring class in the returned method list, there is at most one method
     * annotated with the corresponding Verifyica annotation.
     *
     * <p>For example, a class hierarchy may contain multiple {@code @Verifyica.BeforeEach} methods as long as
     * they are declared in different classes in the hierarchy. However, any single class may declare at most
     * one such method.</p>
     *
     * @param annotationClass the Verifyica annotation class (used for error messages)
     * @param methods the discovered methods (may be {@code null} or empty)
     * @throws TestClassDefinitionException if multiple methods are declared in the same class
     */
    private static void validateSingleMethodPerClass(Class<?> annotationClass, List<Method> methods) {
        if (methods == null || methods.isEmpty()) {
            return;
        }

        Set<Class<?>> declaringClasses = new HashSet<Class<?>>((int) (methods.size() / 0.75f) + 1);

        for (Method method : methods) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (!declaringClasses.add(declaringClass)) {
                String annotationDisplayName = "@Verifyica." + annotationClass.getSimpleName();
                throw new TestClassDefinitionException(format(
                        "Class [%s] declares more than one method annotated with [%s]",
                        declaringClass.getName(), annotationDisplayName));
            }
        }
    }

    /**
     * Determines the argument-parallelism for a test class.
     *
     * <p>This value is read from {@link Verifyica.ArgumentSupplier#parallelism()} on the resolved supplier
     * method and is clamped to a minimum of {@code 1}.</p>
     *
     * @param testClass the test class
     * @return the argument parallelism (minimum 1)
     */
    private static int getTestArgumentParallelism(Class<?> testClass) {
        LOGGER.trace("getTestArgumentParallelism() testClass [%s]", testClass.getName());

        Method argumentSupplierMethod = getArgumentSupplierMethod(testClass);
        Verifyica.ArgumentSupplier annotation = argumentSupplierMethod.getAnnotation(Verifyica.ArgumentSupplier.class);

        int parallelism = Math.max(annotation.parallelism(), 1);

        LOGGER.trace("testClass [%s] parallelism [%d]", testClass.getName(), parallelism);
        return parallelism;
    }

    /**
     * Holder for lifecycle method lists for a single test class.
     *
     * <p>Each list may contain methods declared across a class hierarchy. Validation ensures that for any
     * given declaring class, at most one method carries a specific lifecycle annotation.</p>
     */
    private static final class LifecycleMethods {

        final List<Method> prepare;
        final List<Method> conclude;
        final List<Method> beforeAll;
        final List<Method> afterAll;
        final List<Method> beforeEach;
        final List<Method> afterEach;

        /**
         * Resolves and validates lifecycle methods for {@code testClass}.
         *
         * @param testClass the test class
         */
        LifecycleMethods(Class<?> testClass) {
            this.prepare = findAndValidate(
                    testClass,
                    ResolverPredicates.PREPARE_METHOD,
                    HierarchyTraversalMode.TOP_DOWN,
                    Verifyica.Prepare.class);

            this.conclude = findAndValidate(
                    testClass,
                    ResolverPredicates.CONCLUDE_METHOD,
                    HierarchyTraversalMode.BOTTOM_UP,
                    Verifyica.Conclude.class);

            this.beforeAll = findAndValidate(
                    testClass,
                    ResolverPredicates.BEFORE_ALL_METHOD,
                    HierarchyTraversalMode.TOP_DOWN,
                    Verifyica.BeforeAll.class);

            this.afterAll = findAndValidate(
                    testClass,
                    ResolverPredicates.AFTER_ALL_METHOD,
                    HierarchyTraversalMode.BOTTOM_UP,
                    Verifyica.AfterAll.class);

            this.beforeEach = findAndValidate(
                    testClass,
                    ResolverPredicates.BEFORE_EACH_METHOD,
                    HierarchyTraversalMode.TOP_DOWN,
                    Verifyica.BeforeEach.class);

            this.afterEach = findAndValidate(
                    testClass,
                    ResolverPredicates.AFTER_EACH_METHOD,
                    HierarchyTraversalMode.BOTTOM_UP,
                    Verifyica.AfterEach.class);
        }
    }
}
