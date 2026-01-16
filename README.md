# PF4J-Plus

[![GitHub Actions Status](https://github.com/pf4j/pf4j-plus/actions/workflows/build.yml/badge.svg)](https://github.com/pf4j/pf4j-plus/actions/workflows/build.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pf4j_pf4j-plus&metric=coverage)](https://sonarcloud.io/summary/overall?id=pf4j_pf4j-plus)
[![Maven Central](http://img.shields.io/maven-central/v/org.pf4j.plus/pf4j-plus.svg)](http://search.maven.org/#search|ga|1|pf4j-plus)

**The missing platform layer for PF4J applications.**

PF4J handles plugin loading and extension points. But plugins need more: shared services, configuration, inter-plugin communication, and access to their own metadata. Without a standard approach, you end up writing custom `PluginFactory` and `ExtensionFactory` implementations in every project—wiring that has nothing to do with your domain.

PF4J-Plus provides these building blocks so you can focus on your application logic.

> **Note:** This project is in early development. APIs may change between versions until a stable release.

## Before / After

**Without PF4J-Plus** — custom factories for each project ([common pattern](https://github.com/pf4j/pf4j/issues/319)):

```java
// Custom plugin base class with setters for each service
public abstract class MyPlugin extends Plugin {
    protected GreetingService greetingService;
    protected EventBus eventBus;

    public void setGreetingService(GreetingService s) { this.greetingService = s; }
    public void setEventBus(EventBus e) { this.eventBus = e; }
}

// Custom factory to inject services
public class MyPluginFactory extends DefaultPluginFactory {
    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        Plugin plugin = super.create(pluginWrapper);
        if (plugin instanceof MyPlugin) {
            ((MyPlugin) plugin).setGreetingService(Application.getGreetingService());
            ((MyPlugin) plugin).setEventBus(Application.getEventBus());
        }
        return plugin;
    }
}

// Custom manager to use your factory
public class MyPluginManager extends DefaultPluginManager {
    @Override
    protected PluginFactory createPluginFactory() {
        return new MyPluginFactory();
    }
}
// ... and similar boilerplate for ExtensionFactory if extensions need services
```

**With PF4J-Plus** — declare services, get wiring for free:

```java
PluginManager pluginManager = PlusPluginManagerBuilder.create()
    .serviceRegistry(registry -> {
        registry.register(GreetingService.class, new DefaultGreetingService());
        registry.register(EventBus.class, new DefaultEventBus());
    })
    .build();
```

Plugins implement `ServiceRegistryAware` to receive services automatically. Extensions use `@Inject` for field injection. No custom factories required.

---

**What PF4J-Plus is NOT:**
- Not a DI container (no scopes, no AOP, no dependency graphs)
- Not a framework (no opinions about your architecture)
- Not a replacement for Spring/CDI (use those if you need their features)

It's just the platform wiring that every PF4J application rebuilds. Standardized.

## Components

| Component | Purpose |
|-----------|---------|
| **ServiceRegistry** | Central registry for sharing services between host and plugins |
| **PluginInfo** | Read-only plugin metadata (id, version, path) without PluginManager access |
| **EventBus** | Publish-subscribe communication between plugins |
| **ConfigService** | Scoped configuration for each plugin |
| **@Inject** | Simple field injection from ServiceRegistry |

## Quick Start

### 1. Set up the host application

```java
// Create ServiceRegistry and register services
ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
serviceRegistry.register(GreetingService.class, new DefaultGreetingService());
serviceRegistry.register(EventBus.class, new DefaultEventBus());
serviceRegistry.register(ConfigService.class,
    new DefaultConfigService(new PropertiesConfigPersister(Path.of("config"))));

// Create PluginManager with PF4J-Plus support
PluginManager pluginManager = PlusPluginManagerBuilder.create()
    .serviceRegistry(serviceRegistry)
    .build();

// Load and start plugins
pluginManager.loadPlugins();
pluginManager.startPlugins();
```

Or with inline service registration:

```java
PluginManager pluginManager = PlusPluginManagerBuilder.create()
    .serviceRegistry(registry -> {
        registry.register(GreetingService.class, new DefaultGreetingService());
        registry.register(EventBus.class, new DefaultEventBus());
    })
    .pluginsRoot(Path.of("my-plugins"))
    .build();
```

### 2. Access services in plugins

Each plugin receives a **scoped ServiceRegistry** that provides:
- Access to global services (GreetingService, EventBus, etc.)
- Automatic `PluginInfo` with this plugin's metadata
- Automatic `PluginConfig` scoped to this plugin

```java
public class GreetingPlugin extends Plugin implements ServiceRegistryAware {

    private ServiceRegistry serviceRegistry;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void start() {
        // Get plugin metadata - automatically available
        PluginInfo pluginInfo = serviceRegistry.require(PluginInfo.class);
        log.info("Plugin: id={}, version={}", pluginInfo.getPluginId(), pluginInfo.getVersion());

        // Get global service
        GreetingService greetingService = serviceRegistry.require(GreetingService.class);
        greetingService.greet("World");

        // Get plugin configuration - automatically scoped to this plugin
        PluginConfig config = serviceRegistry.require(PluginConfig.class);
        String prefix = config.get("greeting.prefix", "Hello");
    }
}
```

### 3. Use @Inject in extensions

Extensions can use `@Inject` for cleaner code:

```java
@Extension
public class FancyGreeter implements Greeter {

    @Inject
    private GreetingService greetingService;

    @Inject
    private PluginConfig config;  // Automatically scoped to this plugin

    @Inject(required = false)  // Optional dependency
    private MetricsService metrics;

    @Override
    public void greet() {
        String prefix = config.get("greeting.prefix", "Hello");
        String message = greetingService.greet("Developer");
        System.out.println(prefix + " " + message);
    }
}
```

## ServiceRegistry

The host owns and controls all service registrations. Plugins only consume services.

```java
// Register a service instance
registry.register(MyService.class, new MyServiceImpl());

// Register a lazy provider (created on first access)
registry.register(ExpensiveService.class, () -> new ExpensiveServiceImpl());

// Get a service (returns Optional)
Optional<MyService> service = registry.get(MyService.class);

// Get a required service (throws if not found)
MyService service = registry.require(MyService.class);
```

### Scoped Registry

Each plugin automatically receives its own `ServiceRegistry` that:
- Provides access to global services registered by the host
- Contains plugin-specific services (`PluginInfo`, `PluginConfig`)
- Allows plugins to register local services without affecting the global registry

## PluginInfo

Plugins often need to know their own identity. `PluginInfo` provides read-only metadata without exposing the PluginManager:

```java
PluginInfo info = serviceRegistry.require(PluginInfo.class);

String id = info.getPluginId();        // "my-plugin"
String version = info.getVersion();     // "1.0.0"
String provider = info.getProvider();   // "My Company"
Path path = info.getPluginPath();       // /path/to/plugin.jar
```

This is safer than using `getWrapper()` because plugins cannot access PluginManager operations like stopping or unloading other plugins.

## EventBus

The EventBus provides decoupled communication between plugins. Events are facts, not commands.

```java
// Subscribe to events
eventBus.subscribe(UserCreatedEvent.class, event -> {
    System.out.println("User created: " + event.username());
});

// Publish events
eventBus.publish(new UserCreatedEvent("john"));

// Unsubscribe when done
eventBus.unsubscribe(UserCreatedEvent.class, listener);
```

By default, events are delivered synchronously on the publisher's thread.
For async delivery, provide an executor when creating the EventBus:

```java
EventBus eventBus = new DefaultEventBus(Executors.newFixedThreadPool(4));
```

## ConfigService

Each plugin gets its own isolated configuration. There are two ways to access it:

### Via scoped ServiceRegistry (recommended)

In plugins and extensions, `PluginConfig` is automatically available and scoped:

```java
// In a plugin or extension - config is already scoped to this plugin
PluginConfig config = serviceRegistry.require(PluginConfig.class);

// Read values with defaults
String host = config.get("server.host", "localhost");
int port = config.getInt("server.port", 8080);
boolean enabled = config.getBoolean("feature.enabled", false);
List<String> tags = config.getList("tags");

// Write values (auto-saved by default)
config.set("server.host", "example.com");
config.setInt("server.port", 9090);
```

### Via @Inject

```java
@Extension
public class MyExtension implements MyExtensionPoint {

    @Inject
    private PluginConfig config;  // Automatically scoped to this plugin

    @Override
    public void execute() {
        int timeout = config.getInt("timeout", 30);
    }
}
```

### Via ConfigService (for host application)

```java
// In the host application when you need to access a specific plugin's config
ConfigService configService = registry.require(ConfigService.class);
PluginConfig config = configService.forPlugin("my-plugin");
```

## Design Principles

PF4J-Plus follows a clear philosophy:

- **Host owns infrastructure** - The host application controls ServiceRegistry, EventBus, and all shared resources
- **Explicit over magical** - No implicit auto-wiring, no classpath scanning, no hidden proxies
- **Wiring, not lifecycle management** - The platform connects components but doesn't manage their business lifecycle
- **Simple injection, limited scope** - Injection exists to reduce boilerplate, not to be a full DI container

For the complete philosophy, see [docs/PHILOSOPHY.md](docs/PHILOSOPHY.md).

## What PF4J-Plus is NOT

- Not a DI container (no scopes, no AOP, no dependency graphs)
- Not a framework (no opinions about your architecture)
- Not a sandbox (plugins run in the same JVM with full access)
- Not a replacement for Spring/CDI (use those if you need their features)

## Demo

A working demo is available in the [demo](demo) folder.

**Quick run:**
```bash
./run-demo.sh      # Linux/Mac
run-demo.bat       # Windows
```

See [demo/README.md](demo/README.md) for details.

## Requirements

- Java 11+
- PF4J 3.x

## License

Apache License 2.0