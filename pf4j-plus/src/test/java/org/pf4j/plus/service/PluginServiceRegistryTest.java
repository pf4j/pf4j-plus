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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PluginServiceRegistry}.
 *
 * @author Decebal Suiu
 */
public class PluginServiceRegistryTest {

    private ServiceRegistry parentRegistry;
    private PluginServiceRegistry pluginRegistry;

    @BeforeEach
    public void setUp() {
        parentRegistry = new DefaultServiceRegistry();
        pluginRegistry = new PluginServiceRegistry(parentRegistry);
    }

    /**
     * Test that services registered locally are found.
     */
    @Test
    public void testLocalServiceIsFound() {
        TestService service = new TestServiceImpl("local");
        pluginRegistry.register(TestService.class, service);

        Optional<TestService> result = pluginRegistry.get(TestService.class);

        assertTrue(result.isPresent());
        assertSame(service, result.get());
    }

    /**
     * Test that services from parent are found when not registered locally.
     */
    @Test
    public void testDelegatesToParentWhenNotFoundLocally() {
        TestService parentService = new TestServiceImpl("parent");
        parentRegistry.register(TestService.class, parentService);

        Optional<TestService> result = pluginRegistry.get(TestService.class);

        assertTrue(result.isPresent());
        assertSame(parentService, result.get());
    }

    /**
     * Test that local services shadow parent services (same type).
     */
    @Test
    public void testLocalServiceShadowsParent() {
        TestService parentService = new TestServiceImpl("parent");
        TestService localService = new TestServiceImpl("local");

        parentRegistry.register(TestService.class, parentService);
        pluginRegistry.register(TestService.class, localService);

        Optional<TestService> result = pluginRegistry.get(TestService.class);

        assertTrue(result.isPresent());
        assertSame(localService, result.get());
        assertEquals("local", result.get().getName());
    }

    /**
     * Test that parent service is still accessible directly after shadow.
     */
    @Test
    public void testParentServiceUnaffectedByShadow() {
        TestService parentService = new TestServiceImpl("parent");
        TestService localService = new TestServiceImpl("local");

        parentRegistry.register(TestService.class, parentService);
        pluginRegistry.register(TestService.class, localService);

        // Plugin sees local
        assertEquals("local", pluginRegistry.get(TestService.class).get().getName());

        // Parent still has original
        assertEquals("parent", parentRegistry.get(TestService.class).get().getName());
    }

    /**
     * Test that different service types work independently.
     */
    @Test
    public void testDifferentServiceTypesWorkIndependently() {
        TestService testService = new TestServiceImpl("test");
        AnotherService anotherService = new AnotherServiceImpl();

        parentRegistry.register(TestService.class, testService);
        pluginRegistry.register(AnotherService.class, anotherService);

        // TestService from parent
        assertTrue(pluginRegistry.get(TestService.class).isPresent());
        assertSame(testService, pluginRegistry.get(TestService.class).get());

        // AnotherService from local
        assertTrue(pluginRegistry.get(AnotherService.class).isPresent());
        assertSame(anotherService, pluginRegistry.get(AnotherService.class).get());

        // Parent doesn't have AnotherService
        assertFalse(parentRegistry.get(AnotherService.class).isPresent());
    }

    /**
     * Test that ServiceProvider works with lazy loading.
     */
    @Test
    public void testServiceProviderLazyLoading() {
        int[] callCount = {0};
        ServiceProvider<TestService> provider = () -> {
            callCount[0]++;
            return new TestServiceImpl("lazy");
        };

        pluginRegistry.register(TestService.class, provider);

        // Provider not called yet
        assertEquals(0, callCount[0]);

        // First get triggers provider
        Optional<TestService> result1 = pluginRegistry.get(TestService.class);
        assertTrue(result1.isPresent());
        assertEquals(1, callCount[0]);

        // Second get uses cached value
        Optional<TestService> result2 = pluginRegistry.get(TestService.class);
        assertTrue(result2.isPresent());
        assertEquals(1, callCount[0]); // Still 1, not 2

        // Same instance
        assertSame(result1.get(), result2.get());
    }

    /**
     * Test that local provider shadows parent service.
     */
    @Test
    public void testLocalProviderShadowsParentService() {
        TestService parentService = new TestServiceImpl("parent");
        parentRegistry.register(TestService.class, parentService);

        ServiceProvider<TestService> localProvider = () -> new TestServiceImpl("lazy-local");
        pluginRegistry.register(TestService.class, localProvider);

        Optional<TestService> result = pluginRegistry.get(TestService.class);
        assertTrue(result.isPresent());
        assertEquals("lazy-local", result.get().getName());
    }

    /**
     * Test constructor with null parent throws exception.
     */
    @Test
    public void testConstructorWithNullParentThrows() {
        assertThrows(NullPointerException.class, () -> new PluginServiceRegistry(null));
    }

    /**
     * Test that empty result is returned when service not found anywhere.
     */
    @Test
    public void testEmptyWhenServiceNotFound() {
        Optional<TestService> result = pluginRegistry.get(TestService.class);
        assertFalse(result.isPresent());
    }

    // Test interfaces and implementations

    interface TestService {
        String getName();
    }

    static class TestServiceImpl implements TestService {
        private final String name;

        public TestServiceImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
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