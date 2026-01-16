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
import org.pf4j.plus.injection.InjectionException;
import org.pf4j.plus.injection.Injector;
import org.pf4j.plus.service.DefaultServiceRegistry;
import org.pf4j.plus.service.ServiceRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Decebal Suiu
 */
public class InjectingPluginFactoryTest {

    private ServiceRegistry registry;
    private Injector injector;
    private PluginFactory factory;
    private PluginWrapper pluginWrapper;

    @BeforeEach
    public void setUp() {
        registry = new DefaultServiceRegistry();
        injector = new DefaultInjector(registry);
        factory = new InjectingPluginFactory(new DefaultPluginFactory(), injector);
        pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn("test-plugin");
    }

    /**
     * Test that plugins with @Inject fields are injected.
     */
    @Test
    public void testPluginWithInjectFieldsIsInjected() {
        TestService service = new TestServiceImpl();
        registry.register(TestService.class, service);

        InjectablePlugin injectablePlugin = new InjectablePlugin(pluginWrapper);
        injector.inject(injectablePlugin);

        assertNotNull(injectablePlugin.getService());
        assertSame(service, injectablePlugin.getService());
    }

    /**
     * Test that plugins without @Inject fields are not affected.
     */
    @Test
    public void testPluginWithoutInjectFieldsIsNotAffected() {
        SimplePlugin simplePlugin = new SimplePlugin(pluginWrapper);
        injector.inject(simplePlugin);

        assertNotNull(simplePlugin);
        assertTrue(simplePlugin.wasCreated());
    }

    /**
     * Test that multiple fields are injected.
     */
    @Test
    public void testPluginWithMultipleInjectFields() {
        TestService testService = new TestServiceImpl();
        AnotherService anotherService = new AnotherServiceImpl();

        registry.register(TestService.class, testService);
        registry.register(AnotherService.class, anotherService);

        MultiInjectPlugin multiInjectPlugin = new MultiInjectPlugin(pluginWrapper);
        injector.inject(multiInjectPlugin);

        assertNotNull(multiInjectPlugin.getTestService());
        assertNotNull(multiInjectPlugin.getAnotherService());
        assertSame(testService, multiInjectPlugin.getTestService());
        assertSame(anotherService, multiInjectPlugin.getAnotherService());
    }

    /**
     * Test that injection fails when required service is not registered.
     */
    @Test
    public void testInjectionFailsWhenServiceNotFound() {
        InjectablePlugin injectablePlugin = new InjectablePlugin(pluginWrapper);

        assertThrows(InjectionException.class, () -> injector.inject(injectablePlugin));
    }

    /**
     * Test creating factory with null delegate.
     */
    @Test
    public void testCreateWithNullDelegate() {
        assertThrows(NullPointerException.class,
            () -> new InjectingPluginFactory(null, injector));
    }

    /**
     * Test creating factory with null injector.
     */
    @Test
    public void testCreateWithNullInjector() {
        assertThrows(NullPointerException.class,
            () -> new InjectingPluginFactory(new DefaultPluginFactory(), null));
    }

    // Test plugin classes

    public static class InjectablePlugin extends Plugin {
        @Inject
        private TestService service;

        public InjectablePlugin(PluginWrapper wrapper) {
            super(wrapper);
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

    public static class MultiInjectPlugin extends Plugin {
        @Inject
        private TestService testService;

        @Inject
        private AnotherService anotherService;

        public MultiInjectPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }

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
