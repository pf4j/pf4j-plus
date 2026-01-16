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

import org.pf4j.plus.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Default implementation of {@link Injector}.
 * <p>
 * This implementation scans all declared fields of the target object
 * and injects services from the {@link ServiceRegistry} into fields
 * annotated with {@link Inject}.
 * <p>
 * Injection is performed using reflection and field.setAccessible(true),
 * so it works with private fields as well.
 *
 * @author Decebal Suiu
 */
public class DefaultInjector implements Injector {

    private static final Logger log = LoggerFactory.getLogger(DefaultInjector.class);

    private final ServiceRegistry serviceRegistry;

    public DefaultInjector(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "ServiceRegistry cannot be null");
    }

    @Override
    public void inject(Object target) {
        if (target == null) {
            throw new IllegalArgumentException("Target object cannot be null");
        }

        Class<?> targetClass = target.getClass();
        log.debug("Injecting dependencies into '{}'", targetClass.getName());

        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                injectField(target, field);
            }
        }
    }

    private void injectField(Object target, Field field) {
        Class<?> fieldType = field.getType();
        Inject inject = field.getAnnotation(Inject.class);
        boolean required = inject.required();

        log.trace("Injecting field '{}' of type '{}' (required={})", field.getName(), fieldType.getName(), required);

        Object service = serviceRegistry.get(fieldType).orElse(null);

        if (service == null) {
            if (required) {
                throw new InjectionException(
                    "No service registered for type '" + fieldType.getName() + "' required by field '" + field.getName() + "'"
                );
            }
            log.trace("Skipping optional field '{}' - service not found", field.getName());
            return;
        }

        try {
            field.setAccessible(true);
            field.set(target, service);
            log.trace("Successfully injected field '{}'", field.getName());
        } catch (IllegalAccessException e) {
            throw new InjectionException("Failed to inject field '" + field.getName() + "' of type '" + fieldType.getName() + "'", e);
        }
    }

}
