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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class DefaultEventBusTest {

    private EventBus eventBus;

    @BeforeEach
    public void setUp() {
        eventBus = new DefaultEventBus();
    }

    @AfterEach
    public void tearDown() {
        eventBus = null;
    }

    /**
     * Test basic subscribe and publish.
     */
    @Test
    public void testSubscribeAndPublish() {
        List<String> received = new ArrayList<>();
        Consumer<TestEvent> listener = event -> received.add(event.getMessage());

        eventBus.subscribe(TestEvent.class, listener);
        eventBus.publish(new TestEvent("hello"));

        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));
    }

    /**
     * Test publishing to multiple listeners.
     */
    @Test
    public void testPublishToMultipleListeners() {
        List<String> received1 = new ArrayList<>();
        List<String> received2 = new ArrayList<>();

        eventBus.subscribe(TestEvent.class, event -> received1.add(event.getMessage()));
        eventBus.subscribe(TestEvent.class, event -> received2.add(event.getMessage()));

        eventBus.publish(new TestEvent("test"));

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
        assertEquals("test", received1.get(0));
        assertEquals("test", received2.get(0));
    }

    /**
     * Test unsubscribe removes listener.
     */
    @Test
    public void testUnsubscribe() {
        List<String> received = new ArrayList<>();
        Consumer<TestEvent> listener = event -> received.add(event.getMessage());

        eventBus.subscribe(TestEvent.class, listener);
        eventBus.publish(new TestEvent("first"));

        eventBus.unsubscribe(TestEvent.class, listener);
        eventBus.publish(new TestEvent("second"));

        assertEquals(1, received.size());
        assertEquals("first", received.get(0));
    }

    /**
     * Test unsubscribe with non-registered listener has no effect.
     */
    @Test
    public void testUnsubscribeNonRegisteredListener() {
        Consumer<TestEvent> listener = event -> {};

        // Should not throw
        assertDoesNotThrow(() -> eventBus.unsubscribe(TestEvent.class, listener));
    }

    /**
     * Test publishing with no listeners.
     */
    @Test
    public void testPublishWithNoListeners() {
        // Should not throw
        assertDoesNotThrow(() -> eventBus.publish(new TestEvent("test")));
    }

    /**
     * Test different event types are isolated.
     */
    @Test
    public void testDifferentEventTypesAreIsolated() {
        List<String> testEvents = new ArrayList<>();
        List<String> anotherEvents = new ArrayList<>();

        eventBus.subscribe(TestEvent.class, event -> testEvents.add(event.getMessage()));
        eventBus.subscribe(AnotherEvent.class, event -> anotherEvents.add(event.getData()));

        eventBus.publish(new TestEvent("test"));
        eventBus.publish(new AnotherEvent("another"));

        assertEquals(1, testEvents.size());
        assertEquals(1, anotherEvents.size());
        assertEquals("test", testEvents.get(0));
        assertEquals("another", anotherEvents.get(0));
    }

    /**
     * Test subscribe with null event type.
     */
    @Test
    public void testSubscribeWithNullEventType() {
        Consumer<TestEvent> listener = event -> {};
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(null, listener));
    }

    /**
     * Test subscribe with null listener.
     */
    @Test
    public void testSubscribeWithNullListener() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.subscribe(TestEvent.class, null));
    }

    /**
     * Test unsubscribe with null event type.
     */
    @Test
    public void testUnsubscribeWithNullEventType() {
        Consumer<TestEvent> listener = event -> {};
        assertThrows(IllegalArgumentException.class, () -> eventBus.unsubscribe(null, listener));
    }

    /**
     * Test unsubscribe with null listener.
     */
    @Test
    public void testUnsubscribeWithNullListener() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.unsubscribe(TestEvent.class, null));
    }

    /**
     * Test publish with null event.
     */
    @Test
    public void testPublishWithNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> eventBus.publish(null));
    }

    /**
     * Test creating event bus with null executor.
     */
    @Test
    public void testCreateWithNullExecutor() {
        assertThrows(IllegalArgumentException.class, () -> new DefaultEventBus(null));
    }

    /**
     * Test exception in listener does not affect other listeners.
     */
    @Test
    public void testExceptionInListenerDoesNotAffectOthers() {
        List<String> received = new ArrayList<>();

        eventBus.subscribe(TestEvent.class, event -> {
            throw new RuntimeException("Listener error");
        });
        eventBus.subscribe(TestEvent.class, event -> received.add(event.getMessage()));

        eventBus.publish(new TestEvent("test"));

        // Second listener should still receive the event
        assertEquals(1, received.size());
        assertEquals("test", received.get(0));
    }

    /**
     * Test asynchronous event delivery with custom executor.
     */
    @Test
    public void testAsynchronousEventDelivery() throws InterruptedException {
        EventBus asyncEventBus = new DefaultEventBus(Executors.newSingleThreadExecutor());
        CountDownLatch latch = new CountDownLatch(1);
        List<String> received = new ArrayList<>();

        asyncEventBus.subscribe(TestEvent.class, event -> {
            received.add(event.getMessage());
            latch.countDown();
        });

        asyncEventBus.publish(new TestEvent("async"));

        // Wait for async delivery
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("async", received.get(0));
    }

    // Test event classes

    static class TestEvent {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static class AnotherEvent {
        private final String data;

        public AnotherEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

}
