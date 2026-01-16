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
import org.pf4j.plus.injection.InjectionException;
import org.pf4j.plus.injection.Injector;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class InjectingExtensionFactoryTest {

    private ServiceRegistry registry;
    private Injector injector;
    private ExtensionFactory factory;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        injector = new DefaultInjector(registry);
        factory = new InjectingExtensionFactory(new DefaultExtensionFactory(), injector);
    }

    /**
     * Test that extensions with @Inject fields are injected.
     */
    @Test
    public void testExtensionWithInjectFieldsIsInjected() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        InjectableExtension extension = factory.create(InjectableExtension.class);

        assertNotNull(extension);
        assertNotNull(extension.getService());
        assertSame(service, extension.getService());
    }

    /**
     * Test that extensions without @Inject fields are created normally.
     */
    @Test
    public void testExtensionWithoutInjectFieldsIsCreatedNormally() {
        SimpleExtension extension = factory.create(SimpleExtension.class);

        assertNotNull(extension);
        assertTrue(extension.wasCreated());
    }

    /**
     * Test that multiple fields are injected.
     */
    @Test
    public void testExtensionWithMultipleInjectFields() {
        TestService testService = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, testService);
        registry.register(AnotherService.class, anotherService);

        MultiInjectExtension extension = factory.create(MultiInjectExtension.class);

        assertNotNull(extension);
        assertNotNull(extension.getTestService());
        assertNotNull(extension.getAnotherService());
        assertSame(testService, extension.getTestService());
        assertSame(anotherService, extension.getAnotherService());
    }

    /**
     * Test that injection fails when required service is not registered.
     */
    @Test
    public void testInjectionFailsWhenServiceNotFound() {
        assertThrows(InjectionException.class, () -> factory.create(InjectableExtension.class));
    }

    /**
     * Test creating factory with null delegate.
     */
    @Test
    public void testCreateWithNullDelegate() {
        assertThrows(NullPointerException.class,
            () -> new InjectingExtensionFactory(null, injector));
    }

    /**
     * Test creating factory with null injector.
     */
    @Test
    public void testCreateWithNullInjector() {
        assertThrows(NullPointerException.class,
            () -> new InjectingExtensionFactory(new DefaultExtensionFactory(), null));
    }

    // Test extension classes

    public static class InjectableExtension {
        @Inject
        private TestService service;

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

    public static class MultiInjectExtension {
        @Inject
        private TestService testService;

        @Inject
        private AnotherService anotherService;

        public TestService getTestService() {
            return testService;
        }

        public AnotherService getAnotherService() {
            return anotherService;
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
