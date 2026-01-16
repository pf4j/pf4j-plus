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

import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;
import org.pf4j.plus.config.ConfigService;
import org.pf4j.plus.config.PluginConfig;
import org.pf4j.plus.service.PluginServiceRegistry;
import org.pf4j.plus.service.ServiceProvider;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Decorator for {@link PluginFactory} that provides {@link ServiceRegistry} support.
 * <p>
 * This decorator wraps an existing PluginFactory and creates a scoped
 * {@link PluginServiceRegistry} for each plugin. The scoped registry:
 * <ul>
 *   <li>Provides access to global services from the application registry</li>
 *   <li>Automatically registers plugin-specific services like {@link PluginInfo} and {@link PluginConfig}</li>
 *   <li>Allows plugins to register local services without affecting the global registry</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * Map&lt;ClassLoader, ServiceRegistry&gt; pluginRegistries = new ConcurrentHashMap&lt;&gt;();
 * ServiceRegistry appRegistry = new DefaultServiceRegistry();
 * PluginFactory factory = new ServiceRegistryPluginFactory(
 *     new DefaultPluginFactory(),
 *     appRegistry,
 *     pluginRegistries
 * );
 * pluginManager.setPluginFactory(factory);
 * </pre>
 *
 * @author Decebal Suiu
 * @see ServiceRegistryAware
 * @see PluginServiceRegistry
 * @see InjectingPluginFactory
 */
public class ServiceRegistryPluginFactory implements PluginFactory {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistryPluginFactory.class);

    private final PluginFactory delegate;
    private final ServiceRegistry appRegistry;
    private final Map<ClassLoader, ServiceRegistry> pluginRegistries;

    /**
     * Creates a new ServiceRegistryPluginFactory.
     *
     * @param delegate the original PluginFactory to decorate
     * @param appRegistry the application's global ServiceRegistry
     * @param pluginRegistries shared map to store plugin registries (for extension factory lookup)
     * @throws NullPointerException if any argument is null
     */
    public ServiceRegistryPluginFactory(PluginFactory delegate, ServiceRegistry appRegistry,
                                        Map<ClassLoader, ServiceRegistry> pluginRegistries) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate PluginFactory cannot be null");
        this.appRegistry = Objects.requireNonNull(appRegistry, "Application ServiceRegistry cannot be null");
        this.pluginRegistries = Objects.requireNonNull(pluginRegistries, "Plugin registries map cannot be null");
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        String pluginId = pluginWrapper.getPluginId();
        log.debug("Creating plugin for '{}'", pluginId);

        Plugin plugin = delegate.create(pluginWrapper);

        // Create scoped registry for this plugin
        ServiceRegistry pluginRegistry = createPluginRegistry(pluginWrapper);

        // Store for later lookup by ExtensionFactory
        pluginRegistries.put(pluginWrapper.getPluginClassLoader(), pluginRegistry);

        if (plugin instanceof ServiceRegistryAware) {
            log.debug("Plugin '{}' is ServiceRegistryAware, injecting PluginServiceRegistry", pluginId);
            ((ServiceRegistryAware) plugin).setServiceRegistry(pluginRegistry);
        }

        return plugin;
    }

    /**
     * Creates a scoped ServiceRegistry for a plugin.
     * <p>
     * Subclasses can override this method to customize the plugin registry
     * (e.g., register additional services).
     *
     * @param pluginWrapper the plugin wrapper
     * @return the scoped registry for this plugin
     */
    protected ServiceRegistry createPluginRegistry(PluginWrapper pluginWrapper) {
        String pluginId = pluginWrapper.getPluginId();
        log.debug("Creating PluginServiceRegistry for '{}'", pluginId);

        PluginServiceRegistry pluginRegistry = new PluginServiceRegistry(appRegistry);

        // Register PluginInfo
        pluginRegistry.register(PluginInfo.class, PluginInfo.from(pluginWrapper));

        // Register PluginConfig (lazy via provider)
        ServiceProvider<PluginConfig> configProvider = () -> {
            ConfigService configService = appRegistry.require(ConfigService.class);
            return configService.forPlugin(pluginId);
        };
        pluginRegistry.register(PluginConfig.class, configProvider);

        return pluginRegistry;
    }

}
