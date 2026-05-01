package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class UserServiceFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/main/java/com/example/service/UserService.java";
    private static final Random random = new Random();

    public static void create(Path repoPath) throws Exception {
        int commitCount = 15 + random.nextInt(26);

        for (int i = 0; i < commitCount; i++) {
            String content = generateContentForIteration(i);
            Path filePath = repoPath.resolve(FILE_PATH);
            Files.writeString(filePath, content);
            commitFile(repoPath, FILE_PATH, getCommitMessage(i));
        }
    }

    private static String generateContentForIteration(int iteration) {
        StringBuilder content = new StringBuilder();

        content.append("package com.example.service;\n\n");

        if (iteration >= 1) {
            content.append("import com.example.repository.UserRepository;\n");
            content.append("import com.example.model.User;\n");
        }
        if (iteration >= 3) {
            content.append("import org.springframework.stereotype.Service;\n");
            if (iteration < 18) {
                content.append("import org.springframework.beans.factory.annotation.Autowired;\n");
            }
        }
        if (iteration >= 5) {
            content.append("import java.util.List;\n");
            content.append("import java.util.Optional;\n");
        }
        if (iteration >= 8 && iteration < 25) {
            content.append("import org.slf4j.Logger;\n");
            content.append("import org.slf4j.LoggerFactory;\n");
        }
        if (iteration >= 25) {
            content.append("import lombok.extern.slf4j.Slf4j;\n");
            content.append("import lombok.RequiredArgsConstructor;\n");
        }
        if (iteration >= 16) {
            content.append("import org.springframework.transaction.annotation.Transactional;\n");
        }
        if (iteration >= 20) {
            content.append("import com.example.exception.UserNotFoundException;\n");
            content.append("import com.example.exception.ValidationException;\n");
        }
        if (iteration >= 28) {
            content.append("import org.springframework.cache.annotation.Cacheable;\n");
            content.append("import org.springframework.cache.annotation.CacheEvict;\n");
        }

        content.append("\n");

        if (iteration >= 3) {
            content.append("@Service\n");
        }
        if (iteration >= 25) {
            content.append("@Slf4j\n");
            content.append("@RequiredArgsConstructor\n");
        }
        if (iteration >= 16 && iteration < 22) {
            content.append("@Transactional\n");
        }
        content.append("public class UserService {\n\n");

        if (iteration >= 8 && iteration < 25) {
            content.append("    private static final Logger logger = LoggerFactory.getLogger(UserService.class);\n\n");
        }

        if (iteration >= 1) {
            if (iteration >= 3 && iteration < 18) {
                content.append("    @Autowired\n");
            }
            if (iteration >= 18) {
                content.append("    private final UserRepository userRepository;\n\n");
            } else {
                content.append("    private UserRepository userRepository;\n\n");
            }
        }

        if (iteration >= 2) {
            if (iteration >= 22) {
                content.append("    @Transactional\n");
                if (iteration >= 28) {
                    content.append("    @CacheEvict(value = \"users\", allEntries = true)\n");
                }
            }
            content.append("    public User createUser(User user) {\n");
            if (iteration >= 9 && iteration < 25) {
                content.append("        logger.info(\"Creating user: {}\", user.getUsername());\n");
            } else if (iteration >= 25) {
                content.append("        log.info(\"Creating user: {}\", user.getUsername());\n");
            }
            if (iteration >= 6 && iteration < 20) {
                content.append("        if (user.getUsername() == null || user.getUsername().isEmpty()) {\n");
                content.append("            throw new IllegalArgumentException(\"Username cannot be empty\");\n");
                content.append("        }\n");
            } else if (iteration >= 20) {
                content.append("        validateUser(user);\n");
            }
            if (iteration >= 24) {
                content.append("        if (userRepository.existsByUsername(user.getUsername())) {\n");
                content.append("            throw new ValidationException(\"Username already exists: \" + user.getUsername());\n");
                content.append("        }\n");
            }
            content.append("        return userRepository.save(user);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 4 && iteration < 19) {
            content.append("    public User getUserById(Long id) {\n");
            if (iteration >= 9) {
                content.append("        logger.debug(\"Fetching user with id: {}\", id);\n");
            }
            if (iteration >= 5) {
                content.append("        return userRepository.findById(id)\n");
                content.append("            .orElseThrow(() -> new RuntimeException(\"User not found\"));\n");
            } else {
                content.append("        return userRepository.findById(id);\n");
            }
            content.append("    }\n\n");
        } else if (iteration >= 19) {
            if (iteration >= 28) {
                content.append("    @Cacheable(value = \"users\", key = \"#id\")\n");
            }
            content.append("    public User findById(Long id) {\n");
            if (iteration >= 25) {
                content.append("        log.debug(\"Fetching user with id: {}\", id);\n");
            } else {
                content.append("        logger.debug(\"Fetching user with id: {}\", id);\n");
            }
            if (iteration >= 20) {
                content.append("        return userRepository.findById(id)\n");
                content.append("            .orElseThrow(() -> new UserNotFoundException(\"User not found with id: \" + id));\n");
            } else {
                content.append("        return userRepository.findById(id)\n");
                content.append("            .orElseThrow(() -> new RuntimeException(\"User not found\"));\n");
            }
            content.append("    }\n\n");
        }

        if (iteration >= 5 && iteration < 19) {
            content.append("    public List<User> getAllUsers() {\n");
            if (iteration >= 9) {
                content.append("        logger.debug(\"Fetching all users\");\n");
            }
            content.append("        return userRepository.findAll();\n");
            content.append("    }\n\n");
        } else if (iteration >= 19) {
            if (iteration >= 28) {
                content.append("    @Cacheable(value = \"users\")\n");
            }
            content.append("    public List<User> findAll() {\n");
            if (iteration >= 25) {
                content.append("        log.debug(\"Fetching all users\");\n");
            } else {
                content.append("        logger.debug(\"Fetching all users\");\n");
            }
            content.append("        return userRepository.findAll();\n");
            content.append("    }\n\n");
        }

        if (iteration >= 7) {
            if (iteration >= 22) {
                content.append("    @Transactional\n");
                if (iteration >= 28) {
                    content.append("    @CacheEvict(value = \"users\", key = \"#id\")\n");
                }
            }
            content.append("    public User updateUser(Long id, User updatedUser) {\n");
            if (iteration >= 9 && iteration < 25) {
                content.append("        logger.info(\"Updating user with id: {}\", id);\n");
            } else if (iteration >= 25) {
                content.append("        log.info(\"Updating user with id: {}\", id);\n");
            }
            if (iteration >= 19) {
                content.append("        User user = findById(id);\n");
            } else {
                content.append("        User user = getUserById(id);\n");
            }
            if (iteration >= 21) {
                content.append("        validateUser(updatedUser);\n");
            }
            content.append("        user.setUsername(updatedUser.getUsername());\n");
            content.append("        user.setEmail(updatedUser.getEmail());\n");
            if (iteration >= 10) {
                content.append("        user.setUpdatedAt(java.time.LocalDateTime.now());\n");
            }
            content.append("        return userRepository.save(user);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 8) {
            if (iteration >= 22) {
                content.append("    @Transactional\n");
                if (iteration >= 28) {
                    content.append("    @CacheEvict(value = \"users\", key = \"#id\")\n");
                }
            }
            content.append("    public void deleteUser(Long id) {\n");
            if (iteration >= 25) {
                content.append("        log.info(\"Deleting user with id: {}\", id);\n");
            } else {
                content.append("        logger.info(\"Deleting user with id: {}\", id);\n");
            }
            if (iteration >= 11 && iteration < 20) {
                content.append("        if (!userRepository.existsById(id)) {\n");
                content.append("            throw new RuntimeException(\"User not found\");\n");
                content.append("        }\n");
            } else if (iteration >= 20) {
                content.append("        if (!userRepository.existsById(id)) {\n");
                content.append("            throw new UserNotFoundException(\"User not found with id: \" + id);\n");
                content.append("        }\n");
            }
            content.append("        userRepository.deleteById(id);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 12) {
            content.append("    public User findByUsername(String username) {\n");
            if (iteration >= 25) {
                content.append("        log.debug(\"Finding user by username: {}\", username);\n");
            } else {
                content.append("        logger.debug(\"Finding user by username: {}\", username);\n");
            }
            if (iteration >= 20) {
                content.append("        return userRepository.findByUsername(username)\n");
                content.append("            .orElseThrow(() -> new UserNotFoundException(\"User not found: \" + username));\n");
            } else {
                content.append("        return userRepository.findByUsername(username)\n");
                content.append("            .orElseThrow(() -> new RuntimeException(\"User not found: \" + username));\n");
            }
            content.append("    }\n\n");
        }

        if (iteration >= 14 && iteration < 30) {
            content.append("    public boolean existsByEmail(String email) {\n");
            content.append("        return userRepository.existsByEmail(email);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 20) {
            content.append("    private void validateUser(User user) {\n");
            content.append("        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {\n");
            content.append("            throw new ValidationException(\"Username cannot be empty\");\n");
            content.append("        }\n");
            if (iteration >= 23) {
                content.append("        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {\n");
                content.append("            throw new ValidationException(\"Username must be between 3 and 50 characters\");\n");
                content.append("        }\n");
            }
            if (iteration >= 21) {
                content.append("        if (user.getEmail() == null || !user.getEmail().contains(\"@\")) {\n");
                content.append("            throw new ValidationException(\"Invalid email address\");\n");
                content.append("        }\n");
            }
            content.append("    }\n\n");
        }

        if (iteration >= 27) {
            content.append("    public List<User> searchUsers(String searchTerm) {\n");
            if (iteration >= 25) {
                content.append("        log.debug(\"Searching users with term: {}\", searchTerm);\n");
            } else {
                content.append("        logger.debug(\"Searching users with term: {}\", searchTerm);\n");
            }
            content.append("        return userRepository.searchByUsernameOrEmail(searchTerm);\n");
            content.append("    }\n\n");
        }

        if (iteration >= 30) {
            content.append("    @Transactional(readOnly = true)\n");
            content.append("    public long countActiveUsers() {\n");
            content.append("        return userRepository.countByActiveTrue();\n");
            content.append("    }\n\n");
        }

        content.append("}\n");

        return content.toString();
    }

    private static String getCommitMessage(int iteration) {
        return switch (iteration) {
            case 0 -> "feat: Create initial UserService class";
            case 1 -> "feat: Add UserRepository dependency";
            case 2 -> "feat: Implement createUser method";
            case 3 -> "refactor: Add Spring @Service annotation";
            case 4 -> "feat: Add getUserById method";
            case 5 -> "feat: Add getAllUsers method and proper Optional handling";
            case 6 -> "fix: Add validation for empty username in createUser";
            case 7 -> "feat: Implement updateUser method";
            case 8 -> "feat: Add logging support and deleteUser method";
            case 9 -> "refactor: Add comprehensive logging to all methods";
            case 10 -> "feat: Add timestamp update in updateUser";
            case 11 -> "fix: Add existence check in deleteUser";
            case 12 -> "feat: Add findByUsername method";
            case 13 -> "refactor: Improve error messages";
            case 14 -> "feat: Add existsByEmail method";
            case 15 -> "refactor: Organize imports alphabetically";
            case 16 -> "feat: Add @Transactional support";
            case 17 -> "refactor: Improve method documentation";
            case 18 -> "refactor: Replace @Autowired with constructor injection";
            case 19 -> "refactor: Rename getUserById to findById for consistency";
            case 20 -> "feat: Add custom exception classes and extract validateUser";
            case 21 -> "feat: Add email validation in validateUser";
            case 22 -> "refactor: Add @Transactional to mutating methods";
            case 23 -> "feat: Add username length validation";
            case 24 -> "fix: Check for duplicate username before creating user";
            case 25 -> "refactor: Migrate to Lombok for logging and constructor";
            case 26 -> "refactor: Remove @Autowired annotations after Lombok migration";
            case 27 -> "feat: Add searchUsers method";
            case 28 -> "feat: Add caching annotations";
            case 29 -> "refactor: Rename getAllUsers to findAll for consistency";
            case 30 -> "refactor: Remove existsByEmail method (moved to repository)";
            case 31 -> "refactor: Optimize imports after Lombok migration";
            case 32 -> "fix: Improve error messages with more context";
            case 33 -> "refactor: Update logging statements to use Lombok log";
            case 34 -> "test: Add unit test stubs";
            case 35 -> "refactor: Extract validation constants";
            case 36 -> "perf: Add database query optimization hints";
            case 37 -> "refactor: Improve code readability";
            case 38 -> "docs: Add comprehensive method JavaDoc";
            case 39 -> "refactor: Final code cleanup";
            case 40 -> "feat: Add countActiveUsers method";
            default -> "refactor: Code improvements iteration " + (iteration + 1);
        };
    }
}

