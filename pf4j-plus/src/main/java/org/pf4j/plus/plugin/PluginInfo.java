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
package org.pf4j.plus.plugin;

import org.pf4j.PluginWrapper;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Provides read-only information about a plugin.
 * <p>
 * This class exposes plugin metadata without giving access to
 * the plugin manager or other sensitive operations. It is designed
 * to be injected into plugins and extensions that need to know
 * their own identity.
 * <p>
 * Unlike {@link PluginWrapper}, this class does not expose
 * the plugin manager, preventing plugins from performing unauthorized
 * operations like stopping or unloading other plugins.
 *
 * @author Decebal Suiu
 */
public class PluginInfo {

    private final String pluginId;
    private final String version;
    private final String description;
    private final String provider;
    private final Path pluginPath;

    /**
     * Creates a new PluginInfo.
     *
     * @param pluginId the plugin identifier
     * @param version the plugin version
     * @param description the plugin description
     * @param provider the plugin provider (author)
     * @param pluginPath the plugin path
     */
    public PluginInfo(String pluginId, String version, String description, String provider, Path pluginPath) {
        this.pluginId = Objects.requireNonNull(pluginId, "pluginId cannot be null");
        this.version = version;
        this.description = description;
        this.provider = provider;
        this.pluginPath = pluginPath;
    }

    /**
     * Creates a PluginInfo from a PluginWrapper.
     *
     * @param pluginWrapper the plugin wrapper
     * @return a new PluginInfo instance
     */
    public static PluginInfo from(PluginWrapper pluginWrapper) {
        return new PluginInfo(
            pluginWrapper.getPluginId(),
            pluginWrapper.getDescriptor().getVersion(),
            pluginWrapper.getDescriptor().getPluginDescription(),
            pluginWrapper.getDescriptor().getProvider(),
            pluginWrapper.getPluginPath()
        );
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getProvider() {
        return provider;
    }

    public Path getPluginPath() {
        return pluginPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginInfo that = (PluginInfo) o;
        return Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId);
    }

    @Override
    public String toString() {
        return "PluginInfo{" +
            "pluginId='" + pluginId + '\'' +
            ", version='" + version + '\'' +
            '}';
    }

}