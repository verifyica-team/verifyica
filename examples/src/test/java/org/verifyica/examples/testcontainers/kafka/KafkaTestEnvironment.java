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

package org.verifyica.examples.testcontainers.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.verifyica.api.Argument;
import org.verifyica.examples.support.Resource;
import org.verifyica.examples.testcontainers.util.ContainerLogConsumer;

/**
 * Class to implement a KafkaTestEnvironment
 */
public class KafkaTestEnvironment implements Argument<KafkaTestEnvironment> {

    /**
     * Total time budget for the initialize() method.
     * Keep this as a single constant so callers can reason about worst-case test time.
     */
    private static final Duration INITIALIZE_TIMEOUT = Duration.ofMinutes(3);

    /**
     * Per-probe timeout passed to each AdminClient call.
     * Must be shorter than INITIALIZE_TIMEOUT and long enough for a single
     * round-trip on a slow CI host.
     */
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(8);

    /**
     * How long to sleep between failed probes. Short enough to react quickly,
     * long enough to avoid busy-spinning while the broker is still booting.
     */
    private static final Duration PROBE_INTERVAL = Duration.ofMillis(500);

    private final String dockerImageName;
    private KafkaContainer kafkaContainer;

    /**
     * Constructor
     *
     * @param dockerImageName the docker image name
     */
    public KafkaTestEnvironment(String dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    @Override
    public String getName() {
        return dockerImageName;
    }

    @Override
    public KafkaTestEnvironment getPayload() {
        return this;
    }

    /**
     * Initializes the KafkaTestEnvironment.
     *
     * <p>Design notes:
     * <ul>
     *   <li>A single {@link #INITIALIZE_TIMEOUT} budget covers container startup AND
     *       the AdminClient readiness probe — there is no double-counting.
     *   <li>{@code withStartupAttempts} is intentionally left at 1. Testcontainers'
     *       built-in retry restarts the container from scratch, which doubles or triples
     *       the worst-case hang time. It is cleaner to let the caller decide whether to
     *       retry at a higher level.
     *   <li>We use {@code Wait.forLogMessage} rather than {@code Wait.forListeningPort}.
     *       The listening port becomes open several seconds before the broker finishes
     *       KRaft leader election and can actually serve metadata requests.
     *   <li>Each AdminClient probe is created fresh and closed immediately. Reusing a
     *       single AdminClient across probes causes its internal {@code NetworkClient}
     *       to enter exponential backoff after the first connection refusal, effectively
     *       making subsequent probes no-ops.
     * </ul>
     *
     * @param network the Docker network to attach the container to
     * @throws Exception if the environment does not become ready within the timeout
     */
    public void initialize(Network network) throws Exception {
        boolean interrupted = false;

        try {
            final long deadlineNanos = System.nanoTime() + INITIALIZE_TIMEOUT.toNanos();

            final DockerImageName image =
                    DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("apache/kafka");

            kafkaContainer = new KafkaContainer(image)
                    .withNetwork(network)
                    // A single attempt. See Javadoc above for rationale.
                    .withStartupAttempts(1)
                    .withLogConsumer(new ContainerLogConsumer(getClass().getName(), dockerImageName))
                    // Wait for the broker-ready log line rather than just an open port.
                    // This pattern matches both "Kafka Server started" (KRaft) and
                    // "KafkaServer id=... started" (ZooKeeper-based images).
                    .waitingFor(Wait.forLogMessage(".*[Kk]afka.*[Ss]erver.*started.*\\n", 1)
                            .withStartupTimeout(INITIALIZE_TIMEOUT));

            /*
             * Workaround for Kafka 3.9.0 listener registration bug.
             * https://github.com/testcontainers/testcontainers-java/issues/9506
             * https://issues.apache.org/jira/browse/KAFKA-18281
             */
            if (dockerImageName.contains("3.9.0")) {
                kafkaContainer.withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094");
            }

            try {
                kafkaContainer.start();
            } catch (Exception e) {
                captureLogsOnFailure(e);
                stopQuietly();
                throw e;
            }

            // Compute remaining budget after container startup completes.
            long remainingNanos = deadlineNanos - System.nanoTime();

            if (remainingNanos <= 0) {
                stopQuietly();
                throw new IllegalStateException("Kafka container started, but the overall initialize timeout of "
                        + INITIALIZE_TIMEOUT + " was already exhausted.");
            }

            try {
                awaitKafkaReady(kafkaContainer.getBootstrapServers(), Duration.ofNanos(remainingNanos));
            } catch (Exception e) {
                captureLogsOnFailure(e);
                stopQuietly();
                throw e;
            }

        } catch (InterruptedException e) {
            interrupted = true;
            stopQuietly();
            throw e;
        } finally {
            // Restore the interrupt flag if it was cleared anywhere in the try block.
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Polls Kafka with a fresh AdminClient on each attempt until the broker can serve
     * both cluster-metadata and topic-list requests, or the timeout expires.
     *
     * <p>A new AdminClient is created per probe iteration. This avoids the internal
     * exponential-backoff state that accumulates in a long-lived client when the broker
     * is not yet reachable. The overhead of creating an AdminClient is negligible
     * compared to a connection timeout.
     *
     * @param bootstrapServers Kafka bootstrap address (e.g. {@code "localhost:12345"})
     * @param timeout          how long to keep trying before giving up
     * @throws InterruptedException  if the calling thread is interrupted
     * @throws IllegalStateException if Kafka is not ready within {@code timeout}
     */
    private void awaitKafkaReady(String bootstrapServers, Duration timeout) throws Exception {
        final long deadlineNanos = System.nanoTime() + timeout.toNanos();

        // toMillis() is available in Java 8. toSeconds() is Java 9+.
        final long probeTimeoutMs = PROBE_TIMEOUT.toMillis();

        // Safe narrowing: ListTopicsOptions.timeoutMs() takes an int.
        // In practice PROBE_TIMEOUT is well under Integer.MAX_VALUE ms (~24 days),
        // but the clamp makes the contract explicit.
        final ListTopicsOptions listTopicsOptions = new ListTopicsOptions()
                .timeoutMs((int) Math.min(probeTimeoutMs, Integer.MAX_VALUE))
                .listInternal(true);

        Exception lastException = null;

        while (System.nanoTime() < deadlineNanos) {
            // Fresh AdminClient per probe — no stale internal backoff state.
            Properties props = buildAdminClientProperties(bootstrapServers, probeTimeoutMs);
            try (AdminClient adminClient = AdminClient.create(props)) {

                // describeCluster: proves the broker can answer metadata requests.
                adminClient.describeCluster().nodes().get(probeTimeoutMs, TimeUnit.MILLISECONDS);

                // listTopics: flushes out "partially ready" broker states.
                adminClient.listTopics(listTopicsOptions).names().get(probeTimeoutMs, TimeUnit.MILLISECONDS);

                return; // Broker is ready.

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            } catch (Exception e) {
                lastException = e;
            }

            // Back off before next probe — but honour the deadline.
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }

            long sleepMs = Math.min(PROBE_INTERVAL.toMillis(), remainingNanos / 1_000_000L);
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }

        String message =
                "Kafka did not become ready within " + timeout + " (bootstrap.servers=" + bootstrapServers + ")";

        throw lastException != null
                ? new IllegalStateException(message, lastException)
                : new IllegalStateException(message);
    }

    /**
     * Builds AdminClient properties tuned for short-lived readiness probes.
     *
     * <p>Key choices:
     * <ul>
     *   <li>{@code metadata.max.age.ms} is set equal to the probe timeout so the client
     *       never serves stale cached metadata across probes.
     *   <li>{@code connections.max.idle.ms} is set slightly above the probe timeout so
     *       that an idle connection to an unreachable broker is cleaned up quickly.
     *   <li>{@code reconnect.backoff.max.ms} is capped to prevent the internal
     *       exponential backoff from eclipsing our probe timeout.
     *   <li>{@code retries} is 0 — all retry logic is handled by our own loop.
     * </ul>
     *
     * @param bootstrapServers the Kafka bootstrap address
     * @param probeTimeoutMs   per-call timeout in milliseconds
     * @return a configured {@link Properties} instance
     */
    private static Properties buildAdminClientProperties(String bootstrapServers, long probeTimeoutMs) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);

        // Hard deadline for any single Kafka request.
        props.put("request.timeout.ms", Long.toString(probeTimeoutMs));
        props.put("default.api.timeout.ms", Long.toString(probeTimeoutMs));

        // Disable metadata caching so each fresh AdminClient always fetches
        // current broker state rather than serving a stale "unavailable" entry.
        props.put("metadata.max.age.ms", Long.toString(probeTimeoutMs));

        // Close idle connections quickly so failed probes don't leave sockets open.
        props.put("connections.max.idle.ms", Long.toString(probeTimeoutMs + 1000L));

        // Prevent internal exponential backoff from growing beyond one probe cycle.
        props.put("reconnect.backoff.ms", "100");
        props.put("reconnect.backoff.max.ms", Long.toString(probeTimeoutMs / 2));

        // All retry logic is ours — disable internal retries to avoid double-waiting.
        props.put("retries", "0");

        return props;
    }

    /**
     * Logs the container output on startup failure.
     * Swallows secondary exceptions so the original exception propagates cleanly.
     *
     * @param originalCause the exception that triggered the failure
     */
    private void captureLogsOnFailure(Exception originalCause) {
        if (kafkaContainer == null) {
            return;
        }
        try {
            System.err.printf(
                    "Kafka container logs (cause: %s):%n%s%n", originalCause.getMessage(), kafkaContainer.getLogs());
        } catch (Exception ignored) {
            // Log retrieval must never suppress the original failure.
        }
    }

    /**
     * Stops the container without throwing. Clears the field so {@link #destroy()}
     * is idempotent.
     */
    private void stopQuietly() {
        if (kafkaContainer != null) {
            try {
                kafkaContainer.stop();
            } catch (Exception ignored) {
                // Best-effort cleanup.
            } finally {
                kafkaContainer = null;
            }
        }
    }

    /**
     * Returns {@code true} if the Kafka container is currently running.
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return kafkaContainer != null && kafkaContainer.isRunning();
    }

    /**
     * Returns the Kafka bootstrap servers address.
     *
     * @return the bootstrap servers string
     */
    public String getBootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }

    /**
     * Destroys the KafkaTestEnvironment, stopping the container.
     */
    public void destroy() {
        stopQuietly();
    }

    /**
     * Creates a {@link Stream} of {@link KafkaTestEnvironment} instances,
     * one per image name found in {@code /docker-images.txt}.
     *
     * @return a stream of environments
     * @throws IOException if the resource file cannot be read
     */
    public static Stream<KafkaTestEnvironment> createTestEnvironments() throws IOException {
        List<KafkaTestEnvironment> kafkaTestEnvironments = new ArrayList<>();
        for (String version : Resource.load(KafkaTestEnvironment.class, "/docker-images.txt")) {
            kafkaTestEnvironments.add(new KafkaTestEnvironment(version));
        }
        return kafkaTestEnvironments.stream();
    }
}
