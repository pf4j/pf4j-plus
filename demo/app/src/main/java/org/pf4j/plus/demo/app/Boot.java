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
package org.pf4j.plus.demo.app;

import org.pf4j.PluginManager;
import org.pf4j.plus.PlusPluginManagerBuilder;
import org.pf4j.plus.config.ConfigService;
import org.pf4j.plus.config.DefaultConfigService;
import org.pf4j.plus.config.PropertiesConfigPersister;
import org.pf4j.plus.event.DefaultEventBus;
import org.pf4j.plus.event.EventBus;

import java.nio.file.Path;
import java.util.List;

/**
 * Demo application showing PF4J-Plus features.
 * <p>
 * This application demonstrates:
 * <ul>
 *   <li>Using {@link PlusPluginManagerBuilder} to create a PluginManager</li>
 *   <li>Inline service registration with ServiceRegistry</li>
 *   <li>Custom class loading with {@code excludeFromParentDelegation()}</li>
 *   <li>EventBus for cross-plugin communication</li>
 *   <li>ConfigService for plugin configuration</li>
 *   <li>Loading and starting plugins</li>
 *   <li>Using extensions</li>
 * </ul>
 */
public class Boot {

    public static void main(String[] args) {
        System.out.println("=== PF4J-Plus Demo Application ===\n");

        // 1. Create PluginManager using the builder
        System.out.println("1. Creating PluginManager with PlusPluginManagerBuilder...");

        PluginManager pluginManager = PlusPluginManagerBuilder.create()
            .serviceRegistry(registry -> {
                // Register GreetingService
                registry.register(GreetingService.class, new DefaultGreetingService());
                System.out.println("   - Registered GreetingService");

                // Register EventBus
                registry.register(EventBus.class, new DefaultEventBus());
                System.out.println("   - Registered EventBus");

                // Register ConfigService
                ConfigService configService = new DefaultConfigService(
                    new PropertiesConfigPersister(Path.of("config"))
                );
                registry.register(ConfigService.class, configService);
                System.out.println("   - Registered ConfigService (config directory: ./config)");
            })
            // Exclude demo packages from parent class loader delegation
            // This allows plugins to have their own copies of demo classes
            .excludeFromParentDelegation("org.pf4j.plus.demo")
            .build();

        System.out.println("   - PluginManager created with ServiceRegistry and Injection support");

        // 2. Load and start plugins
        System.out.println("\n2. Loading and starting plugins...");
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        System.out.println("   - Loaded " + pluginManager.getPlugins().size() + " plugin(s)");

        // 3. Use extensions
        System.out.println("\n3. Executing Greeter extensions...");
        List<Greeter> greeters = pluginManager.getExtensions(Greeter.class);
        System.out.println("   - Found " + greeters.size() + " Greeter extension(s)");

        for (Greeter greeter : greeters) {
            greeter.greet();
        }

        // 4. Stop plugins
        System.out.println("\n4. Stopping plugins...");
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();

        System.out.println("\n=== Demo completed successfully! ===");
    }

}
