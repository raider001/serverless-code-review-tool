package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class UserRepositoryFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/main/java/com/example/repository/UserRepository.java";
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

        content.append("package com.example.repository;\n\n");

        if (iteration >= 1) {
            content.append("import com.example.model.User;\n");
        }
        if (iteration >= 2) {
            content.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        }
        if (iteration >= 3) {
            content.append("import org.springframework.stereotype.Repository;\n");
        }
        if (iteration >= 5) {
            content.append("import java.util.Optional;\n");
        }
        if (iteration >= 7) {
            content.append("import java.util.List;\n");
        }
        if (iteration >= 10 && iteration < 20) {
            content.append("import org.springframework.data.jpa.repository.Query;\n");
            content.append("import org.springframework.data.repository.query.Param;\n");
        }
        if (iteration >= 20) {
            content.append("import org.springframework.data.jpa.repository.Query;\n");
            content.append("import org.springframework.data.jpa.repository.QueryHints;\n");
            content.append("import org.springframework.data.repository.query.Param;\n");
            content.append("import jakarta.persistence.QueryHint;\n");
        }
        if (iteration >= 25) {
            content.append("import org.springframework.data.jpa.repository.Modifying;\n");
        }

        content.append("\n");

        if (iteration >= 3) {
            content.append("@Repository\n");
        }
        content.append("public interface UserRepository");
        if (iteration >= 2) {
            content.append(" extends JpaRepository<User, Long>");
        }
        content.append(" {\n");

        if (iteration >= 5 && iteration < 18) {
            content.append("\n    Optional<User> findByUsername(String username);\n");
        } else if (iteration >= 18) {
            content.append("\n    @Query(\"SELECT u FROM User u WHERE u.username = :username AND u.active = true\")\n");
            content.append("    Optional<User> findByUsername(@Param(\"username\") String username);\n");
        }

        if (iteration >= 6) {
            content.append("\n    Optional<User> findByEmail(String email);\n");
        }

        if (iteration >= 7) {
            content.append("\n    List<User> findByActiveTrue();\n");
        }

        if (iteration >= 8) {
            content.append("\n    boolean existsByEmail(String email);\n");
            content.append("\n    boolean existsByUsername(String username);\n");
        }

        if (iteration >= 10) {
            if (iteration < 20) {
                content.append("\n    @Query(\"SELECT u FROM User u WHERE u.createdAt >= :since\")\n");
            } else {
                content.append("\n    @Query(\"SELECT u FROM User u WHERE u.createdAt >= :since\")\n");
                content.append("    @QueryHints(@QueryHint(name = \"org.hibernate.cacheable\", value = \"true\"))\n");
            }
            content.append("    List<User> findRecentUsers(@Param(\"since\") java.time.LocalDateTime since);\n");
        }

        if (iteration >= 12 && iteration < 17) {
            content.append("\n    @Query(\"SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))\")\n");
            content.append("    List<User> searchUsers(@Param(\"search\") String searchTerm);\n");
        } else if (iteration >= 17) {
            content.append("\n    @Query(\"SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))\")\n");
            content.append("    List<User> searchByUsernameOrEmail(@Param(\"search\") String searchTerm);\n");
        }

        if (iteration >= 14) {
            content.append("\n    @Query(\"SELECT COUNT(u) FROM User u WHERE u.active = true\")\n");
            content.append("    long countActiveUsers();\n");
        }

        if (iteration >= 16) {
            content.append("\n    List<User> findByRoleIn(List<String> roles);\n");
        }

        if (iteration >= 22) {
            content.append("\n    long countByActiveTrue();\n");
        }

        if (iteration >= 25) {
            content.append("\n    @Modifying\n");
            content.append("    @Query(\"UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId\")\n");
            content.append("    void updateLastLogin(@Param(\"userId\") Long userId, @Param(\"loginTime\") java.time.LocalDateTime loginTime);\n");
        }

        if (iteration >= 28) {
            content.append("\n    @Query(\"SELECT u FROM User u WHERE u.email LIKE CONCAT('%', :domain)\")\n");
            content.append("    List<User> findByEmailDomain(@Param(\"domain\") String domain);\n");
        }

        if (iteration >= 30 && iteration < 35) {
            content.append("\n    List<User> findTop10ByOrderByCreatedAtDesc();\n");
        }

        content.append("}\n");

        return content.toString();
    }

    private static String getCommitMessage(int iteration) {
        return switch (iteration) {
            case 0 -> "feat: Create initial UserRepository interface";
            case 1 -> "feat: Add User model import";
            case 2 -> "feat: Extend JpaRepository for CRUD operations";
            case 3 -> "feat: Add Repository annotation";
            case 4 -> "refactor: Add interface documentation";
            case 5 -> "feat: Add findByUsername query method";
            case 6 -> "feat: Add findByEmail query method";
            case 7 -> "feat: Add findByActiveTrue query method";
            case 8 -> "feat: Add exists check methods";
            case 9 -> "refactor: Organize import statements";
            case 10 -> "feat: Add custom query for recent users";
            case 11 -> "refactor: Improve query parameter naming";
            case 12 -> "feat: Add user search functionality";
            case 13 -> "refactor: Optimize search query";
            case 14 -> "feat: Add count active users query";
            case 15 -> "refactor: Add query hints for performance";
            case 16 -> "feat: Add role-based query method";
            case 17 -> "refactor: Rename searchUsers to searchByUsernameOrEmail";
            case 18 -> "refactor: Replace derived query with @Query for findByUsername";
            case 19 -> "fix: Add active user filter to findByUsername";
            case 20 -> "feat: Add QueryHints for cacheability";
            case 21 -> "refactor: Update imports to jakarta.persistence";
            case 22 -> "feat: Add countByActiveTrue derived query";
            case 23 -> "refactor: Optimize query performance";
            case 24 -> "refactor: Add fetch join hints";
            case 25 -> "feat: Add updateLastLogin modifying query";
            case 26 -> "refactor: Organize queries by type";
            case 27 -> "test: Add repository integration tests";
            case 28 -> "feat: Add findByEmailDomain query";
            case 29 -> "refactor: Improve query readability";
            case 30 -> "feat: Add findTop10ByOrderByCreatedAtDesc";
            case 31 -> "refactor: Add query timeout hints";
            case 32 -> "perf: Add database index suggestions";
            case 33 -> "refactor: Extract complex queries";
            case 34 -> "refactor: Remove findTop10 method (use Pageable instead)";
            case 35 -> "docs: Add method documentation";
            case 36 -> "refactor: Standardize parameter names";
            case 37 -> "perf: Optimize JOIN strategies";
            case 38 -> "refactor: Final cleanup";
            case 39 -> "test: Add query performance tests";
            case 40 -> "refactor: Code review improvements";
            default -> "refactor: Repository improvements iteration " + (iteration + 1);
        };
    }
}

