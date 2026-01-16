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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ServiceRegistry}.
 * <p>
 * This implementation is thread-safe and uses a {@link ConcurrentHashMap}
 * to store service instances.
 * <p>
 * Services are registered by type (interface or class) and can be retrieved
 * by the same type. Only one service instance per type is supported.
 *
 * @author Decebal Suiu
 */
public class DefaultServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    private final Map<Class<?>, Object> services;
    private final Map<Class<?>, ServiceProvider<?>> providers;

    public DefaultServiceRegistry() {
        this.services = new ConcurrentHashMap<>();
        this.providers = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void register(Class<T> type, T service) {
        if (type == null) {
            throw new IllegalArgumentException("Service type cannot be null");
        }
        if (service == null) {
            throw new IllegalArgumentException("Service instance cannot be null");
        }

        log.debug("Register service '{}' with instance '{}'", type.getName(), service.getClass().getName());
        providers.remove(type); // remove provider if exists
        services.put(type, service);
    }

    @Override
    public <T> void register(Class<T> type, ServiceProvider<T> provider) {
        if (type == null) {
            throw new IllegalArgumentException("Service type cannot be null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Service provider cannot be null");
        }

        log.debug("Register service provider for '{}'", type.getName());
        services.remove(type); // remove instance if exists
        providers.put(type, provider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Service type cannot be null");
        }

        // First check direct instances
        Object service = services.get(type);
        if (service != null) {
            log.trace("Found service '{}' with instance '{}'", type.getName(), service.getClass().getName());
            return Optional.of((T) service);
        }

        // Then check providers (lazy initialization)
        ServiceProvider<?> provider = providers.get(type);
        if (provider != null) {
            log.trace("Found provider for '{}', invoking", type.getName());
            service = provider.get();
            if (service != null) {
                // Cache the instance for future calls
                services.put(type, service);
                providers.remove(type);
                log.trace("Cached service '{}' with instance '{}'", type.getName(), service.getClass().getName());
                return Optional.of((T) service);
            }
        }

        log.trace("Service '{}' not found", type.getName());
        return Optional.empty();
    }

}
