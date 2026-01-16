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
package org.pf4j.plus.demo.plugins.greeting;

import org.pf4j.Plugin;
import org.pf4j.plus.config.PluginConfig;
import org.pf4j.plus.demo.app.GreetingService;
import org.pf4j.plus.plugin.PluginInfo;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;

/**
 * Greeting plugin demonstrating scoped ServiceRegistry with automatic
 * PluginInfo and PluginConfig injection.
 * <p>
 * This plugin receives a scoped ServiceRegistry that provides:
 * <ul>
 *   <li>Access to global services (GreetingService, EventBus, etc.)</li>
 *   <li>Automatic PluginInfo for this plugin's metadata</li>
 *   <li>Automatic PluginConfig for this plugin's configuration</li>
 * </ul>
 * <p>
 * No more need to use getWrapper().getPluginId() or ConfigService.forPlugin()!
 */
public class GreetingPlugin extends Plugin implements ServiceRegistryAware {

    private ServiceRegistry serviceRegistry;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void start() {
        log.info("Greeting Plugin started!");

        // Access PluginInfo - automatically injected for this plugin
        PluginInfo pluginInfo = serviceRegistry.require(PluginInfo.class);
        log.info("  Plugin: id={}, version={}", pluginInfo.getPluginId(), pluginInfo.getVersion());

        // Access global service via ServiceRegistry
        GreetingService greetingService = serviceRegistry.require(GreetingService.class);
        log.info("  Got GreetingService from registry: {}", greetingService.greet("Greeting Plugin"));

        // Access PluginConfig - automatically scoped to this plugin!
        // No more: ConfigService.forPlugin(getWrapper().getPluginId())
        PluginConfig config = serviceRegistry.require(PluginConfig.class);

        // Read start count and increment it
        int startCount = config.getInt("start.count", 0);
        startCount++;
        config.setInt("start.count", startCount);

        // Read/write greeting prefix
        String prefix = config.get("greeting.prefix", "Hello");
        log.info("  Config: start.count={}, greeting.prefix={}", startCount, prefix);

        // Show all config keys
        log.info("  Config keys: {}", config.keys());
    }

    @Override
    public void stop() {
        log.info("Greeting Plugin stopped!");
    }

}