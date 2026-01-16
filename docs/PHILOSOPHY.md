# Philosophy

These principles guide PF4J-Plus design decisions.

## The host owns infrastructure

The host application owns:
- the ServiceRegistry
- the EventBus
- the ConfigService
- threads, executors, and shared resources

Plugins consume infrastructure. They do not create or control it.

Ownership is clear and non-negotiable.

## Explicit over magical

All dependencies must be:
- visible in code
- traceable at runtime
- understandable during debugging

Therefore:
- no implicit auto-wiring
- no classpath scanning
- no hidden proxies or bytecode tricks

If a plugin depends on something, it must be obvious *how* and *where* that dependency comes from.

## Wiring, not lifecycle management

The platform connects components. It does not manage their business lifecycle.

The platform does **not**:
- start or stop business services
- orchestrate domain workflows
- manage resource-heavy lifecycles

Plugins are responsible for their own internal behavior.
The platform only provides the connections.

## Simple injection, limited scope

`@Inject` exists only to reduce boilerplate.

It is intentionally limited to:
- field injection only
- known services from ServiceRegistry
- explicitly registered components

It does **not** provide:
- constructor or method injection
- scope management
- interception or AOP
- dependency graph resolution

This is wiring, not dependency injection as a paradigm.

## EventBus is for notification

The EventBus broadcasts facts. It does not coordinate workflows.

It is **not**:
- a workflow engine
- a replacement for explicit APIs
- a hidden control plane

Events should be small, descriptive, and easy to reason about.

## Threading is explicit

- Synchronous behavior is the default
- Asynchronous execution requires explicit configuration
- Thread pools belong to the host

Plugins must never assume threading behavior they did not request.

## No containers per plugin

PF4J-Plus deliberately avoids:
- CDI containers per plugin
- Spring application contexts per plugin
- nested or child containers of any kind

Reasons:
- complex lifecycles
- fragile unload behavior
- high risk of memory leaks

PF4J controls instantiation. PF4J-Plus enhances instances but never owns them.

---

## Final Principle

If a feature reduces clarity, hides ownership, complicates unload, or makes runtime behavior harder to reason about — it does not belong here.

Simplicity is a deliberate constraint.