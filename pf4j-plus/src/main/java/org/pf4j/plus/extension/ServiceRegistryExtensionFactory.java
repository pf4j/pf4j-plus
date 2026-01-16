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
package org.pf4j.plus.extension;

import org.pf4j.ExtensionFactory;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Decorator for {@link ExtensionFactory} that provides {@link ServiceRegistry} support.
 * <p>
 * This decorator wraps an existing ExtensionFactory and enhances extensions that
 * implement {@link ServiceRegistryAware} by injecting the appropriate ServiceRegistry
 * instance after creation.
 * <p>
 * Extensions from plugins receive their plugin's scoped registry (which has access
 * to both plugin-specific and global services). System extensions (from the application)
 * receive the global application registry.
 * <p>
 * Example usage:
 * <pre>
 * Map&lt;ClassLoader, ServiceRegistry&gt; pluginRegistries = new ConcurrentHashMap&lt;&gt;();
 * ServiceRegistry appRegistry = new DefaultServiceRegistry();
 * ExtensionFactory factory = new ServiceRegistryExtensionFactory(
 *     new DefaultExtensionFactory(),
 *     appRegistry,
 *     pluginRegistries
 * );
 * pluginManager.setExtensionFactory(factory);
 * </pre>
 *
 * @author Decebal Suiu
 * @see ServiceRegistryAware
 * @see InjectingExtensionFactory
 */
public class ServiceRegistryExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistryExtensionFactory.class);

    private final ExtensionFactory delegate;
    private final ServiceRegistry appRegistry;
    private final Map<ClassLoader, ServiceRegistry> pluginRegistries;

    /**
     * Creates a new ServiceRegistryExtensionFactory.
     *
     * @param delegate the original ExtensionFactory to decorate
     * @param appRegistry the application's global ServiceRegistry (fallback for system extensions)
     * @param pluginRegistries shared map containing plugin registries (populated by PluginFactory)
     * @throws NullPointerException if any argument is null
     */
    public ServiceRegistryExtensionFactory(ExtensionFactory delegate, ServiceRegistry appRegistry,
                                           Map<ClassLoader, ServiceRegistry> pluginRegistries) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate ExtensionFactory cannot be null");
        this.appRegistry = Objects.requireNonNull(appRegistry, "Application ServiceRegistry cannot be null");
        this.pluginRegistries = Objects.requireNonNull(pluginRegistries, "Plugin registries map cannot be null");
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        log.debug("Creating extension '{}'", extensionClass.getName());
        T extension = delegate.create(extensionClass);

        if (extension instanceof ServiceRegistryAware) {
            ServiceRegistry registry = getRegistryForExtension(extensionClass);
            log.debug("Extension '{}' is ServiceRegistryAware, injecting ServiceRegistry", extensionClass.getName());
            ((ServiceRegistryAware) extension).setServiceRegistry(registry);
        }

        return extension;
    }

    /**
     * Returns the appropriate ServiceRegistry for an extension class.
     * <p>
     * Plugin extensions get their plugin's scoped registry.
     * System extensions get the application registry.
     *
     * @param extensionClass the extension class
     * @return the appropriate registry for this extension
     */
    private ServiceRegistry getRegistryForExtension(Class<?> extensionClass) {
        ClassLoader classLoader = extensionClass.getClassLoader();
        ServiceRegistry registry = pluginRegistries.get(classLoader);
        if (registry != null) {
            log.trace("Found plugin registry for extension '{}'", extensionClass.getName());
            return registry;
        }
        log.trace("No plugin registry found for extension '{}', using application registry", extensionClass.getName());
        return appRegistry;
    }

}
