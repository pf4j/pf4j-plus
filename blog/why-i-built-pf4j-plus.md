---
title: Why I Built pf4j-plus
date: 2026-01-16
author: Decebal Suiu
tags: java, pf4j, plugins, architecture
---

# Why I Built pf4j-plus

Years ago, I created [PF4J](https://github.com/pf4j/pf4j), a lightweight plugin framework for Java. It now has over 2,600 stars and is used by [companies worldwide](https://github.com/pf4j/pf4j#trusted-by). After maintaining it for years, I started noticing patterns in the GitHub issues. The same three problems kept appearing, and I finally hit them myself.

## The Pattern

Looking through PF4J issues and discussions, three questions come up repeatedly:

**Service access.** How do plugins get shared services like database connections, metrics, or configuration? Everyone ends up writing custom PluginFactory implementations with tons of boilerplate. [Issue #319](https://github.com/pf4j/pf4j/issues/319) is a typical example—someone asking exactly this with no standard answer.

**Plugin communication.** Plugin A produces events that Plugin B cares about. But Plugin A shouldn't know Plugin B exists. So everyone builds their own EventBus or messaging layer.

**Configuration.** Each plugin has its own settings. Where do they come from? How do you override them? No standard approach exists.

These aren't edge cases. They're core platform needs that every serious plugin system eventually hits. And everyone solves them differently.

Large companies have the resources to build custom solutions or integrate frameworks like Spring. But smaller teams and individual developers end up reinventing the same wheel—writing platform code that has nothing to do with their actual domain.

## The Trigger

I'm building vt4j, a video analysis toolkit for sports training. It started as a personal project for my archery practice—I wanted to analyze my form frame by frame.

The architecture is plugin-based: a core that handles video playback and navigation, with features added as plugins. Slow motion analysis. Annotations. Drawing overlays. ML-based posture detection. Users enable what they need through a simple checkbox menu—toggle a plugin off, its toolbar disappears and screen space opens up.

PF4J was perfect for loading plugins. But then I hit all three problems:

**EventBus:** When a user trims a video, all plugins need to recalculate their data. When someone toggles the annotation plugin off, the UI needs to reflow. When video plays or pauses, plugins need to sync. Without a standard event system, coordinating all this leads to tight coupling between plugins.

**Service access:** The ML plugin needs a settings service to choose between different detection models. Multiple plugins need access to the video service. I was writing custom PluginFactory code everywhere—the same boilerplate I'd seen in dozens of GitHub issues.

**Config:** Each plugin has settings. ML thresholds for posture detection. UI preferences for the drawing tools. Sport-specific analysis rules. No standard way to handle any of it.

I realized I was writing the same platform layer that everyone asks about in PF4J discussions.

vt4j will be open-sourced soon—it's the first real-world user of pf4j-plus.

## The Solution

So I built [pf4j-plus](https://github.com/pf4j/pf4j-plus): `ServiceRegistry` for shared services, `EventBus` for decoupled communication, `ConfigService` for plugin configuration. 

It's a lightweight toolkit, not a framework. It standardizes the platform layer that teams end up building anyway—especially useful if you're not using Spring. Desktop apps, CLI tools, embedded systems, anywhere a full enterprise stack feels like overkill.

If you're building plugin systems and keep writing the same wiring code, pf4j-plus might help.
