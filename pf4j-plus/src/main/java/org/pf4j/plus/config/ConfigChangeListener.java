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
package org.pf4j.plus.config;

/**
 * Listener for configuration changes.
 * <p>
 * Host can register this to take control of persistence strategy
 * (batching, debouncing, etc.) instead of default auto-save behavior.
 *
 * @author Decebal Suiu
 */
@FunctionalInterface
public interface ConfigChangeListener {

    /**
     * Called when a configuration value changes.
     *
     * @param event the change event
     */
    void onConfigChanged(ConfigChangeEvent event);

}