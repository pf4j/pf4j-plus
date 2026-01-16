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

import java.util.Objects;
import java.util.Optional;

/**
 * Event fired when a plugin configuration value changes.
 *
 * @author Decebal Suiu
 */
public final class ConfigChangeEvent {

    private final String pluginId;
    private final String key;
    private final Optional<String> oldValue;
    private final Optional<String> newValue;

    public ConfigChangeEvent(String pluginId, String key, Optional<String> oldValue, Optional<String> newValue) {
        this.pluginId = pluginId;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Convenience constructor that wraps nullable values in Optional.
     */
    public ConfigChangeEvent(String pluginId, String key, String oldValue, String newValue) {
        this(pluginId, key, Optional.ofNullable(oldValue), Optional.ofNullable(newValue));
    }

    public String pluginId() {
        return pluginId;
    }

    public String key() {
        return key;
    }

    public Optional<String> oldValue() {
        return oldValue;
    }

    public Optional<String> newValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigChangeEvent)) return false;
        ConfigChangeEvent that = (ConfigChangeEvent) o;
        return Objects.equals(pluginId, that.pluginId)
            && Objects.equals(key, that.key)
            && Objects.equals(oldValue, that.oldValue)
            && Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, key, oldValue, newValue);
    }

    @Override
    public String toString() {
        return "ConfigChangeEvent{" +
            "pluginId='" + pluginId + '\'' +
            ", key='" + key + '\'' +
            ", oldValue=" + oldValue +
            ", newValue=" + newValue +
            '}';
    }

}