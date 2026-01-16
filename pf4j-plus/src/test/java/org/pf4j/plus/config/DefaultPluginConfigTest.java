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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class DefaultPluginConfigTest {

    private static final String PLUGIN_ID = "test-plugin";

    private DefaultPluginConfig config;
    private List<ConfigChangeEvent> events;

    @BeforeEach
    public void setUp() {
        events = new ArrayList<>();
        config = new DefaultPluginConfig(PLUGIN_ID, new HashMap<>(), events::add);
    }

    @Test
    public void testGetReturnsEmptyForNonExistentKey() {
        assertTrue(config.get("non-existent").isEmpty());
    }

    @Test
    public void testSetAndGet() {
        config.set("key", "value");

        assertTrue(config.get("key").isPresent());
        assertEquals("value", config.get("key").get());
    }

    @Test
    public void testGetWithDefault() {
        assertEquals("default", config.get("non-existent", "default"));

        config.set("key", "value");
        assertEquals("value", config.get("key", "default"));
    }

    @Test
    public void testRemove() {
        config.set("key", "value");
        assertTrue(config.get("key").isPresent());

        config.remove("key");
        assertTrue(config.get("key").isEmpty());
    }

    @Test
    public void testRemoveNonExistentKeyDoesNotFireEvent() {
        config.remove("non-existent");
        assertTrue(events.isEmpty());
    }

    @Test
    public void testKeys() {
        assertTrue(config.keys().isEmpty());

        config.set("key1", "value1");
        config.set("key2", "value2");

        assertEquals(2, config.keys().size());
        assertTrue(config.keys().contains("key1"));
        assertTrue(config.keys().contains("key2"));
    }

    @Test
    public void testGetAll() {
        config.set("key1", "value1");
        config.set("key2", "value2");

        Map<String, String> all = config.getAll();
        assertEquals(2, all.size());
        assertEquals("value1", all.get("key1"));
        assertEquals("value2", all.get("key2"));
    }

    @Test
    public void testGetAllReturnsUnmodifiableCopy() {
        config.set("key", "value");

        Map<String, String> all = config.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.put("new", "value"));
    }

    @Test
    public void testSetFiresChangeEvent() {
        config.set("key", "value");

        assertEquals(1, events.size());
        ConfigChangeEvent event = events.get(0);
        assertEquals(PLUGIN_ID, event.pluginId());
        assertEquals("key", event.key());
        assertTrue(event.oldValue().isEmpty());
        assertEquals("value", event.newValue().orElse(null));
    }

    @Test
    public void testSetUpdateFiresChangeEventWithOldValue() {
        config.set("key", "old");
        events.clear();

        config.set("key", "new");

        assertEquals(1, events.size());
        ConfigChangeEvent event = events.get(0);
        assertEquals("old", event.oldValue().orElse(null));
        assertEquals("new", event.newValue().orElse(null));
    }

    @Test
    public void testRemoveFiresChangeEvent() {
        config.set("key", "value");
        events.clear();

        config.remove("key");

        assertEquals(1, events.size());
        ConfigChangeEvent event = events.get(0);
        assertEquals("key", event.key());
        assertEquals("value", event.oldValue().orElse(null));
        assertTrue(event.newValue().isEmpty());
    }

    // Typed helpers tests

    @Test
    public void testGetInt() {
        assertEquals(42, config.getInt("missing", 42));

        config.set("port", "8080");
        assertEquals(8080, config.getInt("port", 0));
    }

    @Test
    public void testSetInt() {
        config.setInt("port", 8080);
        assertEquals("8080", config.get("port").orElse(null));
    }

    @Test
    public void testGetBoolean() {
        assertFalse(config.getBoolean("missing", false));
        assertTrue(config.getBoolean("missing", true));

        config.set("enabled", "true");
        assertTrue(config.getBoolean("enabled", false));

        config.set("disabled", "false");
        assertFalse(config.getBoolean("disabled", true));
    }

    @Test
    public void testSetBoolean() {
        config.setBoolean("enabled", true);
        assertEquals("true", config.get("enabled").orElse(null));
    }

    @Test
    public void testGetLong() {
        assertEquals(100L, config.getLong("missing", 100L));

        config.set("timestamp", "1234567890123");
        assertEquals(1234567890123L, config.getLong("timestamp", 0L));
    }

    @Test
    public void testGetDouble() {
        assertEquals(3.14, config.getDouble("missing", 3.14), 0.001);

        config.set("ratio", "2.5");
        assertEquals(2.5, config.getDouble("ratio", 0.0), 0.001);
    }

    @Test
    public void testGetList() {
        assertTrue(config.getList("missing").isEmpty());

        config.set("items", "a,b,c");
        List<String> items = config.getList("items");
        assertEquals(3, items.size());
        assertEquals("a", items.get(0));
        assertEquals("b", items.get(1));
        assertEquals("c", items.get(2));
    }

    @Test
    public void testSetList() {
        config.setList("items", List.of("x", "y", "z"));
        assertEquals("x,y,z", config.get("items").orElse(null));
    }

    @Test
    public void testNullCallbackDoesNotThrow() {
        DefaultPluginConfig configWithoutCallback = new DefaultPluginConfig(PLUGIN_ID, new HashMap<>(), null);

        assertDoesNotThrow(() -> configWithoutCallback.set("key", "value"));
        assertDoesNotThrow(() -> configWithoutCallback.remove("key"));
    }

    @Test
    public void testInitialDataIsLoaded() {
        Map<String, String> initialData = new HashMap<>();
        initialData.put("existing", "value");

        DefaultPluginConfig configWithData = new DefaultPluginConfig(PLUGIN_ID, initialData, null);

        assertEquals("value", configWithData.get("existing").orElse(null));
    }

}