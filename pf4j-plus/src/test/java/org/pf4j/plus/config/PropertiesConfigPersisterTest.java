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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Decebal Suiu
 */
public class PropertiesConfigPersisterTest {

    @TempDir
    Path tempDir;

    private PropertiesConfigPersister persister;

    @BeforeEach
    public void setUp() {
        persister = new PropertiesConfigPersister(tempDir);
    }

    @Test
    public void testLoadReturnsEmptyMapWhenFileDoesNotExist() {
        Map<String, String> config = persister.load("non-existent");

        assertNotNull(config);
        assertTrue(config.isEmpty());
    }

    @Test
    public void testSaveCreatesFile() {
        Map<String, String> config = new HashMap<>();
        config.put("key", "value");

        persister.save("plugin-a", config);

        Path configFile = tempDir.resolve("plugin-a.properties");
        assertTrue(Files.exists(configFile));
    }

    @Test
    public void testSaveAndLoad() {
        Map<String, String> config = new HashMap<>();
        config.put("key1", "value1");
        config.put("key2", "value2");

        persister.save("plugin-a", config);
        Map<String, String> loaded = persister.load("plugin-a");

        assertEquals(2, loaded.size());
        assertEquals("value1", loaded.get("key1"));
        assertEquals("value2", loaded.get("key2"));
    }

    @Test
    public void testSaveOverwritesExistingFile() {
        Map<String, String> config1 = new HashMap<>();
        config1.put("key", "value1");
        persister.save("plugin-a", config1);

        Map<String, String> config2 = new HashMap<>();
        config2.put("key", "value2");
        persister.save("plugin-a", config2);

        Map<String, String> loaded = persister.load("plugin-a");
        assertEquals("value2", loaded.get("key"));
    }

    @Test
    public void testDelete() {
        Map<String, String> config = new HashMap<>();
        config.put("key", "value");
        persister.save("plugin-a", config);

        Path configFile = tempDir.resolve("plugin-a.properties");
        assertTrue(Files.exists(configFile));

        persister.delete("plugin-a");

        assertFalse(Files.exists(configFile));
    }

    @Test
    public void testDeleteNonExistentFileDoesNotThrow() {
        assertDoesNotThrow(() -> persister.delete("non-existent"));
    }

    @Test
    public void testSaveCreatesDirectoryIfNotExists() throws IOException {
        Path nestedDir = tempDir.resolve("nested/config/dir");
        PropertiesConfigPersister nestedPersister = new PropertiesConfigPersister(nestedDir);

        Map<String, String> config = new HashMap<>();
        config.put("key", "value");

        nestedPersister.save("plugin-a", config);

        assertTrue(Files.exists(nestedDir));
        assertTrue(Files.exists(nestedDir.resolve("plugin-a.properties")));
    }

    @Test
    public void testUtf8Encoding() {
        Map<String, String> config = new HashMap<>();
        config.put("greeting", "Привет мир");
        config.put("japanese", "こんにちは");

        persister.save("plugin-a", config);
        Map<String, String> loaded = persister.load("plugin-a");

        assertEquals("Привет мир", loaded.get("greeting"));
        assertEquals("こんにちは", loaded.get("japanese"));
    }

    @Test
    public void testSpecialCharactersInValues() {
        Map<String, String> config = new HashMap<>();
        config.put("path", "C:\\Users\\test\\file.txt");
        config.put("url", "https://example.com?foo=bar&baz=qux");

        persister.save("plugin-a", config);
        Map<String, String> loaded = persister.load("plugin-a");

        assertEquals("C:\\Users\\test\\file.txt", loaded.get("path"));
        assertEquals("https://example.com?foo=bar&baz=qux", loaded.get("url"));
    }

    @Test
    public void testEmptyConfig() {
        persister.save("plugin-a", new HashMap<>());
        Map<String, String> loaded = persister.load("plugin-a");

        assertTrue(loaded.isEmpty());
    }

    @Test
    public void testFileContainsPluginIdComment() throws IOException {
        Map<String, String> config = new HashMap<>();
        config.put("key", "value");

        persister.save("my-plugin", config);

        Path configFile = tempDir.resolve("my-plugin.properties");
        String content = Files.readString(configFile, StandardCharsets.UTF_8);

        assertTrue(content.contains("my-plugin"));
    }

}