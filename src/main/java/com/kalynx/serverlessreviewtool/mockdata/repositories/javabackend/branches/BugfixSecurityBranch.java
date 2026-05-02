package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.branches;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class BugfixSecurityBranch extends BaseRepository {

    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "bugfix/sql-injection-fix");

        Path queryFile = repoPath.resolve("src/QueryBuilder.java");
        Files.writeString(queryFile, """
            import java.sql.PreparedStatement;
            import java.sql.Connection;
            
            public class QueryBuilder {
                public PreparedStatement buildUserQuery(Connection conn, String username) throws Exception {
                    String sql = "SELECT * FROM users WHERE username = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    return stmt;
                }
            }
            """);
        commitFile(repoPath, "src/QueryBuilder.java", "fix: Use prepared statements to prevent SQL injection");

        Path testFile = repoPath.resolve("src/QueryBuilderTest.java");
        Files.writeString(testFile, """
            public class QueryBuilderTest {
                public void testSqlInjectionPrevention() {
                    // Test that malicious input is properly escaped
                }
            }
            """);
        commitFile(repoPath, "src/QueryBuilderTest.java", "test: Add SQL injection test cases");

        checkoutMain(repoPath);
    }
}

