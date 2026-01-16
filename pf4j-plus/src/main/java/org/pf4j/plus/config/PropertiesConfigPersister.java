/*
 * Copyright (C) 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.plus.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@link ConfigPersister} implementation using Java Properties files.
 * <p>
 * Each plugin's configuration is stored in a separate {@code .properties} file
 * within the configured directory.
 * <p>
 * Files are stored as UTF-8 encoded text.
 *
 * @author Decebal Suiu
 */
public class PropertiesConfigPersister implements ConfigPersister {

    private static final Logger log = LoggerFactory.getLogger(PropertiesConfigPersister.class);
    private static final String FILE_EXTENSION = ".properties";

    private final Path configDirectory;

    /**
     * Create a persister that stores configuration in the given directory.
     *
     * @param configDirectory the directory where config files are stored
     */
    public PropertiesConfigPersister(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    @Override
    public Map<String, String> load(String pluginId) {
        Path configFile = getConfigFile(pluginId);
        Map<String, String> result = new HashMap<>();

        if (Files.exists(configFile)) {
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                properties.load(reader);
                properties.forEach((key, value) -> result.put((String) key, (String) value));
                log.debug("Loaded {} properties from '{}'", result.size(), configFile);
            } catch (IOException e) {
                log.error("Failed to load configuration from '{}'", configFile, e);
            }
        } else {
            log.debug("No configuration file found at '{}'", configFile);
        }

        return result;
    }

    @Override
    public void save(String pluginId, Map<String, String> config) {
        Path configFile = getConfigFile(pluginId);

        try {
            // Ensure directory exists
            Files.createDirectories(configDirectory);

            Properties properties = new Properties();
            properties.putAll(config);

            try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                properties.store(writer, "Configuration for plugin: " + pluginId);
                log.debug("Saved {} properties to '{}'", config.size(), configFile);
            }
        } catch (IOException e) {
            log.error("Failed to save configuration to '{}'", configFile, e);
        }
    }

    @Override
    public void delete(String pluginId) {
        Path configFile = getConfigFile(pluginId);
        try {
            if (Files.deleteIfExists(configFile)) {
                log.debug("Deleted configuration file '{}'", configFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete configuration file '{}'", configFile, e);
        }
    }

    private Path getConfigFile(String pluginId) {
        return configDirectory.resolve(pluginId + FILE_EXTENSION);
    }

}