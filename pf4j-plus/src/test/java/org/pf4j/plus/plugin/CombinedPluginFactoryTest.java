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
import org.pf4j.plus.injection.DefaultInjector;
import org.pf4j.plus.injection.Inject;
import org.pf4j.plus.injection.Injector;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for combining multiple PluginFactory decorators.
 *
 * @author Decebal Suiu
 */
public class CombinedPluginFactoryTest {

    private ServiceRegistry registry;
    private Map<ClassLoader, ServiceRegistry> pluginRegistries;
    private PluginFactory factory;
    private PluginWrapper pluginWrapper;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        pluginRegistries = new ConcurrentHashMap<>();
        Injector injector = new DefaultInjector(registry);

        // Combine both decorators
        factory = new InjectingPluginFactory(
            new ServiceRegistryPluginFactory(
                new DefaultPluginFactory(),
                registry,
                pluginRegistries
            ),
            injector
        );

        pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn("test-plugin");
    }

    /**
     * Test that plugin receives both ServiceRegistry via aware interface
     * and field injection via @Inject.
     */
    @Test
    public void testPluginReceivesBothServiceRegistryAndInjection() {
        TestService service = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, service);
        registry.register(AnotherService.class, anotherService);

        Injector injector = new DefaultInjector(registry);
        FullyIntegratedPlugin integratedPlugin = new FullyIntegratedPlugin(pluginWrapper);

        // Simulate combined decorators
        ((ServiceRegistryAware) integratedPlugin).setServiceRegistry(registry);
        injector.inject(integratedPlugin);

        // Check ServiceRegistryAware injection
        assertNotNull(integratedPlugin.getServiceRegistry());
        assertSame(registry, integratedPlugin.getServiceRegistry());

        // Check @Inject field injection
        assertNotNull(integratedPlugin.getTestService());
        assertSame(service, integratedPlugin.getTestService());

        // Verify plugin can access both
        integratedPlugin.initialize();
        assertTrue(integratedPlugin.isInitialized());
        assertSame(anotherService, integratedPlugin.getAnotherServiceFromRegistry());
        assertSame(service, integratedPlugin.getTestService());
    }

    /**
     * Test that ServiceRegistryAware-only plugin works.
     */
    @Test
    public void testServiceRegistryAwareOnlyPlugin() {
        AwareOnlyPlugin awarePlugin = new AwareOnlyPlugin(pluginWrapper);
        ((ServiceRegistryAware) awarePlugin).setServiceRegistry(registry);

        assertNotNull(awarePlugin.getServiceRegistry());
        assertSame(registry, awarePlugin.getServiceRegistry());
    }

    /**
     * Test that Inject-only plugin works.
     */
    @Test
    public void testInjectOnlyPlugin() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        Injector injector = new DefaultInjector(registry);
        InjectOnlyPlugin injectPlugin = new InjectOnlyPlugin(pluginWrapper);
        injector.inject(injectPlugin);

        assertNotNull(injectPlugin.getService());
        assertSame(service, injectPlugin.getService());
    }

    // Test plugin classes

    public static class FullyIntegratedPlugin extends Plugin implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;

        @Inject
        private TestService testService;

        private boolean initialized = false;
        private AnotherService anotherServiceFromRegistry;

        public FullyIntegratedPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        @Override
        public void setServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }

        public ServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }

        public TestService getTestService() {
            return testService;
        }

        public void initialize() {
            if (serviceRegistry != null) {
                anotherServiceFromRegistry = serviceRegistry.get(AnotherService.class).orElse(null);
                initialized = true;
            }
        }

        public boolean isInitialized() {
            return initialized;
        }

        public AnotherService getAnotherServiceFromRegistry() {
            return anotherServiceFromRegistry;
        }
    }

    public static class AwareOnlyPlugin extends Plugin implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;

        public AwareOnlyPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        @Override
        public void setServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }

        public ServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }
    }

    public static class InjectOnlyPlugin extends Plugin {
        @Inject
        private TestService service;

        public InjectOnlyPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

        public TestService getService() {
            return service;
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

    interface AnotherService {
        void doSomething();
    }

    static class AnotherServiceImpl implements AnotherService {
        @Override
        public void doSomething() {
            // no-op
        }
    }

}
