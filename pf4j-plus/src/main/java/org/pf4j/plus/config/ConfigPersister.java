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

import java.util.Map;

/**
 * SPI for plugin configuration persistence.
 * <p>
 * Host application implements this to control where/how configuration is stored.
 *
 * @author Decebal Suiu
 */
public interface ConfigPersister {

    /**
     * Load configuration for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the configuration as key-value pairs, or empty map if none exists
     */
    Map<String, String> load(String pluginId);

    /**
     * Save configuration for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param config the configuration as key-value pairs
     */
    void save(String pluginId, Map<String, String> config);

    /**
     * Delete configuration for a plugin.
     * <p>
     * Called when plugin is uninstalled (if host chooses to cleanup).
     *
     * @param pluginId the plugin identifier
     */
    default void delete(String pluginId) {
        // Default: no-op. Host can override to cleanup on uninstall.
    }

}