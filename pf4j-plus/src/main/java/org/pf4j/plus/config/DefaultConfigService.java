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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default implementation of {@link ConfigService}.
 * <p>
 * Manages plugin configurations with lazy loading and automatic persistence.
 * <p>
 * By default, configuration is auto-saved on every change. If a {@link ConfigChangeListener}
 * is registered, auto-save is disabled and the listener takes full control of persistence.
 *
 * @author Decebal Suiu
 */
public class DefaultConfigService implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(DefaultConfigService.class);

    private final ConfigPersister persister;
    private final ConcurrentMap<String, DefaultPluginConfig> configs;

    // Volatile is sufficient here: only simple read/write operations on a reference, no compound actions
    @SuppressWarnings("java:S3077")
    private volatile ConfigChangeListener changeListener;

    public DefaultConfigService(ConfigPersister persister) {
        this.persister = persister;
        this.configs = new ConcurrentHashMap<>();
    }

    @Override
    public PluginConfig forPlugin(String pluginId) {
        return configs.computeIfAbsent(pluginId, this::createPluginConfig);
    }

    /**
     * Register a listener for configuration changes.
     * <p>
     * When a listener is registered, auto-save is disabled.
     * The listener is responsible for calling save when appropriate.
     *
     * @param listener the change listener
     */
    public void setChangeListener(ConfigChangeListener listener) {
        this.changeListener = listener;
    }

    /**
     * Remove the change listener and restore auto-save behavior.
     */
    public void removeChangeListener() {
        this.changeListener = null;
    }

    /**
     * Manually save configuration for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    public void save(String pluginId) {
        DefaultPluginConfig config = configs.get(pluginId);
        if (config != null) {
            log.debug("Saving configuration for plugin '{}'", pluginId);
            persister.save(pluginId, config.getAll());
        }
    }

    /**
     * Save configuration for all loaded plugins.
     */
    public void saveAll() {
        configs.keySet().forEach(this::save);
    }

    /**
     * Delete configuration for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    public void delete(String pluginId) {
        configs.remove(pluginId);
        persister.delete(pluginId);
        log.debug("Deleted configuration for plugin '{}'", pluginId);
    }

    private DefaultPluginConfig createPluginConfig(String pluginId) {
        log.debug("Loading configuration for plugin '{}'", pluginId);
        Map<String, String> data = persister.load(pluginId);
        return new DefaultPluginConfig(pluginId, data, this::onConfigChanged);
    }

    private void onConfigChanged(ConfigChangeEvent event) {
        if (changeListener != null) {
            // Listener takes control - no auto-save
            changeListener.onConfigChanged(event);
        } else {
            // Auto-save on every change
            DefaultPluginConfig config = configs.get(event.pluginId());
            if (config != null) {
                log.trace("Auto-saving configuration for plugin '{}' (key='{}')", event.pluginId(), event.key());
                persister.save(event.pluginId(), config.getAll());
            }
        }
    }

}