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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field for dependency injection from the ServiceRegistry.
 * <p>
 * Fields annotated with {@code @Inject} will be automatically populated
 * with the corresponding service from the registry when {@link Injector#inject(Object)}
 * is called on the containing object.
 * <p>
 * By default, injection is required and will throw {@link InjectionException} if the
 * service is not found. Use {@code @Inject(required = false)} for optional dependencies
 * that should remain {@code null} when the service is not registered.
 * <p>
 * This is intentionally limited to field injection only.
 * Method and constructor injection are not supported to keep the implementation simple.
 *
 * @author Decebal Suiu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {

    /**
     * Whether the dependency is required.
     * <p>
     * If {@code true} (default), an {@link InjectionException} is thrown when
     * the service is not found in the registry.
     * <p>
     * If {@code false}, the field remains {@code null} when the service is not found.
     *
     * @return {@code true} if the dependency is required, {@code false} otherwise
     */
    boolean required() default true;

}
