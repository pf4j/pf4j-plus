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
import org.pf4j.plus.injection.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Decorator for {@link PluginFactory} that provides dependency injection support.
 * <p>
 * This decorator wraps an existing PluginFactory and performs field injection
 * on created plugins using the {@link Injector}.
 * Fields marked with {@link org.pf4j.plus.injection.Inject} will be populated
 * with services from the ServiceRegistry.
 * <p>
 * Example usage:
 * <pre>
 * ServiceRegistry registry = new DefaultServiceRegistry();
 * Injector injector = new DefaultInjector(registry);
 * PluginFactory factory = new InjectingPluginFactory(
 *     new DefaultPluginFactory(),
 *     injector
 * );
 * pluginManager.setPluginFactory(factory);
 * </pre>
 * <p>
 * This decorator can be combined with {@link ServiceRegistryPluginFactory}
 * for complete service support:
 * <pre>
 * PluginFactory factory = new InjectingPluginFactory(
 *     new ServiceRegistryPluginFactory(
 *         new DefaultPluginFactory(),
 *         registry
 *     ),
 *     injector
 * );
 * </pre>
 *
 * @author Decebal Suiu
 * @see Injector
 * @see org.pf4j.plus.injection.Inject
 * @see ServiceRegistryPluginFactory
 */
public class InjectingPluginFactory implements PluginFactory {

    private static final Logger log = LoggerFactory.getLogger(InjectingPluginFactory.class);

    private final PluginFactory delegate;
    private final Injector injector;

    /**
     * Creates a new InjectingPluginFactory.
     *
     * @param delegate the original PluginFactory to decorate
     * @param injector the Injector to use for dependency injection
     * @throws IllegalArgumentException if delegate or injector is null
     */
    public InjectingPluginFactory(PluginFactory delegate, Injector injector) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate PluginFactory cannot be null");
        this.injector = Objects.requireNonNull(injector, "Injector cannot be null");
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        log.debug("Creating plugin '{}' with dependency injection", pluginWrapper.getPluginId());
        Plugin plugin = delegate.create(pluginWrapper);

        log.debug("Injecting dependencies into plugin '{}'", pluginWrapper.getPluginId());
        injector.inject(plugin);

        return plugin;
    }

}
