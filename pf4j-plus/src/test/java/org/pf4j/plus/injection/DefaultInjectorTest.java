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
package org.pf4j.plus.injection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class DefaultInjectorTest {

    private ServiceRegistry registry;
    private Injector injector;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        injector = new DefaultInjector(registry);
    }

    @AfterEach
    public void tearDown() {
        registry = null;
        injector = null;
    }

    /**
     * Test successful injection of a single field.
     */
    @Test
    public void testInjectSingleField() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        TargetWithSingleField target = new TargetWithSingleField();
        injector.inject(target);

        assertNotNull(target.service);
        assertSame(service, target.service);
    }

    /**
     * Test injection of multiple fields.
     */
    @Test
    public void testInjectMultipleFields() {
        TestService testService = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, testService);
        registry.register(AnotherService.class, anotherService);

        TargetWithMultipleFields target = new TargetWithMultipleFields();
        injector.inject(target);

        assertNotNull(target.testService);
        assertNotNull(target.anotherService);
        assertSame(testService, target.testService);
        assertSame(anotherService, target.anotherService);
    }

    /**
     * Test injection into private fields.
     */
    @Test
    public void testInjectPrivateField() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        TargetWithPrivateField target = new TargetWithPrivateField();
        injector.inject(target);

        assertNotNull(target.getService());
        assertSame(service, target.getService());
    }

    /**
     * Test injection failure when service is not registered.
     */
    @Test
    public void testInjectFailsWhenServiceNotFound() {
        TargetWithSingleField target = new TargetWithSingleField();

        InjectionException exception = assertThrows(InjectionException.class, () -> injector.inject(target));
        assertTrue(exception.getMessage().contains("TestService"));
        assertTrue(exception.getMessage().contains("service"));
    }

    /**
     * Test injection with null target.
     */
    @Test
    public void testInjectWithNullTarget() {
        assertThrows(IllegalArgumentException.class, () -> injector.inject(null));
    }

    /**
     * Test injection with target having no @Inject fields.
     */
    @Test
    public void testInjectWithNoInjectFields() {
        TargetWithoutInject target = new TargetWithoutInject();

        // Should not throw, just do nothing
        assertDoesNotThrow(() -> injector.inject(target));
        assertNull(target.service);
    }

    /**
     * Test creating injector with null registry.
     */
    @Test
    public void testCreateInjectorWithNullRegistry() {
        assertThrows(NullPointerException.class, () -> new DefaultInjector(null));
    }

    /**
     * Test optional injection when service is not registered.
     */
    @Test
    public void testOptionalInjectionWhenServiceNotFound() {
        TargetWithOptionalField target = new TargetWithOptionalField();

        // Should not throw, field remains null
        assertDoesNotThrow(() -> injector.inject(target));
        assertNull(target.service);
    }

    /**
     * Test optional injection when service is registered.
     */
    @Test
    public void testOptionalInjectionWhenServiceFound() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        TargetWithOptionalField target = new TargetWithOptionalField();
        injector.inject(target);

        assertNotNull(target.service);
        assertSame(service, target.service);
    }

    /**
     * Test mixed required and optional fields.
     */
    @Test
    public void testMixedRequiredAndOptionalFields() {
        TestService testService = new TestServiceImpl();
        registry.register(TestService.class, testService);
        // AnotherService is NOT registered

        TargetWithMixedFields target = new TargetWithMixedFields();
        injector.inject(target);

        // Required field should be injected
        assertNotNull(target.requiredService);
        assertSame(testService, target.requiredService);

        // Optional field should remain null
        assertNull(target.optionalService);
    }

    /**
     * Test that required field still throws when service not found.
     */
    @Test
    public void testRequiredFieldStillThrowsWhenServiceNotFound() {
        // AnotherService registered, but TestService is NOT
        registry.register(AnotherService.class, new AnotherServiceImpl());

        TargetWithMixedFields target = new TargetWithMixedFields();

        InjectionException exception = assertThrows(InjectionException.class, () -> injector.inject(target));
        assertTrue(exception.getMessage().contains("TestService"));
    }

    // Test classes

    static class TargetWithSingleField {
        @Inject
        TestService service;
    }

    static class TargetWithMultipleFields {
        @Inject
        TestService testService;

        @Inject
        AnotherService anotherService;
    }

    static class TargetWithPrivateField {
        @Inject
        private TestService service;

        public TestService getService() {
            return service;
        }
    }

    static class TargetWithoutInject {
        TestService service;
    }

    static class TargetWithOptionalField {
        @Inject(required = false)
        TestService service;
    }

    static class TargetWithMixedFields {
        @Inject
        TestService requiredService;

        @Inject(required = false)
        AnotherService optionalService;
    }

    // Test services

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
