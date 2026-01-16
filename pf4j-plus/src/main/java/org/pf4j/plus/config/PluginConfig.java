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

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Configuration view for a specific plugin.
 * <p>
 * Provides typed accessors with default value support.
 * All values are stored internally as strings.
 *
 * @author Decebal Suiu
 */
public interface PluginConfig {

    /**
     * Get a configuration value.
     *
     * @param key the configuration key
     * @return the value, or empty if not set
     */
    Optional<String> get(String key);

    /**
     * Get a configuration value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not set
     * @return the value, or defaultValue if not set
     */
    default String get(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }

    /**
     * Set a configuration value.
     *
     * @param key the configuration key
     * @param value the value to set
     */
    void set(String key, String value);

    /**
     * Remove a configuration value.
     *
     * @param key the configuration key
     */
    void remove(String key);

    /**
     * Get all configuration keys.
     *
     * @return an unmodifiable set of all keys
     */
    Set<String> keys();

    // Typed helpers with defaults

    default int getInt(String key, int defaultValue) {
        return get(key).map(Integer::parseInt).orElse(defaultValue);
    }

    default void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

    default long getLong(String key, long defaultValue) {
        return get(key).map(Long::parseLong).orElse(defaultValue);
    }

    default void setLong(String key, long value) {
        set(key, String.valueOf(value));
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    default void setBoolean(String key, boolean value) {
        set(key, String.valueOf(value));
    }

    default double getDouble(String key, double defaultValue) {
        return get(key).map(Double::parseDouble).orElse(defaultValue);
    }

    default void setDouble(String key, double value) {
        set(key, String.valueOf(value));
    }

    default List<String> getList(String key) {
        return getList(key, List.of());
    }

    default List<String> getList(String key, List<String> defaultValue) {
        return get(key)
            .map(value -> List.of(value.split(",")))
            .orElse(defaultValue);
    }

    default void setList(String key, List<String> value) {
        set(key, String.join(",", value));
    }

}