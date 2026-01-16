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

/**
 * Performs dependency injection on objects using the {@link Inject} annotation.
 * <p>
 * The Injector scans objects for fields marked with {@link Inject} and populates them
 * with services from the ServiceRegistry.
 * <p>
 * This is simple field injection, not full dependency injection.
 * It does not provide scoping, lifecycle management, or circular dependency resolution.
 *
 * @author Decebal Suiu
 */
public interface Injector {

    /**
     * Injects dependencies into the target object.
     * <p>
     * Scans all fields of the target object for {@link Inject} annotations
     * and populates them with the corresponding services from the registry.
     *
     * @param target the object to inject dependencies into
     * @throws IllegalArgumentException if target is null
     * @throws InjectionException if injection fails (e.g., service not found, field access denied)
     */
    void inject(Object target);

}
