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
package org.pf4j.plus;

import org.pf4j.CompoundPluginLoader;
import org.pf4j.DefaultPluginLoader;
import org.pf4j.DefaultPluginManager;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.ExtensionFactory;
import org.pf4j.JarPluginLoader;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginFactory;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.pf4j.ZipPluginManager;
import org.pf4j.plus.extension.InjectingExtensionFactory;
import org.pf4j.plus.extension.ServiceRegistryExtensionFactory;
import org.pf4j.plus.injection.DefaultInjector;
import org.pf4j.plus.injection.Injector;
import org.pf4j.plus.plugin.InjectingPluginFactory;
import org.pf4j.plus.plugin.ServiceRegistryPluginFactory;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Builder for creating a {@link PluginManager} with PF4J-Plus features.
 * <p>
 * This builder simplifies the setup of a PluginManager with ServiceRegistry
 * and dependency injection support.
 * <p>
 * Basic usage:
 * <pre>
 * PluginManager pluginManager = PlusPluginManagerBuilder.create()
 *     .serviceRegistry(registry)
 *     .build();
 * </pre>
 * <p>
 * With inline service registration:
 * <pre>
 * PluginManager pluginManager = PlusPluginManagerBuilder.create()
 *     .serviceRegistry(registry -&gt; {
 *         registry.register(MyService.class, new MyServiceImpl());
 *         registry.register(EventBus.class, new DefaultEventBus());
 *     })
 *     .build();
 * </pre>
 * <p>
 * With custom class loading (exclude packages from parent delegation):
 * <pre>
 * PluginManager pluginManager = PlusPluginManagerBuilder.create()
 *     .serviceRegistry(registry)
 *     .excludeFromParentDelegation("org.pf4j.plus.demo")
 *     .build();
 * </pre>
 *
 * @author Decebal Suiu
 */
public class PlusPluginManagerBuilder {

    /**
     * Plugin manager types supported by the builder.
     */
    public enum PluginManagerType {
        /** {@link DefaultPluginManager} - supports both JAR and ZIP plugins */
        DEFAULT,
        /** {@link JarPluginManager} - JAR plugins only */
        JAR,
        /** {@link ZipPluginManager} - ZIP plugins only */
        ZIP
    }

    private ServiceRegistry serviceRegistry;
    private Injector injector;
    private Path pluginsRoot;
    private PluginManagerType type = PluginManagerType.DEFAULT;
    private Map<ClassLoader, ServiceRegistry> pluginRegistries;
    private final Set<String> excludedPackages = new HashSet<>();

    private PlusPluginManagerBuilder() {
    }

    /**
     * Creates a new builder instance.
     *
     * @return a new builder
     */
    public static PlusPluginManagerBuilder create() {
        return new PlusPluginManagerBuilder();
    }

    /**
     * Sets the service registry to use.
     *
     * @param serviceRegistry the service registry
     * @return this builder
     */
    public PlusPluginManagerBuilder serviceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
        return this;
    }

    /**
     * Creates a new service registry and configures it using the provided consumer.
     * <p>
     * Example:
     * <pre>
     * .serviceRegistry(registry -&gt; {
     *     registry.register(MyService.class, new MyServiceImpl());
     * })
     * </pre>
     *
     * @param configurer the consumer to configure the registry
     * @return this builder
     */
    public PlusPluginManagerBuilder serviceRegistry(Consumer<ServiceRegistry> configurer) {
        Objects.requireNonNull(configurer, "Configurer cannot be null");
        this.serviceRegistry = new DefaultServiceRegistry();
        configurer.accept(this.serviceRegistry);
        return this;
    }

    /**
     * Sets a custom injector to use.
     * <p>
     * If not set, a {@link DefaultInjector} will be created automatically.
     *
     * @param injector the injector
     * @return this builder
     */
    public PlusPluginManagerBuilder injector(Injector injector) {
        this.injector = Objects.requireNonNull(injector, "Injector cannot be null");
        return this;
    }

    /**
     * Sets the plugins root directory.
     * <p>
     * If not set, the default location will be used (typically "./plugins").
     *
     * @param pluginsRoot the path to the plugins directory
     * @return this builder
     */
    public PlusPluginManagerBuilder pluginsRoot(Path pluginsRoot) {
        this.pluginsRoot = pluginsRoot;
        return this;
    }

    /**
     * Sets the plugin manager type.
     * <p>
     * If not set, {@link PluginManagerType#DEFAULT} is used.
     *
     * @param type the plugin manager type
     * @return this builder
     */
    public PlusPluginManagerBuilder type(PluginManagerType type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        return this;
    }

    /**
     * Excludes packages from parent class loader delegation.
     * <p>
     * By default, PF4J delegates all classes starting with {@code org.pf4j.} to the
     * parent class loader. This method allows excluding specific packages from that
     * delegation, which is useful when plugins contain classes in the {@code org.pf4j}
     * package hierarchy that should be loaded by the plugin class loader instead.
     * <p>
     * Example:
     * <pre>
     * .excludeFromParentDelegation("org.pf4j.plus.demo")
     * </pre>
     *
     * @param packages the package prefixes to exclude from parent delegation
     * @return this builder
     */
    public PlusPluginManagerBuilder excludeFromParentDelegation(String... packages) {
        if (packages != null) {
            excludedPackages.addAll(Arrays.asList(packages));
        }
        return this;
    }

    /**
     * Builds the configured {@link PluginManager}.
     *
     * @return a new PluginManager with PF4J-Plus features enabled
     */
    public PluginManager build() {
        if (serviceRegistry == null) {
            serviceRegistry = new DefaultServiceRegistry();
        }

        if (injector == null) {
            injector = new DefaultInjector(serviceRegistry);
        }

        pluginRegistries = new ConcurrentHashMap<>();

        return createPluginManager();
    }

    private PluginManager createPluginManager() {
        switch (type) {
            case JAR:
                return pluginsRoot != null ? createJarPluginManager(pluginsRoot) : createJarPluginManager();
            case ZIP:
                // ZipPluginManager doesn't support custom pluginsRoot
                return createZipPluginManager();
            default:
                return pluginsRoot != null ? createDefaultPluginManager(pluginsRoot) : createDefaultPluginManager();
        }
    }

    private PluginManager createDefaultPluginManager() {
        return new DefaultPluginManager() {
            @Override
            protected PluginLoader createPluginLoader() {
                return hasExcludedPackages()
                    ? createCustomPluginLoader(this)
                    : super.createPluginLoader();
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return decorateExtensionFactory(super.createExtensionFactory());
            }

            @Override
            protected PluginFactory createPluginFactory() {
                return decoratePluginFactory(super.createPluginFactory());
            }
        };
    }

    private PluginManager createDefaultPluginManager(Path pluginsRoot) {
        return new DefaultPluginManager(pluginsRoot) {
            @Override
            protected PluginLoader createPluginLoader() {
                return hasExcludedPackages()
                    ? createCustomPluginLoader(this)
                    : super.createPluginLoader();
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return decorateExtensionFactory(super.createExtensionFactory());
            }

            @Override
            protected PluginFactory createPluginFactory() {
                return decoratePluginFactory(super.createPluginFactory());
            }
        };
    }

    private PluginManager createJarPluginManager() {
        return new JarPluginManager() {
            @Override
            protected PluginLoader createPluginLoader() {
                return hasExcludedPackages()
                    ? createCustomPluginLoader(this)
                    : super.createPluginLoader();
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return decorateExtensionFactory(super.createExtensionFactory());
            }

            @Override
            protected PluginFactory createPluginFactory() {
                return decoratePluginFactory(super.createPluginFactory());
            }
        };
    }

    private PluginManager createJarPluginManager(Path pluginsRoot) {
        return new JarPluginManager(pluginsRoot) {
            @Override
            protected PluginLoader createPluginLoader() {
                return hasExcludedPackages()
                    ? createCustomPluginLoader(this)
                    : super.createPluginLoader();
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return decorateExtensionFactory(super.createExtensionFactory());
            }

            @Override
            protected PluginFactory createPluginFactory() {
                return decoratePluginFactory(super.createPluginFactory());
            }
        };
    }

    private PluginManager createZipPluginManager() {
        return new ZipPluginManager() {
            @Override
            protected PluginLoader createPluginLoader() {
                return hasExcludedPackages()
                    ? createCustomPluginLoader(this)
                    : super.createPluginLoader();
            }

            @Override
            protected ExtensionFactory createExtensionFactory() {
                return decorateExtensionFactory(super.createExtensionFactory());
            }

            @Override
            protected PluginFactory createPluginFactory() {
                return decoratePluginFactory(super.createPluginFactory());
            }
        };
    }

    private boolean hasExcludedPackages() {
        return !excludedPackages.isEmpty();
    }

    private PluginLoader createCustomPluginLoader(PluginManager pluginManager) {
        CompoundPluginLoader pluginLoader = new CompoundPluginLoader();

        pluginLoader.add(new DevelopmentPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return createCustomPluginClassLoader(pluginManager, pluginDescriptor);
            }
        }, pluginManager::isDevelopment);

        pluginLoader.add(new JarPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return createCustomPluginClassLoader(pluginManager, pluginDescriptor);
            }
        }, pluginManager::isNotDevelopment);

        pluginLoader.add(new DefaultPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return createCustomPluginClassLoader(pluginManager, pluginDescriptor);
            }
        }, pluginManager::isNotDevelopment);

        return pluginLoader;
    }

    private PluginClassLoader createCustomPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor) {
        return new PluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader()) {
            @Override
            protected boolean shouldDelegateToParent(String className) {
                for (String excludedPackage : excludedPackages) {
                    if (className.startsWith(excludedPackage)) {
                        return false;
                    }
                }
                return super.shouldDelegateToParent(className);
            }
        };
    }

    private ExtensionFactory decorateExtensionFactory(ExtensionFactory delegate) {
        return new InjectingExtensionFactory(
            new ServiceRegistryExtensionFactory(delegate, serviceRegistry, pluginRegistries),
            injector
        );
    }

    private PluginFactory decoratePluginFactory(PluginFactory delegate) {
        return new InjectingPluginFactory(
            new ServiceRegistryPluginFactory(delegate, serviceRegistry, pluginRegistries),
            injector
        );
    }

}