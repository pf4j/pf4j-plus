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

import java.util.Optional;

/**
 * Central registry for managing and accessing platform services.
 * <p>
 * The ServiceRegistry acts as a service locator, allowing plugins and extensions
 * to access shared platform services provided by the host application.
 * <p>
 * The host application owns and controls all service registrations.
 * Services are registered by type and accessed through their interface.
 * <p>
 * This is intentionally simple - it does not provide dependency resolution,
 * scoping, or lifecycle management. It is wiring, not dependency injection.
 *
 * @author Decebal Suiu
 */
public interface ServiceRegistry {

    /**
     * Registers a service instance by its type.
     * <p>
     * If a service of the same type is already registered, it will be replaced.
     *
     * @param type the service interface or class
     * @param service the service instance
     * @param <T> the service type
     * @throws IllegalArgumentException if type or service is null
     */
    <T> void register(Class<T> type, T service);

    /**
     * Registers a service provider by its type.
     * <p>
     * The provider is called lazily when the service is first requested via {@link #get(Class)}.
     * If a service of the same type is already registered, it will be replaced.
     *
     * @param type the service interface or class
     * @param provider the service provider
     * @param <T> the service type
     * @throws IllegalArgumentException if type or provider is null
     */
    <T> void register(Class<T> type, ServiceProvider<T> provider);

    /**
     * Retrieves a service by its type.
     *
     * @param type the service interface or class
     * @param <T> the service type
     * @return an Optional containing the service if registered, empty otherwise
     * @throws IllegalArgumentException if type is null
     */
    <T> Optional<T> get(Class<T> type);

    /**
     * Retrieves a required service by its type.
     * <p>
     * Use this method when your plugin genuinely requires the service to function.
     * If the service is optional, use {@link #get(Class)} instead.
     *
     * @param type the service interface or class
     * @param <T> the service type
     * @return the service instance
     * @throws IllegalArgumentException if type is null
     * @throws ServiceNotFoundException if the service is not registered
     */
    default <T> T require(Class<T> type) {
        return get(type).orElseThrow(() -> new ServiceNotFoundException(type));
    }

}
