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
 * A provider of service instances.
 * <p>
 * This functional interface allows for lazy instantiation of services.
 * The service instance is created when {@link #get()} is called, rather than
 * at registration time.
 * <p>
 * This is useful when:
 * <ul>
 *   <li>Service creation is expensive and should be deferred</li>
 *   <li>Service depends on context that is only available later</li>
 *   <li>Service instance should be created fresh on each request</li>
 * </ul>
 *
 * @param <T> the type of service this provider creates
 * @author Decebal Suiu
 */
@FunctionalInterface
public interface ServiceProvider<T> {

    /**
     * Returns an instance of the service.
     * <p>
     * Implementations may return the same instance on each call (singleton behavior)
     * or create a new instance each time (prototype behavior).
     *
     * @return a service instance
     */
    T get();

}