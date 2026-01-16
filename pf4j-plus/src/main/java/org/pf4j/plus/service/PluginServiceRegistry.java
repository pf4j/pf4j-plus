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

import java.util.Objects;
import java.util.Optional;

/**
 * A scoped {@link ServiceRegistry} for plugins.
 * <p>
 * This registry maintains plugin-specific services while delegating to a parent
 * registry for shared/global services. This allows plugins to:
 * <ul>
 *   <li>Access global services registered by the host application</li>
 *   <li>Register plugin-local services without affecting the global registry</li>
 *   <li>Shadow global services with plugin-specific implementations</li>
 * </ul>
 * <p>
 * Service lookup order:
 * <ol>
 *   <li>Local services (registered in this plugin's registry)</li>
 *   <li>Parent services (global/application registry)</li>
 * </ol>
 * <p>
 * Registration always happens locally - plugins cannot modify the global registry.
 *
 * @author Decebal Suiu
 */
public class PluginServiceRegistry extends DefaultServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginServiceRegistry.class);

    private final ServiceRegistry parent;

    /**
     * Creates a new PluginServiceRegistry with the specified parent.
     *
     * @param parent the parent registry (typically the application's global registry)
     * @throws NullPointerException if parent is null
     */
    public PluginServiceRegistry(ServiceRegistry parent) {
        this.parent = Objects.requireNonNull(parent, "Parent registry cannot be null");
    }

    @Override
    public <T> Optional<T> get(Class<T> type) {
        // First check local (this registry)
        Optional<T> result = super.get(type);
        if (result.isPresent()) {
            return result;
        }

        // Delegate to parent
        log.trace("Service '{}' not found locally, delegating to parent", type.getName());
        return parent.get(type);
    }

}