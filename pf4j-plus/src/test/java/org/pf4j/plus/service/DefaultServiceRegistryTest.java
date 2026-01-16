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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class DefaultServiceRegistryTest {

    private ServiceRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
    }

    @AfterEach
    public void tearDown() {
        registry = null;
    }

    /**
     * Test of register and get methods, happy path.
     */
    @Test
    public void testRegisterAndGet() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        Optional<TestService> retrieved = registry.get(TestService.class);
        assertTrue(retrieved.isPresent());
        assertSame(service, retrieved.get());
    }

    /**
     * Test of get method when service is not registered.
     */
    @Test
    public void testGetNonExistentService() {
        Optional<TestService> retrieved = registry.get(TestService.class);
        assertFalse(retrieved.isPresent());
        assertTrue(retrieved.isEmpty());
    }

    /**
     * Test of register method with null type.
     */
    @Test
    public void testRegisterWithNullType() {
        TestService service = new TestServiceImpl();
        assertThrows(IllegalArgumentException.class, () -> registry.register(null, service));
    }

    /**
     * Test of register method with null service.
     */
    @Test
    public void testRegisterWithNullService() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(TestService.class, (TestService) null));
    }

    /**
     * Test of get method with null type.
     */
    @Test
    public void testGetWithNullType() {
        assertThrows(IllegalArgumentException.class, () -> registry.get(null));
    }

    /**
     * Test of register method when replacing existing service.
     */
    @Test
    public void testRegisterReplacesExistingService() {
        TestService firstService = new TestServiceImpl("first");
        TestService secondService = new TestServiceImpl("second");

        registry.register(TestService.class, firstService);
        registry.register(TestService.class, secondService);

        Optional<TestService> retrieved = registry.get(TestService.class);
        assertTrue(retrieved.isPresent());
        assertSame(secondService, retrieved.get());
        assertNotSame(firstService, retrieved.get());
    }

    /**
     * Test using orElseThrow with ServiceNotFoundException.
     */
    @Test
    public void testOrElseThrowWithServiceNotFoundException() {
        ServiceNotFoundException exception = assertThrows(
            ServiceNotFoundException.class,
            () -> registry.get(TestService.class).orElseThrow(() -> new ServiceNotFoundException(TestService.class))
        );

        assertEquals(TestService.class, exception.getServiceType());
        assertTrue(exception.getMessage().contains("TestService"));
    }

    /**
     * Test registering multiple different services.
     */
    @Test
    public void testRegisterMultipleServices() {
        TestService testService = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, testService);
        registry.register(AnotherService.class, anotherService);

        Optional<TestService> retrievedTest = registry.get(TestService.class);
        Optional<AnotherService> retrievedAnother = registry.get(AnotherService.class);

        assertTrue(retrievedTest.isPresent());
        assertTrue(retrievedAnother.isPresent());
        assertSame(testService, retrievedTest.get());
        assertSame(anotherService, retrievedAnother.get());
    }

    // ServiceProvider tests

    /**
     * Test registering and retrieving a service via provider (lazy loading).
     */
    @Test
    public void testRegisterWithProvider() {
        ServiceProvider<TestService> provider = () -> new TestServiceImpl("from-provider");
        registry.register(TestService.class, provider);

        Optional<TestService> retrieved = registry.get(TestService.class);
        assertTrue(retrieved.isPresent());
        assertEquals("from-provider", retrieved.get().getName());
    }

    /**
     * Test that provider is called lazily (only on first get).
     */
    @Test
    public void testProviderIsCalledLazily() {
        int[] callCount = {0};
        ServiceProvider<TestService> provider = () -> {
            callCount[0]++;
            return new TestServiceImpl("lazy");
        };

        registry.register(TestService.class, provider);
        assertEquals(0, callCount[0]); // Not called yet

        registry.get(TestService.class);
        assertEquals(1, callCount[0]); // Called once

        registry.get(TestService.class);
        assertEquals(1, callCount[0]); // Still once (cached)
    }

    /**
     * Test that provider result is cached.
     */
    @Test
    public void testProviderResultIsCached() {
        ServiceProvider<TestService> provider = () -> new TestServiceImpl("cached");
        registry.register(TestService.class, provider);

        Optional<TestService> first = registry.get(TestService.class);
        Optional<TestService> second = registry.get(TestService.class);

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertSame(first.get(), second.get()); // Same instance
    }

    /**
     * Test that registering instance replaces provider.
     */
    @Test
    public void testInstanceReplacesProvider() {
        ServiceProvider<TestService> provider = () -> new TestServiceImpl("provider");
        TestService instance = new TestServiceImpl("instance");

        registry.register(TestService.class, provider);
        registry.register(TestService.class, instance);

        Optional<TestService> retrieved = registry.get(TestService.class);
        assertTrue(retrieved.isPresent());
        assertEquals("instance", retrieved.get().getName());
    }

    /**
     * Test that registering provider replaces instance.
     */
    @Test
    public void testProviderReplacesInstance() {
        TestService instance = new TestServiceImpl("instance");
        ServiceProvider<TestService> provider = () -> new TestServiceImpl("provider");

        registry.register(TestService.class, instance);
        registry.register(TestService.class, provider);

        Optional<TestService> retrieved = registry.get(TestService.class);
        assertTrue(retrieved.isPresent());
        assertEquals("provider", retrieved.get().getName());
    }

    /**
     * Test registering provider with null type throws exception.
     */
    @Test
    public void testRegisterProviderWithNullType() {
        ServiceProvider<TestService> provider = () -> new TestServiceImpl();
        assertThrows(IllegalArgumentException.class,
            () -> registry.register((Class<TestService>) null, provider));
    }

    /**
     * Test registering null provider throws exception.
     */
    @Test
    public void testRegisterNullProvider() {
        assertThrows(IllegalArgumentException.class,
            () -> registry.register(TestService.class, (ServiceProvider<TestService>) null));
    }

    // Test interfaces and implementations

    interface TestService {
        String getName();
    }

    static class TestServiceImpl implements TestService {
        private final String name;

        public TestServiceImpl() {
            this("default");
        }

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
