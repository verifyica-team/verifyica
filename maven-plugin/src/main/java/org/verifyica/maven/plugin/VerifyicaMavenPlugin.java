/*
 * Copyright (C) 2023 The Verifyica project authors
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
import org.verifyica.engine.common.Streams;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.ConcreteConfigurationParameters;
import org.verifyica.engine.configuration.Constants;
import org.verifyica.engine.descriptor.InvocableTestDescriptor;
import org.verifyica.engine.listener.ChainedEngineExecutionListener;
import org.verifyica.engine.listener.StatusEngineExecutionListener;
import org.verifyica.engine.listener.SummaryEngineExecutionListener;
import org.verifyica.engine.listener.TracingEngineExecutionListener;
import org.verifyica.engine.support.HumanReadableTimeSupport;

/** Class to implement VerifyicaMavenPlugin */
@SuppressWarnings({"unused", "deprecation"})
@org.apache.maven.plugins.annotations.Mojo(
        name = "test",
        threadSafe = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class VerifyicaMavenPlugin extends AbstractMojo {

    /** Constant */
    private static final String MAVEN_PLUGIN_PROPERTIES_RESOURCE = "/maven-plugin.properties";

    /** Constant */
    private static final String MAVEN_PLUGIN_VERSION_KEY = "version";

    /** Constant */
    private static final String UNKNOWN_VERSION = "unknown";

    /** Constant */
    private static final String INTERACTIVE = "interactive";

    /** Constant */
    private static final String BATCH = "batch";

    /** Constant */
    private static final String GROUP_ID = "org.verifyica";

    /** Constant */
    private static final String ARTIFACT_ID = "maven-plugin";

    /** Constant */
    public static final String VERSION = version();

    private static final String BANNER =
            new AnsiColoredString()
                    .append(AnsiColor.TEXT_WHITE_BRIGHT)
                    .append("Verifyica ")
                    .append(VerifyicaTestEngine.staticGetVersion())
                    .append(AnsiColor.NONE)
                    .build();

    private static final String SUMMARY_BANNER =
            BANNER + AnsiColor.TEXT_WHITE_BRIGHT.wrap(" Summary");

    private static final String SEPARATOR =
            AnsiColor.TEXT_WHITE_BRIGHT.wrap(
                    "------------------------------------------------------------------------");

    private static final String INFO =
            new AnsiColoredString()
                    .append(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .append(AnsiColor.TEXT_BLUE_BOLD)
                    .append("INFO")
                    .append(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .append(AnsiColor.NONE)
                    .append(" ")
                    .build();

    private static final String ERROR =
            new AnsiColoredString()
                    .append(AnsiColor.TEXT_WHITE)
                    .append("[")
                    .append(AnsiColor.TEXT_RED_BOLD)
                    .append("ERROR")
                    .append(AnsiColor.TEXT_WHITE)
                    .append("]")
                    .append(AnsiColor.NONE)
                    .append(" ")
                    .build();

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "properties")
    private Map<String, String> properties;

    private Logger logger;

    private VerifyicaTestEngine verifyicaTestEngine;

    static {
        Streams.fix();
        System.setProperty(Constants.MAVEN_PLUGIN, "true");
    }

    /** Constructor */
    public VerifyicaMavenPlugin() {
        super();
    }

    /**
     * Method to get the version
     *
     * @return the version
     */
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    /**
     * Method to execute the plugin
     *
     * @throws MojoFailureException MojoFailureException
     */
    public void execute() throws MojoFailureException {
        initialize();

        Stopwatch stopwatch = new Stopwatch();

        System.out.println(INFO + SEPARATOR);
        System.out.println(INFO + BANNER);
        System.out.println(INFO + SEPARATOR);

        TestDescriptor testDescriptor;

        try {
            testDescriptor = discovery();
        } catch (Throwable t) {
            t.printStackTrace(System.err);

            System.out.println(ERROR + SEPARATOR);
            System.out.println(ERROR + SUMMARY_BANNER);
            System.out.println(ERROR + SEPARATOR);
            System.out.println(
                    new AnsiColoredString()
                            .append(ERROR)
                            .append(AnsiColor.TEXT_RED_BOLD)
                            .append("ERROR DURING TEST DISCOVERY")
                            .append(AnsiColor.NONE));
            System.out.println(ERROR + SEPARATOR);

            Duration elapsedTime = stopwatch.elapsedTime();

            System.out.println(
                    new AnsiColoredString()
                            .append(ERROR)
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Total time  : ")
                            .append(
                                    HumanReadableTimeSupport.toHumanReadable(
                                            elapsedTime.toNanos(),
                                            HumanReadableTimeSupport.Format.SHORT))
                            .append(" (")
                            .append(elapsedTime.toNanos() / 1e+6D)
                            .append(" ms)")
                            .append(AnsiColor.NONE));

            System.out.println(
                    new AnsiColoredString()
                            .append(ERROR)
                            .append(AnsiColor.TEXT_WHITE_BRIGHT)
                            .append("Finished at : ")
                            .append(HumanReadableTimeSupport.now())
                            .append(AnsiColor.NONE));

            System.out.println(ERROR + SEPARATOR);

            throw new MojoFailureException("");
        }

        try {
            execute(testDescriptor);

            for (TestDescriptor ancestorTestDescriptor : testDescriptor.getAncestors()) {
                InvocableTestDescriptor invocableTestDescriptor =
                        (InvocableTestDescriptor) ancestorTestDescriptor;
                if (invocableTestDescriptor.getInvocationResult().isFailure()) {
                    throw new MojoFailureException("");
                }
            }
        } catch (Throwable t) {
            throw new MojoFailureException(t);
        }
    }

    private void initialize() {
        logger = Logger.create(getLog());
        verifyicaTestEngine = new VerifyicaTestEngine();
    }

    private TestDescriptor discovery() throws Throwable {
        Configuration configuration = ConcreteConfiguration.getInstance();

        configuration.put(Constants.MAVEN_PLUGIN, Constants.TRUE);
        logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN, Constants.TRUE);

        configuration.put(Constants.MAVEN_PLUGIN_VERSION, VERSION);
        logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_VERSION, VERSION);

        if (mavenSession.getRequest().isInteractiveMode()) {
            configuration.put(Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, INTERACTIVE);
        } else {
            configuration.put(Constants.MAVEN_PLUGIN_MODE, BATCH);
            logger.debug("property [%s] = [%s]", Constants.MAVEN_PLUGIN_MODE, BATCH);
        }

        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                    logger.debug("property [%s] = [%s]", entry.getKey(), entry.getValue());
                }
            }
        }

        Set<Path> artifactPaths = new LinkedHashSet<>();

        artifactPaths.addAll(resolveClasspathElements(mavenProject.getCompileClasspathElements()));
        artifactPaths.addAll(resolveClasspathElements(mavenProject.getCompileClasspathElements()));
        artifactPaths.addAll(resolveClasspathElements(mavenProject.getRuntimeClasspathElements()));
        artifactPaths.addAll(resolveClasspathElements(mavenProject.getTestClasspathElements()));
        artifactPaths.addAll(resolveArtifact(mavenProject.getArtifact()));
        artifactPaths.addAll(resolveArtifacts(mavenProject.getDependencyArtifacts()));
        artifactPaths.addAll(resolveArtifacts(mavenProject.getAttachedArtifacts()));

        Map<String, URL> urls = new LinkedHashMap<>();

        for (Path path : artifactPaths) {
            URL url = path.toUri().toURL();
            urls.putIfAbsent(url.getPath(), url);
        }

        System.setProperty("java.class.path", buildClasspath(urls.values()));

        ClassLoader classLoader =
                new URLClassLoader(
                        urls.values().toArray(new URL[0]),
                        Thread.currentThread().getContextClassLoader());

        Thread.currentThread().setContextClassLoader(classLoader);

        LauncherDiscoveryRequest launcherDiscoveryRequest =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClasspathRoots(artifactPaths))
                        .filters(includeClassNamePatterns(".*"))
                        .configurationParameters(Collections.emptyMap())
                        .build();
        return verifyicaTestEngine.discover(
                launcherDiscoveryRequest, UniqueId.forEngine(verifyicaTestEngine.getId()));
    }

    private void execute(TestDescriptor testDescriptor) {
        ChainedEngineExecutionListener chainedEngineExecutionListener =
                new ChainedEngineExecutionListener(
                        new TracingEngineExecutionListener(),
                        new StatusEngineExecutionListener(),
                        new SummaryEngineExecutionListener());

        ExecutionRequest executionRequest =
                new ExecutionRequest(
                        testDescriptor,
                        chainedEngineExecutionListener,
                        new ConcreteConfigurationParameters(ConcreteConfiguration.getInstance()));

        verifyicaTestEngine.execute(executionRequest);
    }

    /**
     * Method to process a Collection of Strings representing classpath elements
     *
     * @param classpathElements classpathElements
     * @return a Set of Paths
     */
    private Set<Path> resolveClasspathElements(Collection<String> classpathElements) {
        Set<Path> paths = new LinkedHashSet<>();

        if (classpathElements != null) {
            for (String classpathElement : classpathElements) {
                File file = new File(classpathElement);
                if (file.exists()) {
                    Path path = file.toPath();
                    paths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }
        }

        return paths;
    }

    /**
     * Method to process an Artifact representing a classpath element
     *
     * @param artifact artifact
     * @return a Set of Paths
     */
    private Set<Path> resolveArtifact(Artifact artifact) {
        Set<Artifact> artifacts = new LinkedHashSet<>();
        artifacts.add(artifact);
        return resolveArtifacts(artifacts);
    }

    /**
     * Method to process a Collection of Artifacts representing a classpath element
     *
     * @param artifacts artifacts
     * @return a Set of Paths
     */
    private Set<Path> resolveArtifacts(Collection<Artifact> artifacts) {
        Set<Path> paths = new LinkedHashSet<>();

        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                File file = artifact.getFile();
                if (file.exists()) {
                    Path path = file.toPath();
                    paths.add(path);
                    logger.debug("classpathElement [%s]", path);
                }
            }
        }

        return paths;
    }

    /**
     * Method to build a String representing the classpath
     *
     * @param urls urls
     * @return a String representing the classpath
     */
    private static String buildClasspath(Collection<URL> urls) {
        StringJoiner stringJoiner = new StringJoiner(File.pathSeparator);
        for (URL url : urls) {
            stringJoiner.add(url.getPath());
        }
        return stringJoiner.toString();
    }

    /**
     * Method to return the version
     *
     * @return the version
     */
    private static String version() {
        String value = UNKNOWN_VERSION;

        try (InputStream inputStream =
                VerifyicaMavenPlugin.class.getResourceAsStream(MAVEN_PLUGIN_PROPERTIES_RESOURCE)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                value = properties.getProperty(MAVEN_PLUGIN_VERSION_KEY).trim();
            }
        } catch (IOException e) {
            // INTENTIONALLY BLANK
        }

        return value;
    }

    /** Class to implement Logger */
    private static class Logger {

        private final Log log;

        /**
         * Constructor
         *
         * @param log log
         */
        private Logger(Log log) {
            this.log = log;
        }

        /**
         * Method to log a DEBUG message
         *
         * @param format format
         * @param objects object
         */
        public void debug(String format, Object... objects) {
            if (log.isDebugEnabled()) {
                log.debug(format(format, objects));
            }
        }

        /**
         * Method to create a Logger from a Maven Log
         *
         * @param log log
         * @return a Logger
         */
        public static Logger create(Log log) {
            return new Logger(log);
        }
    }
}
