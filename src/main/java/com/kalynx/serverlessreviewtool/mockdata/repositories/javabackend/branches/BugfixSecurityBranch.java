package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.branches;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class BugfixSecurityBranch extends BaseRepository {

    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "bugfix/sql-injection-fix");

        Path queryFile = repoPath.resolve("src/QueryBuilder.java");
        Files.writeString(queryFile,
            "import java.sql.PreparedStatement;\n" +
            "import java.sql.Connection;\n" +
            "\n" +
            "public class QueryBuilder {\n" +
            "    public PreparedStatement buildUserQuery(Connection conn, String username) throws Exception {\n" +
            "        String sql = \"SELECT * FROM users WHERE username = ?\";\n" +
            "        PreparedStatement stmt = conn.prepareStatement(sql);\n" +
            "        stmt.setString(1, username);\n" +
            "        return stmt;\n" +
            "    }\n" +
            "}\n");
        commitFile(repoPath, "src/QueryBuilder.java", "fix: Use prepared statements to prevent SQL injection");

        Path testFile = repoPath.resolve("src/QueryBuilderTest.java");
        Files.writeString(testFile,
            "public class QueryBuilderTest {\n" +
            "    public void testSqlInjectionPrevention() {\n" +
            "        // Test that malicious input is properly escaped\n" +
            "    }\n" +
            "}\n");
        commitFile(repoPath, "src/QueryBuilderTest.java", "test: Add SQL injection test cases");

        checkoutMain(repoPath);
    }
}

