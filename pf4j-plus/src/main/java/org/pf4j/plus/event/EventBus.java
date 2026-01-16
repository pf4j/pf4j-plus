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
package org.pf4j.plus.event;

import java.util.function.Consumer;

/**
 * Event bus for decoupled communication between plugins and extensions.
 * <p>
 * The EventBus provides a publish-subscribe mechanism where components can
 * publish events and subscribe to events of specific types.
 * <p>
 * This is for notification, not coordination. Events are facts that have occurred,
 * not commands or workflow steps.
 * <p>
 * Threading behavior is explicit - by default events are delivered synchronously
 * on the publisher's thread. Asynchronous delivery requires explicit configuration.
 *
 * @author Decebal Suiu
 */
public interface EventBus {

    /**
     * Subscribes a listener to events of the specified type.
     * <p>
     * The same listener can be registered multiple times - each registration
     * will result in the listener being called once per event.
     *
     * @param eventType the type of events to listen for
     * @param listener the consumer to be called when events are published
     * @param <T> the event type
     * @throws IllegalArgumentException if eventType or listener is null
     */
    <T> void subscribe(Class<T> eventType, Consumer<T> listener);

    /**
     * Unsubscribes a listener from events of the specified type.
     * <p>
     * If the listener was registered multiple times, this removes only one registration.
     * If the listener is not registered, this method has no effect.
     *
     * @param eventType the type of events to stop listening for
     * @param listener the consumer to unregister
     * @param <T> the event type
     * @throws IllegalArgumentException if eventType or listener is null
     */
    <T> void unsubscribe(Class<T> eventType, Consumer<T> listener);

    /**
     * Publishes an event to all registered listeners of its type.
     * <p>
     * Only listeners registered for the exact event type will receive the event.
     * Subclass relationships are not considered.
     * <p>
     * By default, listeners are called synchronously on the publisher's thread.
     * If an executor was provided, listeners are called on the executor's threads.
     *
     * @param event the event to publish
     * @param <T> the event type
     * @throws IllegalArgumentException if event is null
     */
    <T> void publish(T event);

}
