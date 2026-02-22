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

package org.verifyica.maven.plugin;

import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.verifyica.api.Configuration;
import org.verifyica.engine.VerifyicaTestEngine;
import org.verifyica.engine.common.AnsiColor;
import org.verifyica.engine.common.AnsiColoredString;
import org.verifyica.engine.common.Stopwatch;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.ConcreteConfigurationParameters;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.descriptor.TestableTestDescriptor;
import org.verifyica.engine.exception.TestClassDefinitionException;
import org.verifyica.engine.listener.ChainedEngineExecutionListener;
import org.verifyica.engine.listener.StatusEngineExecutionListener;
import org.verifyica.engine.listener.SummaryEngineExecutionListener;
import org.verifyica.engine.listener.TracingEngineExecutionListener;
import org.verifyica.engine.support.TimestampSupport;

/**
 * Maven plugin entry point for running Verifyica tests in a Maven build.
 */
@SuppressWarnings({"unused", "deprecation"})
@org.apache.maven.plugins.annotations.Mojo(
        name = "test",
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class VerifyicaMavenPlugin extends AbstractMojo {

    /**
     * Resource path for plugin metadata properties.
     */
    private static final String MAVEN_PLUGIN_PROPERTIES_RESOURCE = "/maven-plugin.properties";

    /**
     * Property key for the plugin version.
     */
    private static final String MAVEN_PLUGIN_VERSION_KEY = "version";

    /**
     * Fallback version when the properties resource is unavailable.
     */
    private static final String UNKNOWN_VERSION = "unknown";

    /**
     * Configuration value for interactive execution mode.
     */
    private static final String INTERACTIVE = "interactive";

    /**
     * Configuration value for batch execution mode.
     */
    private static final String BATCH = "batch";

    /**
     * Maven group id for the plugin artifact.
     */
    private static final String GROUP_ID = "org.verifyica";

    /**
     * Maven artifact id for the plugin artifact.
     */
    private static final String ARTIFACT_ID = "maven-plugin";

    /**
     * Plugin version resolved from the embedded properties resource.
     */
    public static final String VERSION = version();

    /**
     * Startup banner displayed before execution.
     */
    private static final String BANNER = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("Verifyica ")
            .append(VerifyicaTestEngine.staticGetVersion())
            .append(" Started @ ")
            .append(TimestampSupport.now())
            .append(AnsiColor.NONE)
            .build();

    /**
     * Summary banner displayed after execution.
     */
    private static final String SUMMARY_BANNER = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE_BRIGHT)
            .append("Verifyica ")
            .append(VerifyicaTestEngine.staticGetVersion())
            .append(" Summary @ ")
            .append(TimestampSupport.now())
            .append(AnsiColor.NONE)
            .build();

    /**
     * Separator line for console output.
     */
    private static final String SEPARATOR = AnsiColor.TEXT_WHITE_BRIGHT.wrap(
            "------------------------------------------------------------------------");

    /**
     * INFO-level console prefix.
     */
    private static final String INFO = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE)
            .append("[")
            .append(AnsiColor.TEXT_BLUE_BOLD)
            .append("INFO")
            .append(AnsiColor.TEXT_WHITE)
            .append("]")
            .append(AnsiColor.NONE)
            .append(" ")
            .build();

    /**
     * ERROR-level console prefix.
     */
    private static final String ERROR = new AnsiColoredString()
            .append(AnsiColor.TEXT_WHITE)
            .append("[")
            .append(AnsiColor.TEXT_RED_BOLD)
            .append("ERROR")
            .append(AnsiColor.TEXT_WHITE)
            .append("]")
            .append(AnsiColor.NONE)
            .append(" ")
            .build();

    /**
     * Maven project injected by the Maven runtime.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    /**
     * Maven session injected by the Maven runtime.
     */
    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    /**
     * Plugin logger wrapper.
     */
    private Logger logger;

    /**
     * Verifyica test engine used for discovery and execution.
     */
    private VerifyicaTestEngine verifyicaTestEngine;

    static {
        System.setProperty(Constants.MAVEN_PLUGIN, "true");
    }

    /**
     * Constructor
     */
    public VerifyicaMavenPlugin() {
        super();
    }

    /**
     * Returns the plugin version.
     *
     * @return the plugin version
     */
    public Optional<String> getVersion() {
        return of(VERSION);
    }

    /**
     * Executes Verifyica discovery and test execution within the Maven build.
     *
     * @throws MojoFailureException when test discovery or execution fails
     * @throws MojoExecutionException when an unexpected error occurs
     */
    public void execute() throws MojoFailureException, MojoExecutionException {
        initialize();

        if (isSkipTestsEnabled()) {
            return;
        }

        final ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            final Stopwatch stopwatch = new Stopwatch();

            System.out.println(INFO + SEPARATOR);
            System.out.println(INFO + BANNER);
            System.out.println(INFO + SEPARATOR);

            final TestDescriptor testDescriptor;

            try {
                testDescriptor = discovery();
            } catch (Throwable t) {
                if (t instanceof TestClassDefinitionException) {
                    System.err.println(ERROR + AnsiColor.TEXT_RED_BOLD.wrap(t.getMessage()));
                } else {
                    t.printStackTrace(System.err);
                }

                System.out.println(ERROR + SEPARATOR);
                System.out.println(ERROR + SUMMARY_BANNER);
                System.out.println(ERROR + SEPARATOR);
                System.out.println(new AnsiColoredString()
                        .append(ERROR)
                        .append(AnsiColor.TEXT_RED_BOLD)
                        .append("STATUS ERROR DURING TEST DISCOVERY")
                        .append(AnsiColor.NONE));
                System.out.println(ERROR + SEPARATOR);

                final Duration elapsedTime = stopwatch.elapsed();

                System.out.println(new AnsiColoredString()
                        .append(ERROR)
                        .append(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append("Total time  : ")
                        .append(TimestampSupport.toHumanReadable(TimestampSupport.Format.SHORT, elapsedTime.toNanos()))
                        .append(" (")
                        .append(elapsedTime.toNanos() / 1e+6D)
                        .append(" ms)")
                        .append(AnsiColor.NONE));

                System.out.println(new AnsiColoredString()
                        .append(ERROR)
                        .append(AnsiColor.TEXT_WHITE_BRIGHT)
                        .append("Finished at : ")
                        .append(TimestampSupport.now())
                        .append(AnsiColor.NONE));

                System.out.println(ERROR + SEPARATOR);

                if (t instanceof TestClassDefinitionException) {
                    throw new MojoFailureException(t.getMessage(), t);
                } else {
                    throw new MojoExecutionException(t.getMessage(), t);
                }
            }

            try {
                execute(testDescriptor);
            } catch (Throwable t) {
                throw new MojoExecutionException(t);
            }

            final Optional<TestableTestDescriptor> optionalExecutableTestDescriptor =
                    testDescriptor.getDescendants().stream()
                            .filter(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_FILTER)
                            .map(TestableTestDescriptor.TESTABLE_TEST_DESCRIPTOR_MAPPER)
                            .filter(testableTestDescriptor -> testableTestDescriptor
                                    .getTestDescriptorStatus()
                                    .isFailure())
                            .findFirst();

            if (optionalExecutableTestDescriptor.isPresent()) {
                throw new MojoFailureException(optionalExecutableTestDescriptor
                        .get()
                        .getTestDescriptorStatus()
                        .getThrowable());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    /**
     * Determines whether tests should be skipped based on Maven properties.
     *
     * @return {@code true} when tests should be skipped
     */
    private boolean isSkipTestsEnabled() {
        return Boolean.parseBoolean(System.getProperty("skipTests"))
                || Boolean.parseBoolean(System.getProperty("maven.test.skip"));
    }

    /**
     * Initializes configuration, logging, and engine state prior to execution.
     */
    private void initialize() {
        logger = Logger.create(getLog());

        final Configuration configuration = ConcreteConfiguration.getInstance();

        System.getProperties().putAll(configuration.getProperties());

        if (mavenProject.getProperties() != null) {
            System.getProperties().putAll(mavenProject.getProperties());
        }

        if (mavenSession.getSystemProperties() != null) {
            System.getProperties().putAll(mavenSession.getSystemProperties());
        }

        if (mavenSession.getUserProperties() != null) {
            System.getProperties().putAll(mavenSession.getUserProperties());
        }

        configuration.getProperties().setProperty(Constants.MAVEN_PLUGIN, Constants.TRUE);
        logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN, Constants.TRUE);

        configuration.getProperties().setProperty(Constants.MAVEN_PLUGIN_VERSION, VERSION);
        logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_VERSION, VERSION);

        if (mavenSession.getRequest().isInteractiveMode()) {
            configuration.getProperties().setProperty(Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
        } else {
            configuration.getProperties().setProperty(Constants.MAVEN_PLUGIN_MODE, BATCH);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, BATCH);
        }

        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                logger.debug("property [%s] = [%s]", entry.getKey(), entry.getValue());
            }
        }

        verifyicaTestEngine = new VerifyicaTestEngine();
    }

    /**
     * Discovers test descriptors using the Verifyica test engine.
     *
     * @return the root test descriptor
     * @throws Throwable if discovery fails
     */
    private TestDescriptor discovery() throws Throwable {
        final Set<Path> artifactPaths = new LinkedHashSet<>();

        artifactPaths.addAll(resolveClasspathElements(mavenProject.getCompileClasspathElements()));
        artifactPaths.addAll(resolveClasspathElements(mavenProject.getRuntimeClasspathElements()));
        artifactPaths.addAll(resolveClasspathElements(mavenProject.getTestClasspathElements()));
        artifactPaths.addAll(resolveArtifact(mavenProject.getArtifact()));
        artifactPaths.addAll(resolveArtifacts(mavenProject.getDependencyArtifacts()));
        artifactPaths.addAll(resolveArtifacts(mavenProject.getAttachedArtifacts()));

        final Map<String, URL> urls = new LinkedHashMap<>();

        for (Path path : artifactPaths) {
            final URL url = path.toUri().toURL();
            urls.putIfAbsent(url.getPath(), url);
        }

        System.setProperty("java.class.path", buildClasspath(urls.values()));

        final ClassLoader classLoader = new URLClassLoader(
                urls.values().toArray(new URL[0]), Thread.currentThread().getContextClassLoader());

        Thread.currentThread().setContextClassLoader(classLoader);

        final LauncherDiscoveryRequest launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                .filters(includeClassNamePatterns(".*"))
                .configurationParameters(Collections.emptyMap())
                .build();
        return verifyicaTestEngine.discover(launcherDiscoveryRequest, UniqueId.forEngine(verifyicaTestEngine.getId()));
    }

    /**
     * Executes the Verifyica test engine with the provided descriptor.
     *
     * @param testDescriptor the root test descriptor to execute
     */
    private void execute(final TestDescriptor testDescriptor) {
        final ChainedEngineExecutionListener chainedEngineExecutionListener = new ChainedEngineExecutionListener(
                new TracingEngineExecutionListener(),
                new StatusEngineExecutionListener(),
                new SummaryEngineExecutionListener());

        final ExecutionRequest executionRequest = new ExecutionRequest(
                testDescriptor,
                chainedEngineExecutionListener,
                new ConcreteConfigurationParameters(ConcreteConfiguration.getInstance()));

        verifyicaTestEngine.execute(executionRequest);
    }

    /**
     * Resolves a collection of classpath elements into existing paths.
     *
     * @param classpathElements classpath element strings
     * @return resolved classpath paths
     */
    private Set<Path> resolveClasspathElements(final Collection<String> classpathElements) {
        final Set<Path> paths = new LinkedHashSet<>();

        if (classpathElements != null) {
            for (String classpathElement : classpathElements) {
                final File file = new File(classpathElement);
                if (file.exists()) {
                    final Path path = file.toPath();
                    paths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }
        }

        return paths;
    }

    /**
     * Resolves a single artifact into an existing path.
     *
     * @param artifact artifact to resolve
     * @return resolved classpath paths
     */
    private Set<Path> resolveArtifact(final Artifact artifact) {
        if (artifact == null) {
            return Collections.emptySet();
        }

        final Set<Artifact> artifacts = new LinkedHashSet<>();
        artifacts.add(artifact);
        return resolveArtifacts(artifacts);
    }

    /**
     * Resolves a collection of artifacts into existing paths.
     *
     * @param artifacts artifacts to resolve
     * @return resolved classpath paths
     */
    private Set<Path> resolveArtifacts(final Collection<Artifact> artifacts) {
        final Set<Path> paths = new LinkedHashSet<>();

        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (artifact == null) {
                    continue;
                }

                final File file = artifact.getFile();
                if (file != null && file.exists()) {
                    final Path path = file.toPath();
                    paths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }
        }

        return paths;
    }

    /**
     * Builds a {@code java.class.path} string from the provided URLs.
     *
     * @param urls classpath URLs
     * @return a platform-specific classpath string
     */
    private static String buildClasspath(final Collection<URL> urls) {
        final StringJoiner stringJoiner = new StringJoiner(File.pathSeparator);
        for (URL url : urls) {
            stringJoiner.add(url.getPath());
        }
        return stringJoiner.toString();
    }

    /**
     * Resolves the plugin version from the embedded properties resource.
     *
     * @return the resolved version or a fallback when unavailable
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream =
                VerifyicaMavenPlugin.class.getResourceAsStream(MAVEN_PLUGIN_PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                final Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(MAVEN_PLUGIN_VERSION_KEY).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY EMPTY
        }

        return value;
    }

    /**
     * Simple Maven logger wrapper for formatted debug output.
     */
    private static class Logger {

        /**
         * Maven log used for output.
         */
        private final Log log;

        /**
         * Constructor
         *
         * @param log Maven log to wrap
         */
        private Logger(final Log log) {
            this.log = log;
        }

        /**
         * Logs a formatted DEBUG message when debug is enabled.
         *
         * @param format format string
         * @param objects format arguments
         */
        public void debug(final String format, final Object... objects) {
            if (log.isDebugEnabled()) {
                log.debug(format(format, objects));
            }
        }

        /**
         * Creates a logger wrapper for the provided Maven log.
         *
         * @param log Maven log to wrap
         * @return a logger wrapper
         */
        public static Logger create(final Log log) {
            return new Logger(log);
        }
    }
}
