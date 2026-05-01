package com.kalynx.serverlessreviewtool.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ProcessUtils provides a fluent callback-based API for running external processes asynchronously.
 */
public class ProcessUtils {

    /**
     * Starts building a process execution with the given command arguments.
     *
     * @param args command and arguments
     * @return builder for configuring success callback
     */
    public static RunProcess runProcess(String... args) {
        return new RunProcess(Arrays.asList(args));
    }

    /**
     * RunProcess configures the process and requires a success callback.
     */
    public static class RunProcess {

        private final List<String> command;
        private Path workingDirectory;
        private String stdin;
        private Duration timeout = Duration.ofSeconds(60);

        private RunProcess(List<String> command) {
            this.command = command;
        }

        /**
         * Sets the working directory for process execution.
         *
         * @param path working directory
         * @return this builder
         */
        public RunProcess workingDirectory(Path path) {
            this.workingDirectory = path;
            return this;
        }

        /**
         * Sets stdin content to pipe to the process.
         *
         * @param content UTF-8 stdin content
         * @return this builder
         */
        public RunProcess stdin(String content) {
            this.stdin = content;
            return this;
        }

        /**
         * Sets the maximum execution duration.
         *
         * @param duration timeout duration
         * @return this builder
         */
        public RunProcess timeout(Duration duration) {
            this.timeout = duration;
            return this;
        }

        /**
         * Registers the success callback and advances to failure handling.
         *
         * @param onSuccess callback receiving stdout when exit code is zero
         * @return builder for configuring failure callback
         */
        public HandleFailure onSuccess(Consumer<String> onSuccess) {
            return new HandleFailure(command, workingDirectory, stdin, timeout, onSuccess);
        }
    }

    /**
     * HandleFailure configures the failure callback.
     */
    public static class HandleFailure {

        private final List<String> command;
        private final Path workingDirectory;
        private final String stdin;
        private final Duration timeout;
        private final Consumer<String> onSuccess;

        private HandleFailure(List<String> command, Path workingDirectory, String stdin,
                              Duration timeout, Consumer<String> onSuccess) {
            this.command = command;
            this.workingDirectory = workingDirectory;
            this.stdin = stdin;
            this.timeout = timeout;
            this.onSuccess = onSuccess;
        }

        /**
         * Registers the failure callback and advances to optional timeout handling.
         *
         * @param onFailure callback receiving stderr when exit code is non-zero
         * @return builder for configuring optional timeout and execution
         */
        public HandleTimeout onFailure(Consumer<String> onFailure) {
            return new HandleTimeout(command, workingDirectory, stdin, timeout, onSuccess, onFailure);
        }
    }

    /**
     * HandleTimeout configures optional timeout callback and triggers execution.
     */
    public static class HandleTimeout {

        private final List<String> command;
        private final Path workingDirectory;
        private final String stdin;
        private final Duration timeout;
        private final Consumer<String> onSuccess;
        private final Consumer<String> onFailure;
        private Runnable onTimeout;

        private HandleTimeout(List<String> command, Path workingDirectory, String stdin,
                              Duration timeout, Consumer<String> onSuccess, Consumer<String> onFailure) {
            this.command = command;
            this.workingDirectory = workingDirectory;
            this.stdin = stdin;
            this.timeout = timeout;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

        /**
         * Registers an optional timeout callback.
         *
         * @param onTimeout callback invoked when process exceeds timeout
         * @return this builder
         */
        public HandleTimeout onTimeout(Runnable onTimeout) {
            this.onTimeout = onTimeout;
            return this;
        }

        /**
         * Executes the process asynchronously with configured callbacks.
         */
        public void runAsync() {
            CompletableFuture.runAsync(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(command);
                    if (workingDirectory != null) {
                        pb.directory(workingDirectory.toFile());
                    }
                    Process process = pb.start();

                    if (stdin != null) {
                        CompletableFuture.runAsync(() -> {
                            try {
                                process.getOutputStream().write(stdin.getBytes(StandardCharsets.UTF_8));
                                process.getOutputStream().close();
                            } catch (IOException ignored) {
                            }
                        });
                    } else {
                        process.getOutputStream().close();
                    }

                    CompletableFuture<String> stdoutFuture = readAsync(process.getInputStream());
                    CompletableFuture<String> stderrFuture = readAsync(process.getErrorStream());

                    boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);

                    if (!finished) {
                        process.destroyForcibly();
                        process.waitFor();
                        if (onTimeout != null) {
                            onTimeout.run();
                        }
                        return;
                    }

                    String stdout = stdoutFuture.join();
                    String stderr = stderrFuture.join();
                    int exitCode = process.exitValue();

                    if (exitCode == 0) {
                        onSuccess.accept(stdout);
                    } else {
                        onFailure.accept(stderr);
                    }
                } catch (IOException | InterruptedException e) {
                    onFailure.accept("Process execution failed: " + e.getMessage());
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        private CompletableFuture<String> readAsync(InputStream stream) {
            return CompletableFuture.supplyAsync(() -> {
                try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    return out.toString(StandardCharsets.UTF_8);
                } catch (IOException e) {
                    return "";
                }
            });
        }
    }
}