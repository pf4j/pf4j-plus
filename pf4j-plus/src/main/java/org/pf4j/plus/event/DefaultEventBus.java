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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Default implementation of {@link EventBus}.
 * <p>
 * This implementation is thread-safe and uses:
 * <ul>
 *   <li>{@link ConcurrentHashMap} to store event type to listeners mappings</li>
 *   <li>{@link CopyOnWriteArrayList} to store listeners per event type</li>
 * </ul>
 * <p>
 * By default, events are delivered synchronously on the publisher's thread.
 * An {@link Executor} can be provided for asynchronous delivery.
 *
 * @author Decebal Suiu
 */
public class DefaultEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(DefaultEventBus.class);

    private final Map<Class<?>, List<Consumer<?>>> listeners;
    private final Executor executor;

    /**
     * Creates an EventBus with synchronous event delivery.
     * Events will be delivered on the publisher's thread.
     */
    public DefaultEventBus() {
        this(Runnable::run);
    }

    /**
     * Creates an EventBus with custom event delivery executor.
     * <p>
     * The executor controls threading behavior. For example:
     * <ul>
     *   <li>Synchronous: {@code Runnable::run}</li>
     *   <li>Asynchronous: {@code Executors.newCachedThreadPool()}</li>
     * </ul>
     *
     * @param executor the executor for event delivery
     * @throws IllegalArgumentException if executor is null
     */
    public DefaultEventBus(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor cannot be null");
        }
        this.listeners = new ConcurrentHashMap<>();
        this.executor = executor;
    }

    @Override
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        log.debug("Subscribe listener for event type '{}'", eventType.getName());
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    @Override
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        List<Consumer<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            log.debug("Unsubscribe listener for event type '{}'", eventType.getName());
            eventListeners.remove(listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        Class<?> eventType = event.getClass();
        List<Consumer<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null && !eventListeners.isEmpty()) {
            log.debug("Publishing event of type '{}' to {} listener(s)", eventType.getName(), eventListeners.size());
            for (Consumer<?> listener : eventListeners) {
                executor.execute(() -> {
                    try {
                        ((Consumer<T>) listener).accept(event);
                    } catch (Exception e) {
                        log.error("Error delivering event of type '{}' to listener", eventType.getName(), e);
                    }
                });
            }
        } else {
            log.trace("No listeners registered for event type '{}'", eventType.getName());
        }
    }

}
