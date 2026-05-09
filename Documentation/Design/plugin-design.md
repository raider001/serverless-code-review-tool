# Plugin Design

## Purpose

Define how third-party services can integrate with the Serverless Review Tool without modifying the core application.

## Module Layout

- `ReviewToolApplication` - core UI/application logic and `PluginRegistry`
- `ReviewToolPluginInterface` - base plugin contracts (`Plugin`, `UserPlugin`, `NotificationPlugin`)
- `ReviewToolDefaultPlugins` - optional default plugin implementations

---

## Design Goals

- **Optional by default** — the tool functions fully with zero plugins registered
- **Isolated** — plugin failures must never crash or degrade the core tool
- **Loosely coupled** — plugins depend on the tool's interfaces, not its internals
- **Replaceable** — any plugin can be swapped without changing the tool
- **Minimal surface area** — the tool exposes only what it needs to, not its implementation

---

## Plugin Model

Plugins are Java class implementations. The tool defines a base `Plugin` contract and abstract plugin types (`UserPlugin`, `NotificationPlugin`) in a dedicated interface/API module. A third party extends one or more of those abstract types and registers the implementation with the tool.

The tool calls plugins. Plugins do not call into the tool (no reverse dependency).

```
Tool Core
  └─ PluginRegistry
       ├─ resolves registered implementations at startup
       └─ provides them to internal services on demand

Third-party plugin
  └─ extends one or more plugin abstract classes
  └─ auto-discovered from plugin JARs in the plugins directory
```

---

## Plugin API Module

Each integration point is defined in a dedicated plugin module: `ReviewToolPluginInterface`. This module contains only plugin contracts and shared records — no tool implementation code.

This keeps the contract stable and independent of internal changes.

```
ReviewToolPluginInterface/
  └─ src/main/java/
  └─ com.kalynx.serverlessreviewtool.plugin/
       ├─ Plugin.java                (base interface)
       ├─ UserPlugin.java            (abstract class, extends Plugin)
       └─ NotificationPlugin.java    (abstract class, extends Plugin)
```

Plugin authors depend **only** on this module, not on the full tool application.

---

## Registration — Drop a JAR, Nothing Else

Plugins are auto-discovered at startup. No tool configuration changes are required.

**How to install a plugin:**
1. Build the plugin as a JAR
2. Drop it into the `plugins/` directory next to the tool
3. Restart the tool — it is automatically picked up

**How it works:**
- At startup, `PluginRegistry` scans the `plugins/` directory for `*.jar` files
- A `URLClassLoader` is created from all discovered JARs
- `ServiceLoader.load(Plugin.class, classLoader)` enumerates all implementations
- Each implementation is registered by its plugin interface type

**Plugin directory:**
- Default: `./plugins` (relative to working directory)
- Override: system property `srt.plugins.dir`

**Plugin JAR requirements:**
```
myplugin.jar
  └─ META-INF/services/
       └─ com.kalynx.serverlessreviewtool.plugin.Plugin
            → com.example.myplugin.MyImplementation
  └─ com/example/myplugin/MyImplementation.class
```

The only requirement in the JAR is a `META-INF/services` entry pointing to the implementation class. The tool handles everything else.

---

## Plugin Lifecycle

```
1. Startup     → PluginRegistry scans ServiceLoader for each known interface
2. Registered  → found implementations are stored and available
3. Event       → tool fires relevant calls to registered plugins
4. Shutdown    → tool calls close() on any plugin implementing AutoCloseable
```

---

## Isolation and Failure Handling

Plugin calls are wrapped by the tool:

- Executed asynchronously where the integration point allows it
- Any unchecked exception from a plugin is caught and logged
- The tool continues normally regardless of plugin outcome
- A plugin that consistently fails may be flagged in the Logs panel

---

## Versioning

The plugin API module is versioned independently of the tool. Interface changes follow:

- **Additive changes** (new optional methods with defaults) — backwards compatible
- **Breaking changes** — new major version of the API module
- Plugins declare which API version they target

---

## Example Integration Points

The following are examples of integration points, not an exhaustive list. Each maps to a plugin interface in the API module.

| Integration Point | Purpose |
|-------------------|---------|
| Notification Service | Receive review lifecycle events and dispatch to external systems |
| User Service | Provide enriched user information given a git username |

Both are **optional**. The tool falls back to raw Git identity and no notifications if nothing is registered.

---

## What Plugins Cannot Do

- Cannot intercept or modify core Git operations
- Cannot block the tool's main workflow (failures are swallowed)
- Cannot enforce identity at the Git remote level (see `design-decision-register.md`)
- Cannot communicate back to the tool unsolicited

---

## Related Documents

- `design-decision-register.md` — User Identity Trust Model
- `user-management.md` — Identity limitations and why user enrichment is plugin-based








