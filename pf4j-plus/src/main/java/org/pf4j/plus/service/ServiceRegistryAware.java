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

/**
 * Interface to be implemented by plugins or extensions that need access to the {@link ServiceRegistry}.
 * <p>
 * Classes implementing this interface will receive the ServiceRegistry instance
 * during their initialization lifecycle, typically via a plugin or extension factory.
 * <p>
 * This provides an alternative to field injection via {@link org.pf4j.plus.injection.Inject}
 * when you want explicit lifecycle awareness.
 * <p>
 * Example usage:
 * <pre>
 * public class MyPlugin extends Plugin implements ServiceRegistryAware {
 *     private ServiceRegistry serviceRegistry;
 *
 *     {@literal @}Override
 *     public void setServiceRegistry(ServiceRegistry serviceRegistry) {
 *         this.serviceRegistry = serviceRegistry;
 *     }
 *
 *     {@literal @}Override
 *     public void start() {
 *         MyService service = serviceRegistry.require(MyService.class);
 *         service.doSomething();
 *     }
 * }
 * </pre>
 *
 * @author Decebal Suiu
 * @see ServiceRegistry
 */
public interface ServiceRegistryAware {

    /**
     * Sets the ServiceRegistry for this component.
     * <p>
     * This method is called by the platform during component initialization,
     * before the plugin or extension start method is invoked.
     *
     * @param serviceRegistry the service registry instance
     */
    void setServiceRegistry(ServiceRegistry serviceRegistry);

}
