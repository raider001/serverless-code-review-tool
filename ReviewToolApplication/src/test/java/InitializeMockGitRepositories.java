import com.kalynx.serverlessreviewtool.mockdata.GitRepositoryInitializer;

import java.nio.file.Path;

public class InitializeMockGitRepositories {

    public static void main(String[] args) {
        try {
            System.out.println("=================================================");
            System.out.println("  Mock Git Repository Initialization");
            System.out.println("=================================================\n");

            System.out.println("Starting initialization...");
            GitRepositoryInitializer.main(new String[0]);

            Path basePath = GitRepositoryInitializer.getBasePath();

            System.out.println("\n=================================================");
            System.out.println("  Summary");
            System.out.println("=================================================");
            System.out.println("Mock repositories created at:");
            System.out.println("  " + basePath);
            System.out.println();
            System.out.println("Created repositories:");
            System.out.println("  1. java-backend-repo");
            System.out.println("     - Location: " + basePath.resolve("java-backend-repo"));
            System.out.println("     - Features: Multiple commits, branches, Git notes");
            System.out.println();
            System.out.println("  2. python-api-repo");
            System.out.println("     - Location: " + basePath.resolve("python-api-repo"));
            System.out.println("     - Features: Multiple commits, branches, Git notes");
            System.out.println();
            System.out.println("  3. react-frontend-repo");
            System.out.println("     - Location: " + basePath.resolve("react-frontend-repo"));
            System.out.println("     - Features: Multiple commits, branches, Git notes");
            System.out.println();
            System.out.println("=================================================");
            System.out.println("  ✓ All repositories initialized successfully!");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("Failed to initialize mock repositories:");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}


