# NotificationPlugin Interface Reference
A `NotificationPlugin` lets you react to review lifecycle events — such as a review being created, a reviewer joining, or an approval being submitted. Use this plugin type to integrate the tool with external systems like Slack, email, JIRA, or a custom webhook service.
See the [General Plugin Guide](./README.md) for project setup and ServiceLoader registration steps.
---
## Table of Contents
1. [Overview](#overview)
2. [Interface Declaration](#interface-declaration)
3. [Method Reference](#method-reference)
4. [Complete Example](#complete-example)
5. [Behaviour Contract](#behaviour-contract)
---
## Overview
```
NotificationPlugin  implements  Plugin
```
`NotificationPlugin` is an **abstract class** with every event method defaulting to a no-op. You only need to override the events your integration cares about — there is no requirement to implement all of them.
The application calls these methods **fire-and-forget**: exceptions thrown by your implementation are caught and logged without affecting the core review workflow.
---
## Interface Declaration
```java
package com.kalynx.serverlessreviewtool.plugin;
public abstract class NotificationPlugin implements Plugin {
    /** Called once by the application immediately after the plugin is loaded. Default: no-op. */
    @Override
    public void initialize() {}
    /** Called when a new review is created. */
    public void onReviewCreated(String reviewId, String author, String title) {}
    /** Called when a reviewer joins a review. */
    public void onReviewerJoined(String reviewId, String reviewer) {}
    /** Called when a reviewer leaves a review. */
    public void onReviewerLeft(String reviewId, String reviewer) {}
    /**
     * Called when a comment is added to a review.
     *
     * @param filePath   the file the comment is on, or null for general (non-inline) comments
     * @param lineNumber the line number of an inline comment, or -1 if not line-specific
     */
    public void onCommentAdded(String reviewId, String author, String filePath, int lineNumber) {}
    /** Called when a reviewer approves a review. */
    public void onReviewApproved(String reviewId, String reviewer) {}
    /** Called when a reviewer requests changes on a review. */
    public void onChangesRequested(String reviewId, String reviewer) {}
}
```
---
## Method Reference
### `void initialize()`
Called **once** after the plugin is loaded. Override this to set up any connections, thread pools, or configuration your plugin needs before events start arriving. The default implementation is a no-op.
---
### `void onReviewCreated(String reviewId, String author, String title)`
Fired when a user creates a new review.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review (UUID v7) |
| `author` | Display name of the user who created the review |
| `title` | Title string entered by the author |
---
### `void onReviewerJoined(String reviewId, String reviewer)`
Fired when a user joins an existing review as a reviewer.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review |
| `reviewer` | Display name of the joining reviewer |
---
### `void onReviewerLeft(String reviewId, String reviewer)`
Fired when a reviewer leaves a review.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review |
| `reviewer` | Display name of the leaving reviewer |
---
### `void onCommentAdded(String reviewId, String author, String filePath, int lineNumber)`
Fired when any comment is added to a review — both general comments and inline code comments.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review |
| `author` | Display name of the comment author |
| `filePath` | Relative path of the commented file, or `null` for general comments |
| `lineNumber` | Line number for inline comments, or `-1` if not line-specific |
---
### `void onReviewApproved(String reviewId, String reviewer)`
Fired when a reviewer submits an approval decision.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review |
| `reviewer` | Display name of the approving reviewer |
---
### `void onChangesRequested(String reviewId, String reviewer)`
Fired when a reviewer submits a "request changes" decision.
| Parameter | Description |
|---|---|
| `reviewId` | Unique identifier for the review |
| `reviewer` | Display name of the reviewer requesting changes |
---
## Complete Example
The example below posts a message to a Slack-compatible incoming webhook for each relevant event. Only the events that matter to the integration are overridden.
```java
package com.example.myplugin;
import com.kalynx.serverlessreviewtool.plugin.NotificationPlugin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class SlackNotificationPlugin extends NotificationPlugin {
    private static final String WEBHOOK_PROP = "myplugin.slack.webhook";
    private HttpClient httpClient;
    private ExecutorService executor;
    private String webhookUrl;
    @Override
    public void initialize() {
        webhookUrl = System.getProperty(WEBHOOK_PROP, "");
        if (webhookUrl.isBlank()) {
            System.err.println("SlackNotificationPlugin: no webhook URL configured via -D" + WEBHOOK_PROP);
            return;
        }
        httpClient = HttpClient.newHttpClient();
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "slack-notifier");
            t.setDaemon(true);
            return t;
        });
    }
    @Override
    public void onReviewCreated(String reviewId, String author, String title) {
        post(":pencil: *" + author + "* opened review *" + title + "* (`" + reviewId + "`)");
    }
    @Override
    public void onReviewApproved(String reviewId, String reviewer) {
        post(":white_check_mark: *" + reviewer + "* approved review `" + reviewId + "`");
    }
    @Override
    public void onChangesRequested(String reviewId, String reviewer) {
        post(":x: *" + reviewer + "* requested changes on review `" + reviewId + "`");
    }
    @Override
    public void onCommentAdded(String reviewId, String author, String filePath, int lineNumber) {
        String location = filePath != null
            ? filePath + (lineNumber >= 0 ? ":" + lineNumber : "")
            : "general";
        post(":speech_balloon: *" + author + "* commented on `" + location + "` in review `" + reviewId + "`");
    }
    private void post(String message) {
        if (webhookUrl.isBlank() || executor == null) return;
        String body = "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}";
        executor.submit(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                System.err.println("SlackNotificationPlugin: failed to post: " + e.getMessage());
            }
        });
    }
}
```
**`META-INF/services/com.kalynx.serverlessreviewtool.plugin.Plugin`**:
```
com.example.myplugin.SlackNotificationPlugin
```
**Configure the webhook URL at launch:**
```bash
java -Dmyplugin.slack.webhook=https://hooks.slack.com/services/XXX/YYY/ZZZ -jar serverless-review-tool.jar
```
---
## Behaviour Contract
| Requirement | Consequence if violated |
|---|---|
| Do not throw unchecked exceptions from event methods | Exceptions are caught and logged, but consider them a sign of a bug in your plugin |
| Do not block event methods | Blocks the thread that drives review state changes; use an async executor as shown above |
| Background threads must be daemon threads | JVM hangs after the application window closes |
| `initialize()` should be idempotent if possible | The application calls it exactly once, but defensive coding prevents issues if the contract changes |
