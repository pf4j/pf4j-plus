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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.DefaultExtensionFactory;
import org.pf4j.ExtensionFactory;
import org.pf4j.plus.injection.DefaultInjector;
import org.pf4j.plus.injection.Inject;
import org.pf4j.plus.injection.Injector;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for combining multiple ExtensionFactory decorators.
 *
 * @author Decebal Suiu
 */
public class CombinedExtensionFactoryTest {

    private ServiceRegistry registry;
    private Map<ClassLoader, ServiceRegistry> pluginRegistries;
    private ExtensionFactory factory;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        pluginRegistries = new ConcurrentHashMap<>();
        Injector injector = new DefaultInjector(registry);

        // Combine both decorators
        factory = new InjectingExtensionFactory(
            new ServiceRegistryExtensionFactory(
                new DefaultExtensionFactory(),
                registry,
                pluginRegistries
            ),
            injector
        );
    }

    /**
     * Test that extension receives both ServiceRegistry via aware interface
     * and field injection via @Inject.
     */
    @Test
    public void testExtensionReceivesBothServiceRegistryAndInjection() {
        TestService service = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, service);
        registry.register(AnotherService.class, anotherService);

        FullyIntegratedExtension extension = factory.create(FullyIntegratedExtension.class);

        // Check ServiceRegistryAware injection
        assertNotNull(extension.getServiceRegistry());
        assertSame(registry, extension.getServiceRegistry());

        // Check @Inject field injection
        assertNotNull(extension.getTestService());
        assertSame(service, extension.getTestService());

        // Verify extension can access both
        extension.initialize();
        assertTrue(extension.isInitialized());
        assertSame(anotherService, extension.getAnotherServiceFromRegistry());
        assertSame(service, extension.getTestService());
    }

    /**
     * Test that ServiceRegistryAware-only extension works.
     */
    @Test
    public void testServiceRegistryAwareOnlyExtension() {
        AwareOnlyExtension extension = factory.create(AwareOnlyExtension.class);

        assertNotNull(extension.getServiceRegistry());
        assertSame(registry, extension.getServiceRegistry());
    }

    /**
     * Test that Inject-only extension works.
     */
    @Test
    public void testInjectOnlyExtension() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        InjectOnlyExtension extension = factory.create(InjectOnlyExtension.class);

        assertNotNull(extension.getService());
        assertSame(service, extension.getService());
    }

    // Test extension classes

    public static class FullyIntegratedExtension implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;

        @Inject
        private TestService testService;

        private boolean initialized = false;
        private AnotherService anotherServiceFromRegistry;

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

    public static class AwareOnlyExtension implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;

        @Override
        public void setServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistry = serviceRegistry;
        }

        public ServiceRegistry getServiceRegistry() {
            return serviceRegistry;
        }
    }

    public static class InjectOnlyExtension {
        @Inject
        private TestService service;

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
