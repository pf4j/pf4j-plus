# PF4J-Plus Demo

This demo application showcases the main features of PF4J-Plus.

## What This Demo Shows

1. **PlusPluginManagerBuilder** - Simplified PluginManager setup
   - Inline service registration with `serviceRegistry(registry -> {...})`
   - Custom class loading with `excludeFromParentDelegation()`

2. **ServiceRegistry** - Centralized service management
   - Registering services in the host application
   - Accessing services from plugins and extensions

3. **Service Injection** - Two approaches for accessing services:
   - **ServiceRegistryAware** interface - plugins receive the ServiceRegistry directly
   - **@Inject annotation** - automatic field injection into plugins and extensions

4. **EventBus** - Cross-plugin communication (registered as a service)

## Project Structure

```
demo/
├── app/                           # Host application
│   └── org.pf4j.plus.demo.app
│       ├── Boot.java              # Main application entry point
│       ├── GreetingService.java   # Example service interface
│       ├── DefaultGreetingService.java
│       └── Greeter.java           # Extension point
│
└── plugins/
    ├── greeting-plugin/           # Plugin demonstrating ServiceRegistryAware
    │   └── org.pf4j.plus.demo.plugins.greeting
    │       ├── GreetingPlugin.java    # Uses ServiceRegistryAware
    │       └── SimpleGreeter.java     # Uses @Inject
    │
    └── welcome-plugin/            # Plugin demonstrating @Inject only
        └── org.pf4j.plus.demo.plugins.welcome
            ├── WelcomePlugin.java     # Uses @Inject
            └── FancyGreeter.java      # Uses @Inject
```

## Building and Running

### 1. Build everything

```bash
cd /path/to/pf4j-plus
mvn clean package
```

This will:
- Build `pf4j-plus` core library
- Build demo `app`
- Build both demo `plugins`

### 2. Copy plugins to the plugins directory

```bash
# Create plugins directory
mkdir -p demo/app/plugins

# Copy plugin JARs
cp demo/plugins/greeting-plugin/target/pf4j-plus-demo-greeting-plugin-0.1.0-SNAPSHOT.jar demo/app/plugins/
cp demo/plugins/welcome-plugin/target/pf4j-plus-demo-welcome-plugin-0.1.0-SNAPSHOT.jar demo/app/plugins/
```

### 3. Run the demo application

```bash
cd demo/app
mvn exec:java
```

## Expected Output

```
=== PF4J-Plus Demo Application ===

1. Creating PluginManager with PlusPluginManagerBuilder...
   - Registered GreetingService
   - Registered EventBus
   - Registered ConfigService (config directory: ./config)
   - PluginManager created with ServiceRegistry and Injection support

2. Loading and starting plugins...
   - Loaded 2 plugin(s)
Greeting Plugin started!
  Got GreetingService from registry: Hello, Greeting Plugin!
Welcome Plugin started!
  Got GreetingService via @Inject: Hello, Welcome Plugin!

3. Executing Greeter extensions...
   - Found 2 Greeter extension(s)
   [SimpleGreeter] Hello, World!
   [FancyGreeter] *** Hello, Amazing Developer! ***

4. Stopping plugins...
Greeting Plugin stopped!
Welcome Plugin stopped!

=== Demo completed successfully! ===
```

## Key Takeaways

### ServiceRegistryAware Pattern
```java
public class MyPlugin extends Plugin implements ServiceRegistryAware {
    private ServiceRegistry serviceRegistry;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void start() {
        MyService service = serviceRegistry.require(MyService.class);
        service.doSomething();
    }
}
```

### @Inject Pattern
```java
public class MyPlugin extends Plugin {
    @Inject
    private MyService myService;

    @Override
    public void start() {
        myService.doSomething();
    }
}
```

### Setting Up PluginManager with Builder
```java
PluginManager pluginManager = PlusPluginManagerBuilder.create()
    .serviceRegistry(registry -> {
        registry.register(MyService.class, new MyServiceImpl());
        registry.register(EventBus.class, new DefaultEventBus());
    })
    .build();
```

## Next Steps

- Try adding your own plugin
- Experiment with EventBus for cross-plugin communication
- Add more services to the ServiceRegistry
- Combine ServiceRegistryAware with @Inject in the same plugin
