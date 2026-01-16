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
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;
import org.pf4j.plus.service.ServiceRegistryAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class ServiceRegistryExtensionFactoryTest {

    private ServiceRegistry registry;
    private Map<ClassLoader, ServiceRegistry> pluginRegistries;
    private ExtensionFactory factory;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        pluginRegistries = new ConcurrentHashMap<>();
        factory = new ServiceRegistryExtensionFactory(new DefaultExtensionFactory(), registry, pluginRegistries);
    }

    /**
     * Test that ServiceRegistryAware extensions receive the registry.
     */
    @Test
    public void testServiceRegistryAwareExtensionReceivesRegistry() {
        AwareExtension extension = factory.create(AwareExtension.class);

        assertNotNull(extension);
        assertNotNull(extension.getServiceRegistry());
        assertSame(registry, extension.getServiceRegistry());
    }

    /**
     * Test that non-aware extensions are created normally.
     */
    @Test
    public void testNonAwareExtensionIsCreatedNormally() {
        SimpleExtension extension = factory.create(SimpleExtension.class);

        assertNotNull(extension);
        assertTrue(extension.wasCreated());
    }

    /**
     * Test that ServiceRegistryAware extension can access services.
     */
    @Test
    public void testServiceRegistryAwareExtensionCanAccessServices() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        AwareExtension extension = factory.create(AwareExtension.class);
        extension.initialize();

        assertTrue(extension.isInitialized());
        assertSame(service, extension.getService());
    }

    /**
     * Test creating factory with null delegate.
     */
    @Test
    public void testCreateWithNullDelegate() {
        assertThrows(NullPointerException.class,
            () -> new ServiceRegistryExtensionFactory(null, registry, pluginRegistries));
    }

    /**
     * Test creating factory with null registry.
     */
    @Test
    public void testCreateWithNullRegistry() {
        assertThrows(NullPointerException.class,
            () -> new ServiceRegistryExtensionFactory(new DefaultExtensionFactory(), null, pluginRegistries));
    }

    // Test extension classes

    public static class AwareExtension implements ServiceRegistryAware {
        private ServiceRegistry serviceRegistry;
        private boolean initialized = false;
        private TestService service;

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

    public static class SimpleExtension {
        private final boolean created = true;

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
