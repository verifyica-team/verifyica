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

package org.verifyica.engine.configuration;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.verifyica.api.Configuration;
import org.verifyica.engine.common.ImmutableProperties;

/** Class to implement ImmutableConfiguration */
public class ImmutableConfiguration implements Configuration {

    private final Path propertiesFilenamePath;
    private final Properties properties;

    /**
     * Constructor
     *
     * @param configuration configuration
     */
    public ImmutableConfiguration(Configuration configuration) {
        propertiesFilenamePath = configuration.getPropertiesFilename().orElse(null);
        properties = new ImmutableProperties(configuration.getProperties());
    }

    @Override
    public Optional<Path> getPropertiesFilename() {
        return Optional.ofNullable(propertiesFilenamePath);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
