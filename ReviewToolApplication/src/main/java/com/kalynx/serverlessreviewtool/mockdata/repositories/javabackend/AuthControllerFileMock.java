package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class AuthControllerFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/main/java/com/example/controller/AuthController.java";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(16);

        for (int i = 0; i < commitCount; i++) {
            String content = generateContentForIteration(i);
            Path filePath = repoPath.resolve(FILE_PATH);
            Files.writeString(filePath, content);
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generateContentForIteration(int iteration) {
        StringBuilder content = new StringBuilder();

        content.append("package com.example.controller;\n\n");

        if (iteration >= 1) {
            content.append("import org.springframework.web.bind.annotation.*;\n");
        }
        if (iteration >= 2) {
            content.append("import org.springframework.http.ResponseEntity;\n");
            content.append("import org.springframework.http.HttpStatus;\n");
        }
        if (iteration >= 4 && iteration < 20) {
            content.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        }
        if (iteration >= 4) {
            content.append("import com.example.service.AuthService;\n");
        }
        if (iteration >= 6) {
            content.append("import com.example.dto.LoginRequest;\n");
            content.append("import com.example.dto.LoginResponse;\n");
        }
        if (iteration >= 8 && iteration < 15) {
            content.append("import javax.validation.Valid;\n");
        }
        if (iteration >= 15) {
            content.append("import jakarta.validation.Valid;\n");
        }
        if (iteration >= 10 && iteration < 24) {
            content.append("import org.slf4j.Logger;\n");
            content.append("import org.slf4j.LoggerFactory;\n");
        }
        if (iteration >= 20) {
            content.append("import lombok.RequiredArgsConstructor;\n");
        }
        if (iteration >= 24) {
            content.append("import lombok.extern.slf4j.Slf4j;\n");
        }
        if (iteration >= 16 && iteration < 19) {
            content.append("import org.springframework.validation.annotation.Validated;\n");
        }
        if (iteration >= 19) {
            content.append("import com.example.exception.AuthenticationException;\n");
        }
        if (iteration >= 27) {
            content.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
        }

        content.append("\n");

        if (iteration >= 1) {
            content.append("@RestController\n");
            content.append("@RequestMapping(\"/api/auth\")\n");
        }
        if (iteration >= 7 && iteration < 28) {
            content.append("@CrossOrigin(origins = \"*\")\n");
        }
        if (iteration >= 28) {
            content.append("@CrossOrigin(origins = \"${app.cors.allowed-origins}\")\n");
        }
        if (iteration >= 16 && iteration < 19) {
            content.append("@Validated\n");
        }
        if (iteration >= 20) {
            content.append("@RequiredArgsConstructor\n");
        }
        if (iteration >= 24) {
            content.append("@Slf4j\n");
        }
        content.append("public class AuthController {\n\n");

        if (iteration >= 10 && iteration < 24) {
            content.append("    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);\n\n");
        }

        if (iteration >= 4) {
            if (iteration >= 4 && iteration < 20) {
                content.append("    @Autowired\n");
                content.append("    private AuthService authService;\n\n");
            } else {
                content.append("    private final AuthService authService;\n\n");
            }
        }

        if (iteration >= 2) {
            content.append("    @PostMapping(\"/login\")\n");
            content.append("    public ResponseEntity");
            if (iteration >= 6) {
                content.append("<LoginResponse>");
            } else {
                content.append("<String>");
            }
            content.append(" login(");
            if (iteration >= 6) {
                if (iteration >= 8) {
                    content.append("@Valid ");
                }
                content.append("@RequestBody LoginRequest request");
            } else {
                content.append("@RequestBody String credentials");
            }
            content.append(") {\n");
            if (iteration >= 10 && iteration < 24) {
                content.append("        logger.info(\"Login attempt for user: {}\", ");
                content.append(iteration >= 6 ? "request.getUsername()" : "credentials");
                content.append(");\n");
            } else if (iteration >= 24) {
                content.append("        log.info(\"Login attempt for user: {}\", request.getUsername());\n");
            }
            if (iteration >= 5 || iteration >= 19) {
                content.append("        try {\n");
                content.append("            ");
            }
            if (iteration >= 6) {
                content.append("LoginResponse response = authService.authenticate(request);\n");
                if (iteration >= 5) {
                    content.append("            ");
                }
                if (iteration >= 11 && iteration < 24) {
                    content.append("logger.info(\"Login successful for user: {}\", request.getUsername());\n");
                    if (iteration >= 5) {
                        content.append("            ");
                    }
                } else if (iteration >= 24) {
                    content.append("log.info(\"Login successful for user: {}\", request.getUsername());\n");
                    if (iteration >= 5) {
                        content.append("            ");
                    }
                }
                content.append("return ResponseEntity.ok(response);\n");
            } else if (iteration >= 4) {
                content.append("String token = authService.authenticate(credentials);\n");
                if (iteration >= 5) {
                    content.append("            ");
                }
                content.append("return ResponseEntity.ok(token);\n");
            } else {
                content.append("return ResponseEntity.ok(\"Login successful\");\n");
            }
            if (iteration >= 5 && iteration < 19) {
                content.append("        } catch (Exception e) {\n");
                if (iteration >= 11 && iteration < 24) {
                    content.append("            logger.error(\"Login failed: {}\", e.getMessage());\n");
                } else if (iteration >= 24) {
                    content.append("            log.error(\"Login failed: {}\", e.getMessage());\n");
                }
                content.append("            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(");
                content.append(iteration >= 6 ? "null" : "\"Login failed\"");
                content.append(");\n");
                content.append("        }\n");
            } else if (iteration >= 19) {
                content.append("        } catch (AuthenticationException e) {\n");
                if (iteration >= 24) {
                    content.append("            log.error(\"Authentication failed: {}\", e.getMessage());\n");
                }
                content.append("            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)\n");
                content.append("                .body(new LoginResponse(null, e.getMessage()));\n");
                content.append("        } catch (Exception e) {\n");
                if (iteration >= 24) {
                    content.append("            log.error(\"Unexpected error during login: {}\", e.getMessage());\n");
                }
                content.append("            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)\n");
                content.append("                .body(new LoginResponse(null, \"Internal server error\"));\n");
                content.append("        }\n");
            }
            content.append("    }\n\n");
        }

        if (iteration >= 9) {
            content.append("    @PostMapping(\"/register\")\n");
            content.append("    public ResponseEntity<String> register(");
            if (iteration >= 15) {
                content.append("@Valid ");
            }
            content.append("@RequestBody LoginRequest request) {\n");
            if (iteration >= 11 && iteration < 24) {
                content.append("        logger.info(\"Registration attempt for user: {}\", request.getUsername());\n");
            } else if (iteration >= 24) {
                content.append("        log.info(\"Registration attempt for user: {}\", request.getUsername());\n");
            }
            content.append("        try {\n");
            content.append("            authService.register(request);\n");
            if (iteration >= 11 && iteration < 24) {
                content.append("            logger.info(\"Registration successful for user: {}\", request.getUsername());\n");
            } else if (iteration >= 24) {
                content.append("            log.info(\"Registration successful for user: {}\", request.getUsername());\n");
            }
            content.append("            return ResponseEntity.status(HttpStatus.CREATED).body(\"User registered successfully\");\n");
            content.append("        } catch (Exception e) {\n");
            if (iteration >= 11 && iteration < 24) {
                content.append("            logger.error(\"Registration failed: {}\", e.getMessage());\n");
            } else if (iteration >= 24) {
                content.append("            log.error(\"Registration failed: {}\", e.getMessage());\n");
            }
            content.append("            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());\n");
            content.append("        }\n");
            content.append("    }\n\n");
        }

        if (iteration >= 12) {
            content.append("    @PostMapping(\"/logout\")\n");
            if (iteration >= 27) {
                content.append("    @PreAuthorize(\"isAuthenticated()\")\n");
            }
            content.append("    public ResponseEntity<String> logout(@RequestHeader(\"Authorization\") String token) {\n");
            if (iteration >= 24) {
                content.append("        log.info(\"Logout request received\");\n");
            }
            content.append("        authService.logout(token);\n");
            content.append("        return ResponseEntity.ok(\"Logout successful\");\n");
            content.append("    }\n\n");
        }

        if (iteration >= 14 && iteration < 22) {
            content.append("    @PostMapping(\"/refresh\")\n");
            content.append("    public ResponseEntity<LoginResponse> refresh(@RequestHeader(\"Authorization\") String refreshToken) {\n");
            if (iteration >= 24) {
                content.append("        log.info(\"Token refresh request received\");\n");
            }
            content.append("        try {\n");
            content.append("            LoginResponse response = authService.refreshToken(refreshToken);\n");
            content.append("            return ResponseEntity.ok(response);\n");
            content.append("        } catch (Exception e) {\n");
            if (iteration >= 24) {
                content.append("            log.error(\"Token refresh failed: {}\", e.getMessage());\n");
            }
            content.append("            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);\n");
            content.append("        }\n");
            content.append("    }\n\n");
        } else if (iteration >= 22) {
            content.append("    @PostMapping(\"/refresh-token\")\n");
            if (iteration >= 27) {
                content.append("    @PreAuthorize(\"isAuthenticated()\")\n");
            }
            content.append("    public ResponseEntity<LoginResponse> refreshAccessToken(@RequestHeader(\"Authorization\") String refreshToken) {\n");
            if (iteration >= 24) {
                content.append("        log.info(\"Token refresh request received\");\n");
            }
            content.append("        try {\n");
            content.append("            LoginResponse response = authService.refreshAccessToken(refreshToken);\n");
            content.append("            return ResponseEntity.ok(response);\n");
            content.append("        } catch (AuthenticationException e) {\n");
            if (iteration >= 24) {
                content.append("            log.error(\"Token refresh failed: {}\", e.getMessage());\n");
            }
            content.append("            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)\n");
            content.append("                .body(new LoginResponse(null, e.getMessage()));\n");
            content.append("        }\n");
            content.append("    }\n\n");
        }

        if (iteration >= 25) {
            content.append("    @GetMapping(\"/validate\")\n");
            if (iteration >= 27) {
                content.append("    @PreAuthorize(\"isAuthenticated()\")\n");
            }
            content.append("    public ResponseEntity<Boolean> validateToken(@RequestHeader(\"Authorization\") String token) {\n");
            content.append("        boolean isValid = authService.validateToken(token);\n");
            content.append("        return ResponseEntity.ok(isValid);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 30) {
            content.append("    @PostMapping(\"/reset-password\")\n");
            content.append("    public ResponseEntity<String> resetPassword(@RequestBody String email) {\n");
            content.append("        log.info(\"Password reset requested for: {}\", email);\n");
            content.append("        try {\n");
            content.append("            authService.initiatePasswordReset(email);\n");
            content.append("            return ResponseEntity.ok(\"Password reset email sent\");\n");
            content.append("        } catch (Exception e) {\n");
            content.append("            log.error(\"Password reset failed: {}\", e.getMessage());\n");
            content.append("            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());\n");
            content.append("        }\n");
            content.append("    }\n");
        }

        content.append("}\n");

        return content.toString();
    }

    private static String getCommitMessage(int iteration) {
        return switch (iteration) {
            case 0 -> "feat: Create initial AuthController class";
            case 1 -> "feat: Add REST controller annotations";
            case 2 -> "feat: Add login endpoint with ResponseEntity";
            case 3 -> "refactor: Improve response structure";
            case 4 -> "feat: Integrate AuthService dependency";
            case 5 -> "fix: Add exception handling to login endpoint";
            case 6 -> "refactor: Use LoginRequest and LoginResponse DTOs";
            case 7 -> "feat: Add CORS support";
            case 8 -> "feat: Add validation annotations";
            case 9 -> "feat: Add register endpoint";
            case 10 -> "feat: Add logging support";
            case 11 -> "refactor: Add comprehensive logging";
            case 12 -> "feat: Add logout endpoint";
            case 13 -> "refactor: Improve error handling";
            case 14 -> "feat: Add token refresh endpoint";
            case 15 -> "refactor: Migrate from javax to jakarta validation";
            case 16 -> "feat: Add @Validated annotation";
            case 17 -> "refactor: Extract error response creation";
            case 18 -> "refactor: Remove @Validated annotation (redundant)";
            case 19 -> "feat: Add custom AuthenticationException handling";
            case 20 -> "refactor: Replace @Autowired with constructor injection";
            case 21 -> "refactor: Improve exception handling structure";
            case 22 -> "refactor: Rename /refresh to /refresh-token for clarity";
            case 23 -> "fix: Update refresh method to use new service method";
            case 24 -> "refactor: Migrate to Lombok logging";
            case 25 -> "feat: Add token validation endpoint";
            case 26 -> "refactor: Optimize controller imports";
            case 27 -> "feat: Add PreAuthorize security annotations";
            case 28 -> "refactor: Use configuration property for CORS origins";
            case 29 -> "refactor: Improve error response consistency";
            case 30 -> "feat: Add password reset endpoint";
            case 31 -> "refactor: Extract validation logic";
            case 32 -> "test: Add controller integration tests";
            case 33 -> "refactor: Improve code documentation";
            case 34 -> "fix: Handle malformed authorization headers";
            case 35 -> "perf: Add caching headers to responses";
            case 36 -> "refactor: Standardize error messages";
            case 37 -> "feat: Add rate limiting annotations";
            case 38 -> "refactor: Final code cleanup";
            case 39 -> "docs: Add API documentation annotations";
            case 40 -> "refactor: Improve null safety";
            default -> "refactor: Code improvements iteration " + (iteration + 1);
        };
    }
}

