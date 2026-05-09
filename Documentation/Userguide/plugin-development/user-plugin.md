# UserPlugin Interface Reference
A `UserPlugin` tells the application which users exist and validates login attempts. When a `UserPlugin` is present, the login screen is shown at startup and users must authenticate before using the tool.
See the [General Plugin Guide](./README.md) for project setup and ServiceLoader registration steps.
---
## Table of Contents
1. [Overview](#overview)
2. [Interface Declaration](#interface-declaration)
3. [Method Reference](#method-reference)
4. [Event Notification System](#event-notification-system)
5. [Complete Example](#complete-example)
6. [Behaviour Contract](#behaviour-contract)
---
## Overview
```
UserPlugin  extends  Notifier<String, UserPlugin.NotificationType>
            implements  Plugin
```
`UserPlugin` is an **abstract class** (not an interface) so it inherits the built-in notification dispatch machinery from `Notifier`. Your implementation extends `UserPlugin`, implements the two abstract methods (`initialize` and `validateUser`), and calls `notifyListeners(...)` whenever the user roster changes.
---
## Interface Declaration
```java
package com.kalynx.serverlessreviewtool.plugin;
public abstract class UserPlugin
        extends Notifier<String, UserPlugin.NotificationType>
        implements Plugin {
    public enum NotificationType {
        USER_ADDED,
        USER_REMOVED
    }
    /** Called once by the application immediately after the plugin is loaded. */
    @Override
    public abstract void initialize();
    /**
     * Validates a user attempting to log in.
     *
     * @param user             the username entered on the login screen
     * @param validationString a secondary credential (password, token, email, ...)
     * @return true if the user should be allowed to log in, false otherwise
     */
    public abstract boolean validateUser(String user, String validationString);
}
```
### Notifier (inherited)
```java
// Dispatch an event to all registered listeners — call this from YOUR code
protected final void notifyListeners(NotificationType type, String... values);
// Listener management — called by the APPLICATION, not by plugin code
public void addListener(NotificationType type, Consumer<String[]> listener);
public void removeListener(NotificationType type, Consumer<String[]> listener);
```
---
## Method Reference
### `void initialize()`
Called **once** after all application listeners have been attached. Use this method to:
- Load the initial user roster from your data source.
- Start background watcher threads (file watch, HTTP polling, WebSocket, etc.).
- Fire an initial `USER_ADDED` event for every user currently known.
The application guarantees its listeners are registered **before** `initialize()` is called, so events fired during `initialize()` will always be received.
> Do not block `initialize()` for more than a few milliseconds. Long-running work must be moved to a daemon background thread.
---
### `boolean validateUser(String user, String validationString)`
Called on the Swing Event Dispatch Thread when a user submits the login form. The meaning of `validationString` is entirely up to your plugin — it may be a password, an email address, a one-time token, or left empty.
| Return | Meaning |
|---|---|
| `true`  | Credentials are valid; the user is logged in and the main application opens. |
| `false` | Credentials are invalid; the login screen displays an error and remains visible. |
**Do not perform blocking network calls here.** Pre-fetch and cache validation data in `initialize()` and answer synchronously from the cache.
---
## Event Notification System
### `NotificationType.USER_ADDED`
Fire whenever one or more users join the roster:
```java
notifyListeners(NotificationType.USER_ADDED, "alice", "bob");
```
The application responds by adding the usernames to its internal `UserManager` so they appear as selectable reviewers.
### `NotificationType.USER_REMOVED`
Fire whenever one or more users leave the roster:
```java
notifyListeners(NotificationType.USER_REMOVED, "charlie");
```
The application responds by removing them from `UserManager` and automatically logging out any currently-logged-in user whose username appears in the list.
### Initial population
Always fire `USER_ADDED` for every currently-known user **inside `initialize()`**. If you skip this, the reviewer list starts empty and logins will all fail.
---
## Complete Example
The example below backs `UserPlugin` with a plain-text file where each line is `username,password` — matching the format used by the built-in `DefaultUserPlugin`.
```java
package com.example.myplugin;
import com.kalynx.serverlessreviewtool.plugin.UserPlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
public class FileBackedUserPlugin extends UserPlugin {
    private static final String FILE_PROP = "myplugin.users.file";
    private static final String DEFAULT_FILE = "users.txt";
    private final Map<String, String> knownUsers = new ConcurrentHashMap<>();
    @Override
    public void initialize() {
        Path filePath = Path.of(System.getProperty(FILE_PROP, DEFAULT_FILE)).toAbsolutePath();
        Map<String, String> initial = readFile(filePath);
        knownUsers.putAll(initial);
        if (!initial.isEmpty()) {
            notifyListeners(NotificationType.USER_ADDED, initial.keySet().toArray(String[]::new));
        }
    }
    @Override
    public boolean validateUser(String user, String validationString) {
        return user != null && knownUsers.contains(user.trim());
    }
    private Set<String> readFile(Path path) {
        if (!Files.exists(path)) return Set.of();
        try {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            System.err.println("FileBackedUserPlugin: cannot read " + path + ": " + e.getMessage());
            return Set.of();
        }
    }
}
```
**`META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin`**:
```
com.example.myplugin.FileBackedUserPlugin
```
---
## Behaviour Contract
| Requirement | Consequence if violated |
|---|---|
| Fire `USER_ADDED` for all known users inside `initialize()` | Reviewer list is empty; nobody can log in |
| `validateUser()` must not block | Login screen freezes |
| Background threads must be daemon threads | JVM hangs after the application window closes |
| Do not call `addListener()` yourself | Duplicate events delivered to the application |

