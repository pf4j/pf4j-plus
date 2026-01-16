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
import org.pf4j.plus.injection.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Decorator for {@link ExtensionFactory} that provides dependency injection support.
 * <p>
 * This decorator wraps an existing ExtensionFactory and performs field injection
 * on created extensions using the {@link Injector}.
 * Fields marked with {@link org.pf4j.plus.injection.Inject} will be populated
 * with services from the ServiceRegistry.
 * <p>
 * Example usage:
 * <pre>
 * ServiceRegistry registry = new DefaultServiceRegistry();
 * Injector injector = new DefaultInjector(registry);
 * ExtensionFactory factory = new InjectingExtensionFactory(
 *     new DefaultExtensionFactory(),
 *     injector
 * );
 * pluginManager.setExtensionFactory(factory);
 * </pre>
 * <p>
 * This decorator can be combined with {@link ServiceRegistryExtensionFactory}
 * for complete service support:
 * <pre>
 * ExtensionFactory factory = new InjectingExtensionFactory(
 *     new ServiceRegistryExtensionFactory(
 *         new DefaultExtensionFactory(),
 *         registry
 *     ),
 *     injector
 * );
 * </pre>
 *
 * @author Decebal Suiu
 * @see Injector
 * @see org.pf4j.plus.injection.Inject
 * @see ServiceRegistryExtensionFactory
 */
public class InjectingExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(InjectingExtensionFactory.class);

    private final ExtensionFactory delegate;
    private final Injector injector;

    /**
     * Creates a new InjectingExtensionFactory.
     *
     * @param delegate the original ExtensionFactory to decorate
     * @param injector the Injector to use for dependency injection
     * @throws IllegalArgumentException if delegate or injector is null
     */
    public InjectingExtensionFactory(ExtensionFactory delegate, Injector injector) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate ExtensionFactory cannot be null");
        this.injector = Objects.requireNonNull(injector, "Injector cannot be null");
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        log.debug("Creating extension '{}' with dependency injection", extensionClass.getName());
        T extension = delegate.create(extensionClass);

        log.debug("Injecting dependencies into extension '{}'", extensionClass.getName());
        injector.inject(extension);

        return extension;
    }

}
