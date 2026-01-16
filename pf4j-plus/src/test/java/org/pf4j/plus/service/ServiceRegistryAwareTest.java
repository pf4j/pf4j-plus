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
package org.pf4j.plus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class ServiceRegistryAwareTest {

    private ServiceRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
    }

    /**
     * Test that ServiceRegistryAware components receive the registry.
     */
    @Test
    public void testServiceRegistryAwareReceivesRegistry() {
        TestComponent component = new TestComponent();
        component.setServiceRegistry(registry);

        assertNotNull(component.getServiceRegistry());
        assertSame(registry, component.getServiceRegistry());
    }

    /**
     * Test that ServiceRegistryAware component can access services.
     */
    @Test
    public void testServiceRegistryAwareCanAccessServices() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        TestComponent component = new TestComponent();
        component.setServiceRegistry(registry);
        component.initialize();

        assertTrue(component.isInitialized());
        assertSame(service, component.getService());
    }

    /**
     * Test multiple components can receive the same registry.
     */
    @Test
    public void testMultipleComponentsReceiveSameRegistry() {
        TestComponent component1 = new TestComponent();
        TestComponent component2 = new TestComponent();

        component1.setServiceRegistry(registry);
        component2.setServiceRegistry(registry);

        assertSame(component1.getServiceRegistry(), component2.getServiceRegistry());
    }

    // Test classes

    static class TestComponent implements ServiceRegistryAware {
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
