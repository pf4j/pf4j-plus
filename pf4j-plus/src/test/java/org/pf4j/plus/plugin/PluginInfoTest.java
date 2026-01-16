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
package org.pf4j.plus.plugin;

import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link PluginInfo}.
 *
 * @author Decebal Suiu
 */
public class PluginInfoTest {

    /**
     * Test constructor and getters.
     */
    @Test
    public void testConstructorAndGetters() {
        Path path = Path.of("/plugins/test-plugin.jar");
        PluginInfo info = new PluginInfo("test-plugin", "1.0.0", "Test description", "Test Provider", path);

        assertEquals("test-plugin", info.getPluginId());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("Test description", info.getDescription());
        assertEquals("Test Provider", info.getProvider());
        assertEquals(path, info.getPluginPath());
    }

    /**
     * Test constructor with null pluginId throws exception.
     */
    @Test
    public void testConstructorWithNullPluginIdThrows() {
        assertThrows(NullPointerException.class,
            () -> new PluginInfo(null, "1.0.0", "desc", "provider", Path.of("/path")));
    }

    /**
     * Test constructor allows null for optional fields.
     */
    @Test
    public void testConstructorAllowsNullOptionalFields() {
        PluginInfo info = new PluginInfo("test-plugin", null, null, null, null);

        assertEquals("test-plugin", info.getPluginId());
        assertNull(info.getVersion());
        assertNull(info.getDescription());
        assertNull(info.getProvider());
        assertNull(info.getPluginPath());
    }

    /**
     * Test from(PluginWrapper) factory method.
     */
    @Test
    public void testFromPluginWrapper() {
        PluginDescriptor descriptor = mock(PluginDescriptor.class);
        when(descriptor.getVersion()).thenReturn("2.0.0");
        when(descriptor.getPluginDescription()).thenReturn("A test plugin");
        when(descriptor.getProvider()).thenReturn("Acme Corp");

        PluginWrapper wrapper = mock(PluginWrapper.class);
        when(wrapper.getPluginId()).thenReturn("acme-plugin");
        when(wrapper.getDescriptor()).thenReturn(descriptor);
        when(wrapper.getPluginPath()).thenReturn(Path.of("/plugins/acme.jar"));

        PluginInfo info = PluginInfo.from(wrapper);

        assertEquals("acme-plugin", info.getPluginId());
        assertEquals("2.0.0", info.getVersion());
        assertEquals("A test plugin", info.getDescription());
        assertEquals("Acme Corp", info.getProvider());
        assertEquals(Path.of("/plugins/acme.jar"), info.getPluginPath());
    }

    /**
     * Test equals - same pluginId means equal.
     */
    @Test
    public void testEqualsSamePluginId() {
        PluginInfo info1 = new PluginInfo("same-id", "1.0.0", "desc1", "prov1", Path.of("/path1"));
        PluginInfo info2 = new PluginInfo("same-id", "2.0.0", "desc2", "prov2", Path.of("/path2"));

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    /**
     * Test equals - different pluginId means not equal.
     */
    @Test
    public void testNotEqualsDifferentPluginId() {
        PluginInfo info1 = new PluginInfo("plugin-a", "1.0.0", "desc", "prov", Path.of("/path"));
        PluginInfo info2 = new PluginInfo("plugin-b", "1.0.0", "desc", "prov", Path.of("/path"));

        assertNotEquals(info1, info2);
    }

    /**
     * Test equals with null and different type.
     */
    @Test
    public void testEqualsWithNullAndDifferentType() {
        PluginInfo info = new PluginInfo("test", "1.0", null, null, null);

        assertNotEquals(null, info);
        assertNotEquals("not a PluginInfo", info);
    }

    /**
     * Test toString contains id and version.
     */
    @Test
    public void testToString() {
        PluginInfo info = new PluginInfo("my-plugin", "3.2.1", null, null, null);

        String str = info.toString();
        assertTrue(str.contains("my-plugin"));
        assertTrue(str.contains("3.2.1"));
    }

}