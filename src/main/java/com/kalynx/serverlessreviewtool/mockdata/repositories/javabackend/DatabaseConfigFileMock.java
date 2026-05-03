package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class DatabaseConfigFileMock extends BaseRepository {
    private static final String FILE_PATH = "src/main/java/com/example/config/DatabaseConfig.java";
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

        content.append("package com.example.config;\n\n");

        if (iteration >= 1) {
            content.append("import org.springframework.context.annotation.Configuration;\n");
        }
        if (iteration >= 2) {
            content.append("import org.springframework.context.annotation.Bean;\n");
            content.append("import javax.sql.DataSource;\n");
        }
        if (iteration >= 3) {
            content.append("import org.springframework.boot.jdbc.DataSourceBuilder;\n");
            content.append("import org.springframework.beans.factory.annotation.Value;\n");
        }
        if (iteration >= 6 && iteration < 18) {
            content.append("import com.zaxxer.hikari.HikariConfig;\n");
            content.append("import com.zaxxer.hikari.HikariDataSource;\n");
        }
        if (iteration >= 18) {
            content.append("import org.springframework.boot.context.properties.ConfigurationProperties;\n");
            content.append("import org.springframework.boot.jdbc.DataSourceProperties;\n");
        }
        if (iteration >= 9 && iteration < 25) {
            content.append("import org.slf4j.Logger;\n");
            content.append("import org.slf4j.LoggerFactory;\n");
        }
        if (iteration >= 25) {
            content.append("import lombok.extern.slf4j.Slf4j;\n");
        }

        content.append("\n");

        if (iteration >= 1) {
            content.append("@Configuration\n");
        }
        if (iteration >= 25) {
            content.append("@Slf4j\n");
        }
        content.append("public class DatabaseConfig {\n\n");

        if (iteration >= 9 && iteration < 25) {
            content.append("    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);\n\n");
        }

        if (iteration >= 3 && iteration < 18) {
            content.append("    @Value(\"${spring.datasource.url}\")\n");
            content.append("    private String dbUrl;\n\n");
            content.append("    @Value(\"${spring.datasource.username}\")\n");
            content.append("    private String dbUsername;\n\n");
            content.append("    @Value(\"${spring.datasource.password}\")\n");
            content.append("    private String dbPassword;\n\n");
        }

        if (iteration >= 7 && iteration < 18) {
            content.append("    @Value(\"${spring.datasource.hikari.maximum-pool-size:10}\")\n");
            content.append("    private int maxPoolSize;\n\n");
            content.append("    @Value(\"${spring.datasource.hikari.minimum-idle:5}\")\n");
            content.append("    private int minIdle;\n\n");
        }

        if (iteration >= 2 && iteration < 18) {
            content.append("    @Bean\n");
            content.append("    public DataSource dataSource() {\n");
            if (iteration >= 9 && iteration < 25) {
                content.append("        logger.info(\"Initializing DataSource\");\n");
            } else if (iteration >= 25) {
                content.append("        log.info(\"Initializing DataSource\");\n");
            }
            if (iteration >= 6) {
                content.append("        HikariConfig config = new HikariConfig();\n");
                if (iteration >= 3) {
                    content.append("        config.setJdbcUrl(dbUrl);\n");
                    content.append("        config.setUsername(dbUsername);\n");
                    content.append("        config.setPassword(dbPassword);\n");
                } else {
                    content.append("        config.setJdbcUrl(\"jdbc:postgresql://localhost:5432/mydb\");\n");
                    content.append("        config.setUsername(\"user\");\n");
                    content.append("        config.setPassword(\"password\");\n");
                }
                if (iteration >= 7) {
                    content.append("        config.setMaximumPoolSize(maxPoolSize);\n");
                    content.append("        config.setMinimumIdle(minIdle);\n");
                }
                if (iteration >= 8) {
                    content.append("        config.setConnectionTimeout(30000);\n");
                    content.append("        config.setIdleTimeout(600000);\n");
                    content.append("        config.setMaxLifetime(1800000);\n");
                }
                if (iteration >= 10) {
                    content.append("        config.setPoolName(\"UserServiceHikariPool\");\n");
                    content.append("        config.setConnectionTestQuery(\"SELECT 1\");\n");
                }
                if (iteration >= 11) {
                    content.append("        config.setLeakDetectionThreshold(60000);\n");
                }
                content.append("        return new HikariDataSource(config);\n");
            } else if (iteration >= 3) {
                content.append("        return DataSourceBuilder.create()\n");
                content.append("            .url(dbUrl)\n");
                content.append("            .username(dbUsername)\n");
                content.append("            .password(dbPassword)\n");
                content.append("            .build();\n");
            } else {
                content.append("        return DataSourceBuilder.create()\n");
                content.append("            .url(\"jdbc:postgresql://localhost:5432/mydb\")\n");
                content.append("            .username(\"user\")\n");
                content.append("            .password(\"password\")\n");
                content.append("            .build();\n");
            }
            content.append("    }\n");
        } else if (iteration >= 18) {
            content.append("    @Bean\n");
            content.append("    @ConfigurationProperties(prefix = \"spring.datasource\")\n");
            content.append("    public DataSourceProperties dataSourceProperties() {\n");
            if (iteration >= 25) {
                content.append("        log.info(\"Configuring DataSource properties\");\n");
            }
            content.append("        return new DataSourceProperties();\n");
            content.append("    }\n\n");
            content.append("    @Bean\n");
            content.append("    @ConfigurationProperties(prefix = \"spring.datasource.hikari\")\n");
            content.append("    public DataSource dataSource(DataSourceProperties properties) {\n");
            if (iteration >= 25) {
                content.append("        log.info(\"Creating HikariCP DataSource\");\n");
            }
            content.append("        return properties.initializeDataSourceBuilder()\n");
            content.append("            .type(com.zaxxer.hikari.HikariDataSource.class)\n");
            content.append("            .build();\n");
            content.append("    }\n");
        }

        if (iteration >= 13 && iteration < 20) {
            content.append("\n");
            content.append("    @Bean\n");
            content.append("    public DatabaseHealthChecker databaseHealthChecker(DataSource dataSource) {\n");
            if (iteration >= 25) {
                content.append("        log.info(\"Initializing DatabaseHealthChecker\");\n");
            } else if (iteration >= 13) {
                content.append("        logger.info(\"Initializing DatabaseHealthChecker\");\n");
            }
            content.append("        return new DatabaseHealthChecker(dataSource);\n");
            content.append("    }\n");
        }

        if (iteration >= 22) {
            content.append("\n");
            content.append("    @Bean\n");
            content.append("    public DatabaseMigrationRunner migrationRunner(DataSource dataSource) {\n");
            if (iteration >= 25) {
                content.append("        log.info(\"Initializing database migration runner\");\n");
            }
            content.append("        return new DatabaseMigrationRunner(dataSource);\n");
            content.append("    }\n");
        }

        if (iteration >= 28) {
            content.append("\n");
            content.append("    @Bean\n");
            content.append("    public DataSourceMonitor dataSourceMonitor(DataSource dataSource) {\n");
            content.append("        log.info(\"Initializing DataSource monitor\");\n");
            content.append("        return new DataSourceMonitor(dataSource);\n");
            content.append("    }\n");
        }

        content.append("}\n");

        return content.toString();
    }

    private static String getCommitMessage(int iteration) {
        return switch (iteration) {
            case 0 -> "feat: Create initial DatabaseConfig class";
            case 1 -> "feat: Add Configuration annotation";
            case 2 -> "feat: Add DataSource bean with hardcoded values";
            case 3 -> "refactor: Use externalized configuration properties";
            case 4 -> "refactor: Update database URL pattern";
            case 5 -> "docs: Add configuration comments";
            case 6 -> "feat: Switch to HikariCP for connection pooling";
            case 7 -> "feat: Add configurable pool size settings";
            case 8 -> "feat: Add connection timeout configurations";
            case 9 -> "feat: Add logging support";
            case 10 -> "feat: Add pool name and connection test query";
            case 11 -> "feat: Add connection leak detection";
            case 12 -> "refactor: Optimize pool settings";
            case 13 -> "feat: Add database health checker bean";
            case 14 -> "refactor: Extract database initialization logic";
            case 15 -> "fix: Correct connection pool configuration";
            case 16 -> "refactor: Improve configuration organization";
            case 17 -> "test: Add configuration tests";
            case 18 -> "refactor: Migrate to ConfigurationProperties pattern";
            case 19 -> "refactor: Remove manual @Value field injection";
            case 20 -> "refactor: Remove DatabaseHealthChecker (moved to separate config)";
            case 21 -> "refactor: Simplify DataSource bean creation";
            case 22 -> "feat: Add database migration runner";
            case 23 -> "refactor: Organize beans logically";
            case 24 -> "fix: Update Hikari configuration binding";
            case 25 -> "refactor: Migrate to Lombok logging";
            case 26 -> "refactor: Remove manual logger declaration";
            case 27 -> "feat: Add connection pool metrics";
            case 28 -> "feat: Add DataSource monitoring bean";
            case 29 -> "refactor: Extract monitoring configuration";
            case 30 -> "perf: Optimize pool settings for production";
            case 31 -> "refactor: Add configuration validation";
            case 32 -> "docs: Document configuration properties";
            case 33 -> "refactor: Improve error handling";
            case 34 -> "feat: Add connection retry logic";
            case 35 -> "refactor: Cleanup unused imports";
            case 36 -> "test: Add DataSource integration tests";
            case 37 -> "refactor: Final configuration polish";
            case 38 -> "docs: Add JavaDoc to beans";
            case 39 -> "refactor: Optimize import statements";
            case 40 -> "feat: Add database metrics collection";
            default -> "refactor: Configuration improvements iteration " + (iteration + 1);
        };
    }
}

