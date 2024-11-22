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

package org.verifyica.examples.testcontainers.nginx;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.testcontainers.containers.Network;
import org.verifyica.api.Trap;
import org.verifyica.api.Verifyica;
import org.verifyica.examples.support.Logger;

public class NginxTest {

    private static final Logger LOGGER = Logger.createLogger(NginxTest.class);

    private final ThreadLocal<Network> networkThreadLocal = new ThreadLocal<>();

    @Verifyica.ArgumentSupplier(parallelism = Integer.MAX_VALUE)
    public static Stream<NginxTestEnvironment> arguments() {
        return NginxTestEnvironment.createTestEnvironments();
    }

    @Verifyica.BeforeAll
    public void initializeTestEnvironment(NginxTestEnvironment nginxTestEnvironment) {
        LOGGER.info("[%s] initialize test environment ...", nginxTestEnvironment.getName());

        Network network = Network.newNetwork();
        network.getId();

        networkThreadLocal.set(network);
        nginxTestEnvironment.initialize(network);

        assertThat(nginxTestEnvironment.isRunning()).isTrue();
    }

    @Verifyica.Test
    @Verifyica.Order(1)
    public void testGet(NginxTestEnvironment nginxTestEnvironment) throws Throwable {
        LOGGER.info("[%s] testing testGet() ...", nginxTestEnvironment.getName());

        int port = nginxTestEnvironment.getNginxContainer().getMappedPort(80);

        String content = doGet("http://localhost:" + port);

        assertThat(content).contains("Welcome to nginx!");
    }

    @Verifyica.AfterAll
    public void destroyTestEnvironment(NginxTestEnvironment nginxTestEnvironment) throws Throwable {
        LOGGER.info("[%s] destroy test environment ...", nginxTestEnvironment.getName());

        List<Trap> traps = new ArrayList<>();

        traps.add(new Trap(nginxTestEnvironment::destroy));
        traps.add(new Trap(() -> ofNullable(networkThreadLocal.get()).ifPresent(Network::close)));
        traps.add(new Trap(networkThreadLocal::remove));

        Trap.assertEmpty(traps);
    }

    public static String doGet(String urlString) throws Throwable {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }
}
