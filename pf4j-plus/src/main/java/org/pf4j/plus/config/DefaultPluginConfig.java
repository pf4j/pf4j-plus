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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default implementation of {@link PluginConfig}.
 * <p>
 * Stores configuration in memory and notifies a callback on changes.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginConfig implements PluginConfig {

    private final String pluginId;
    private final ConcurrentHashMap<String, String> data;
    private final Consumer<ConfigChangeEvent> changeCallback;

    public DefaultPluginConfig(String pluginId, Map<String, String> initialData, Consumer<ConfigChangeEvent> changeCallback) {
        this.pluginId = pluginId;
        this.data = new ConcurrentHashMap<>(initialData);
        this.changeCallback = changeCallback;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public void set(String key, String value) {
        String oldValue = data.put(key, value);
        if (changeCallback != null) {
            changeCallback.accept(new ConfigChangeEvent(pluginId, key, oldValue, value));
        }
    }

    @Override
    public void remove(String key) {
        String oldValue = data.remove(key);
        if (oldValue != null && changeCallback != null) {
            changeCallback.accept(new ConfigChangeEvent(pluginId, key, oldValue, null));
        }
    }

    @Override
    public Set<String> keys() {
        return Set.copyOf(data.keySet());
    }

    /**
     * Get a snapshot of all configuration data.
     *
     * @return unmodifiable copy of the configuration
     */
    public Map<String, String> getAll() {
        return Map.copyOf(data);
    }

}