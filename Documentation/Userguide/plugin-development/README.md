# Plugin Development Guide

The Serverless Review Tool is extended through a Java **Plugin API** built on the standard `java.util.ServiceLoader` mechanism. Plugins are packaged as ordinary JAR files and dropped into the application's `plugins/` directory — no source code changes or restarts of the host process beyond what the application already manages are required.

This guide covers everything you need to produce a working plugin from scratch. For the full interface contracts of each plugin type, see:

- [User Plugin](./user-plugin.md)
- [Notification Plugin](./notification-plugin.md)
- [Syntax Highlighter Plugin](./syntax-highlighter-plugin.md)

---

## Table of Contents

1. [Concepts](#concepts)
2. [Setting Up a Plugin Project](#setting-up-a-plugin-project)
3. [Plugin Lifecycle](#plugin-lifecycle)
4. [Registering Your Plugin (ServiceLoader SPI)](#registering-your-plugin-serviceloder-spi)
5. [Building and Deploying](#building-and-deploying)
6. [Packaging Checklist](#packaging-checklist)

---

## Concepts

### What is a plugin?

A plugin is any class that:

1. Is on the classpath of a JAR placed in the `plugins/` directory.
2. Implements one of the plugin interfaces that extend `Plugin`.
3. Is declared in the JAR's `META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin` file.

The application discovers plugins at startup using `ServiceLoader`, calls `initialize()` on each one, and then routes events to them throughout the session.

### Available plugin types

| Interface | Purpose |
|---|---|
| `UserPlugin` | Provide and validate the list of known users |
| `NotificationPlugin` | React to review lifecycle events (create, approve, comment …) |
| `SyntaxHighlighterPlugin` | Provide token-based syntax highlighting in the diff viewer |

More plugin types may be added in future versions. All will extend the base `Plugin` interface.

---

## Setting Up a Plugin Project

### Maven project structure

```
my-review-plugin/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/myplugin/
        │       └── MyUserPlugin.java
        └── resources/
            └── META-INF/
                └── services/
                    └── com.kalynx.serverlessreviewtool.plugin.Plugin
```

### `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-review-plugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <dependencies>
        <!-- Plugin interface – provided at runtime by the host application -->
        <dependency>
            <groupId>com.serverless</groupId>
            <artifactId>review-tool-plugin-interface</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <!-- Must be compatible with the host application's Java version -->
                    <release>25</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> **Important:** The `review-tool-plugin-interface` dependency must be declared with `scope=provided`. The interface JAR is already on the host application's classpath; bundling it again will cause `ClassCastException` at runtime.

### Obtaining the interface JAR

The interface JAR (`review-tool-plugin-interface-1.0.0.jar`) is distributed alongside the application. Install it into your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=review-tool-plugin-interface-1.0.0.jar \
  -DgroupId=com.serverless \
  -DartifactId=review-tool-plugin-interface \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

---

## Plugin Lifecycle

```
Application start
      │
      ▼
PluginRegistry.load()          ← JAR discovered, class loaded, instance created
      │
      ▼
Listeners attached             ← Application registers callbacks before init
      │
      ▼
Plugin.initialize()            ← YOUR code runs here – start watchers, connect to APIs, etc.
      │
      ▼
[Normal operation]             ← Plugin fires events / handles callbacks on demand
      │
      ▼
PluginManager.shutdown()       ← Application exits; clean up resources here if needed
```

### Key rules

- `initialize()` is called **once** on the EDT-adjacent startup thread — not on the Swing Event Dispatch Thread itself.
- Any long-running work (file watching, polling, HTTP connections) **must** be moved to a background daemon thread inside `initialize()` so the application does not block.
- The application guarantees that its listeners are registered **before** `initialize()` is called, so events fired during `initialize()` will be received correctly.

---

## Registering Your Plugin (ServiceLoader SPI)

The application discovers plugins via `java.util.ServiceLoader<Plugin>`. To register your plugin, create the file:

```
src/main/resources/META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin
```

List every plugin class your JAR provides, one fully-qualified class name per line:

```
com.example.myplugin.MyUserPlugin
com.example.myplugin.MyNotificationPlugin
```

> A single JAR can provide multiple plugin implementations across different plugin types. Just list them all in the same services file.

### No-args constructor required

`ServiceLoader` instantiates plugins via reflection using the **no-argument constructor**. Any initialisation that requires parameters must be deferred to `initialize()` or performed through system properties.

---

## Building and Deploying

### Build

```bash
mvn clean package
```

This produces `target/my-review-plugin-1.0.0.jar`.

### Deploy

Copy the JAR into the application's `plugins/` directory:

```bash
cp target/my-review-plugin-1.0.0.jar /path/to/serverless-review-tool/plugins/
```

Any **transitive dependencies** your plugin needs that are not already provided by the host application must also be placed in the `plugins/` directory. The application adds every JAR in that directory to the plugin class loader.

### Passing configuration

Use **system properties** (set via `-D` flags on the host application's JVM command line) to pass configuration into your plugin:

```java
@Override
public void initialize() {
    String apiUrl = System.getProperty("myplugin.api.url", "https://default.example.com");
    // ...
}
```

---

## Packaging Checklist

Before distributing your plugin JAR, verify the following:

- [ ] The JAR does **not** bundle `review-tool-plugin-interface-*.jar` (use `scope=provided`).
- [ ] `META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin` is present and lists every plugin class.
- [ ] Each plugin class has a **public no-args constructor** (explicit or implicit).
- [ ] Long-running work in `initialize()` runs on a **daemon thread** so the application does not hang on shutdown.
- [ ] The JAR was compiled with `--release 25` (or the version shipped with the application).
- [ ] All transitive runtime dependencies are co-located in the `plugins/` directory.

