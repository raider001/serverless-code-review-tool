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
import java.util.function.Function;

/**
 * ProcessUtils provides a fluent callback-based API for running external processes asynchronously.
 *
 * <p>Supports generic result types through transformation functions, allowing onSuccess callbacks
 * to receive parsed/transformed objects instead of raw strings.
 */
public class ProcessUtils {

    /**
     * Starts building a process execution with the given command arguments.
     *
     * @param args command and arguments
     * @return builder for configuring success callback (returns String by default)
     */
    public static RunProcess<String> runProcess(String... args) {
        return new RunProcess<>(Arrays.asList(args), Function.identity());
    }

    /**
     * RunProcess configures the process and requires a success callback.
     *
     * @param <T> the type of result passed to onSuccess callback
     */
    public static class RunProcess<T> {

        private final List<String> command;
        private final Function<String, T> resultMapper;
        private Path workingDirectory;
        private String stdin;
        private Duration timeout = Duration.ofSeconds(60);

        private RunProcess(List<String> command, Function<String, T> resultMapper) {
            this.command = command;
            this.resultMapper = resultMapper;
        }

        /**
         * Sets the working directory for process execution.
         *
         * @param path working directory
         * @return this builder
         */
        public RunProcess<T> workingDirectory(Path path) {
            this.workingDirectory = path;
            return this;
        }

        /**
         * Sets stdin content to pipe to the process.
         *
         * @param content UTF-8 stdin content
         * @return this builder
         */
        public RunProcess<T> stdin(String content) {
            this.stdin = content;
            return this;
        }

        /**
         * Sets the maximum execution duration.
         *
         * @param duration timeout duration
         * @return this builder
         */
        public RunProcess<T> timeout(Duration duration) {
            this.timeout = duration;
            return this;
        }

        /**
         * Transforms the stdout result to a different type.
         *
         * @param mapper function to transform result to desired type
         * @param <R> the new result type
         * @return new builder with transformed result type
         */
        public <R> RunProcess<R> map(Function<T, R> mapper) {
            Function<String, R> composedMapper = this.resultMapper.andThen(mapper);
            RunProcess<R> newBuilder = new RunProcess<>(command, composedMapper);
            newBuilder.workingDirectory = this.workingDirectory;
            newBuilder.stdin = this.stdin;
            newBuilder.timeout = this.timeout;
            return newBuilder;
        }

        /**
         * Registers the success callback and advances to failure handling.
         *
         * @param onSuccess callback receiving transformed result when exit code is zero
         * @return builder for configuring failure callback
         */
        public HandleFailure<T> onSuccess(Consumer<T> onSuccess) {
            return new HandleFailure<>(command, workingDirectory, stdin, timeout, resultMapper, onSuccess);
        }
    }

    /**
     * HandleFailure configures the failure callback.
     *
     * @param <T> the type of result passed to onSuccess callback
     */
    public static class HandleFailure<T> {

        private final List<String> command;
        private final Path workingDirectory;
        private final String stdin;
        private final Duration timeout;
        private final Function<String, T> resultMapper;
        private final Consumer<T> onSuccess;

        private HandleFailure(List<String> command, Path workingDirectory, String stdin,
                              Duration timeout, Function<String, T> resultMapper, Consumer<T> onSuccess) {
            this.command = command;
            this.workingDirectory = workingDirectory;
            this.stdin = stdin;
            this.timeout = timeout;
            this.resultMapper = resultMapper;
            this.onSuccess = onSuccess;
        }

        /**
         * Registers the failure callback and advances to optional timeout handling.
         *
         * @param onFailure callback receiving stderr when exit code is non-zero
         * @return builder for configuring optional timeout and execution
         */
        public HandleTimeout<T> onFailure(Consumer<String> onFailure) {
            return new HandleTimeout<>(command, workingDirectory, stdin, timeout, resultMapper, onSuccess, onFailure);
        }
    }

    /**
     * HandleTimeout configures optional timeout callback and triggers execution.
     *
     * @param <T> the type of result passed to onSuccess callback
     */
    public static class HandleTimeout<T> {

        private final List<String> command;
        private final Path workingDirectory;
        private final String stdin;
        private final Duration timeout;
        private final Function<String, T> resultMapper;
        private final Consumer<T> onSuccess;
        private final Consumer<String> onFailure;
        private Runnable onTimeout;

        private HandleTimeout(List<String> command, Path workingDirectory, String stdin,
                              Duration timeout, Function<String, T> resultMapper,
                              Consumer<T> onSuccess, Consumer<String> onFailure) {
            this.command = command;
            this.workingDirectory = workingDirectory;
            this.stdin = stdin;
            this.timeout = timeout;
            this.resultMapper = resultMapper;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

        /**
         * Registers an optional timeout callback.
         *
         * @param onTimeout callback invoked when process exceeds timeout
         * @return this builder
         */
        public HandleTimeout<T> onTimeout(Runnable onTimeout) {
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
                        try {
                            T result = resultMapper.apply(stdout);
                            onSuccess.accept(result);
                        } catch (Exception e) {
                            onFailure.accept("Result transformation failed: " + e.getMessage());
                        }
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

