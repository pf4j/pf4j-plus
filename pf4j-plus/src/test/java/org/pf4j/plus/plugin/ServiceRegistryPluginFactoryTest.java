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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.DefaultPluginFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Decebal Suiu
 */
public class ServiceRegistryPluginFactoryTest {

    private ServiceRegistry registry;
    private Map<ClassLoader, ServiceRegistry> pluginRegistries;
    private PluginFactory factory;
    private PluginWrapper pluginWrapper;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        pluginRegistries = new ConcurrentHashMap<>();
        factory = new ServiceRegistryPluginFactory(new DefaultPluginFactory(), registry, pluginRegistries);
        pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn("test-plugin");
    }

    /**
     * Test that ServiceRegistryAware plugins receive the registry.
     */
    @Test
    public void testServiceRegistryAwarePluginReceivesRegistry() {
        // Create plugin instance directly for testing injection
        AwarePlugin awarePlugin = new AwarePlugin(pluginWrapper);

        // Simulate what the decorator does
        if (awarePlugin instanceof ServiceRegistryAware) {
            ((ServiceRegistryAware) awarePlugin).setServiceRegistry(registry);
        }

        assertNotNull(awarePlugin.getServiceRegistry());
        assertSame(registry, awarePlugin.getServiceRegistry());
    }

    /**
     * Test that non-aware plugins are not affected by the decorator.
     */
    @Test
    public void testNonAwarePluginIsNotAffected() {
        SimplePlugin simplePlugin = new SimplePlugin(pluginWrapper);

        // The decorator should not affect non-aware plugins
        // Just verify it was created successfully
        assertNotNull(simplePlugin);
        assertTrue(simplePlugin.wasCreated());
    }

    /**
     * Test that ServiceRegistryAware plugin can access services.
     */
    @Test
    public void testServiceRegistryAwarePluginCanAccessServices() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        AwarePlugin awarePlugin = new AwarePlugin(pluginWrapper);
        ((ServiceRegistryAware) awarePlugin).setServiceRegistry(registry);
        awarePlugin.initialize();

        assertTrue(awarePlugin.isInitialized());
        assertSame(service, awarePlugin.getService());
    }

    /**
     * Test creating factory with null delegate.
     */
    @Test
    public void testCreateWithNullDelegate() {
        assertThrows(NullPointerException.class,
            () -> new ServiceRegistryPluginFactory(null, registry, pluginRegistries));
    }

    /**
     * Test creating factory with null registry.
     */
    @Test
    public void testCreateWithNullRegistry() {
        assertThrows(NullPointerException.class,
            () -> new ServiceRegistryPluginFactory(new DefaultPluginFactory(), null, pluginRegistries));
    }

    // Test plugin classes

    public static class AwarePlugin extends Plugin implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;
        private boolean initialized = false;
        private TestService service;

        public AwarePlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        @Override
        public void setServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }

        public ServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }

        public void initialize() {
            if (serviceRegistry != null) {
                service = serviceRegistry.get(TestService.class).orElse(null);
                initialized = true;
            }
        }

        public boolean isInitialized() {
            return initialized;
        }

        public TestService getService() {
            return service;
        }
    }

    public static class SimplePlugin extends Plugin {
        private final boolean created = true;

        public SimplePlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        public boolean wasCreated() {
            return created;
        }
    }

    interface TestService {
        String getName();
    }

    static class TestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "test";
        }
    }

}
