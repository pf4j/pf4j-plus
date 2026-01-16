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
public class DefaultConfigServiceTest {

    private DefaultConfigService configService;
    private MockConfigPersister persister;

    @BeforeEach
    public void setUp() {
        persister = new MockConfigPersister();
        configService = new DefaultConfigService(persister);
    }

    @Test
    public void testForPluginReturnsConfig() {
        PluginConfig config = configService.forPlugin("plugin-a");

        assertNotNull(config);
    }

    @Test
    public void testForPluginReturnsSameInstance() {
        PluginConfig config1 = configService.forPlugin("plugin-a");
        PluginConfig config2 = configService.forPlugin("plugin-a");

        assertSame(config1, config2);
    }

    @Test
    public void testForPluginReturnsDifferentInstancesForDifferentPlugins() {
        PluginConfig configA = configService.forPlugin("plugin-a");
        PluginConfig configB = configService.forPlugin("plugin-b");

        assertNotSame(configA, configB);
    }

    @Test
    public void testForPluginLoadsFromPersister() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        persister.setData("plugin-a", data);

        PluginConfig config = configService.forPlugin("plugin-a");

        assertEquals("value", config.get("key").orElse(null));
    }

    @Test
    public void testAutoSaveOnSet() {
        PluginConfig config = configService.forPlugin("plugin-a");

        config.set("key", "value");

        Map<String, String> saved = persister.getSavedData("plugin-a");
        assertNotNull(saved);
        assertEquals("value", saved.get("key"));
    }

    @Test
    public void testAutoSaveOnEveryChange() {
        PluginConfig config = configService.forPlugin("plugin-a");

        config.set("key1", "value1");
        assertEquals(1, persister.getSaveCount("plugin-a"));

        config.set("key2", "value2");
        assertEquals(2, persister.getSaveCount("plugin-a"));
    }

    @Test
    public void testListenerDisablesAutoSave() {
        List<ConfigChangeEvent> events = new ArrayList<>();
        configService.setChangeListener(events::add);

        PluginConfig config = configService.forPlugin("plugin-a");
        config.set("key", "value");

        // Listener received the event
        assertEquals(1, events.size());
        // But persister was not called
        assertEquals(0, persister.getSaveCount("plugin-a"));
    }

    @Test
    public void testRemoveListenerRestoresAutoSave() {
        configService.setChangeListener(event -> {});
        configService.removeChangeListener();

        PluginConfig config = configService.forPlugin("plugin-a");
        config.set("key", "value");

        assertEquals(1, persister.getSaveCount("plugin-a"));
    }

    @Test
    public void testManualSave() {
        configService.setChangeListener(event -> {}); // Disable auto-save

        PluginConfig config = configService.forPlugin("plugin-a");
        config.set("key", "value");

        assertEquals(0, persister.getSaveCount("plugin-a"));

        configService.save("plugin-a");

        assertEquals(1, persister.getSaveCount("plugin-a"));
        assertEquals("value", persister.getSavedData("plugin-a").get("key"));
    }

    @Test
    public void testSaveAll() {
        configService.setChangeListener(event -> {}); // Disable auto-save

        configService.forPlugin("plugin-a").set("keyA", "valueA");
        configService.forPlugin("plugin-b").set("keyB", "valueB");

        configService.saveAll();

        assertEquals(1, persister.getSaveCount("plugin-a"));
        assertEquals(1, persister.getSaveCount("plugin-b"));
    }

    @Test
    public void testDelete() {
        PluginConfig config = configService.forPlugin("plugin-a");
        config.set("key", "value");

        configService.delete("plugin-a");

        assertTrue(persister.isDeleted("plugin-a"));

        // After delete, forPlugin returns a fresh instance
        PluginConfig newConfig = configService.forPlugin("plugin-a");
        assertTrue(newConfig.get("key").isEmpty());
    }

    // Mock ConfigPersister for testing

    static class MockConfigPersister implements ConfigPersister {

        private final Map<String, Map<String, String>> storage = new HashMap<>();
        private final Map<String, Map<String, String>> savedData = new HashMap<>();
        private final Map<String, Integer> saveCounts = new HashMap<>();
        private final List<String> deleted = new ArrayList<>();

        public void setData(String pluginId, Map<String, String> data) {
            storage.put(pluginId, new HashMap<>(data));
        }

        public Map<String, String> getSavedData(String pluginId) {
            return savedData.get(pluginId);
        }

        public int getSaveCount(String pluginId) {
            return saveCounts.getOrDefault(pluginId, 0);
        }

        public boolean isDeleted(String pluginId) {
            return deleted.contains(pluginId);
        }

        @Override
        public Map<String, String> load(String pluginId) {
            return new HashMap<>(storage.getOrDefault(pluginId, new HashMap<>()));
        }

        @Override
        public void save(String pluginId, Map<String, String> config) {
            savedData.put(pluginId, new HashMap<>(config));
            saveCounts.merge(pluginId, 1, Integer::sum);
        }

        @Override
        public void delete(String pluginId) {
            storage.remove(pluginId);
            savedData.remove(pluginId);
            deleted.add(pluginId);
        }

    }

}