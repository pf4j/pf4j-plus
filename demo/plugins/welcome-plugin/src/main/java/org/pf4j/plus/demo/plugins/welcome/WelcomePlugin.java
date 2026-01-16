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
package org.pf4j.plus.demo.plugins.welcome;

import org.pf4j.Plugin;
import org.pf4j.plus.demo.app.GreetingService;
import org.pf4j.plus.injection.Inject;

/**
 * Welcome plugin demonstrating @Inject on the plugin itself.
 * The GreetingService is injected automatically via @Inject annotation.
 */
public class WelcomePlugin extends Plugin {

    @Inject
    private GreetingService greetingService;

    @Override
    public void start() {
        log.info("Welcome Plugin started!");

        // Use injected service
        if (greetingService != null) {
            log.info("  Got GreetingService via @Inject: {}", greetingService.greet("Welcome Plugin"));
        }
    }

    @Override
    public void stop() {
        log.info("Welcome Plugin stopped!");
    }

}
