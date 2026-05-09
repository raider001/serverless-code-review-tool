import com.kalynx.serverlessreviewtool.mockdata.GitRepositoryInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

void main() {
    try {
        logger.info("=================================================");
        logger.info("  Mock Git Repository Initialization");
        logger.info("=================================================");
        logger.info("Starting initialization...");
        GitRepositoryInitializer.main();

        Path basePath = GitRepositoryInitializer.getBasePath();

        logger.info("=================================================");
        logger.info("  Summary");
        logger.info("=================================================");
        logger.info("Mock repositories created at: {}", basePath);
        logger.info("Created repositories:");
        logger.info("  1. java-backend-repo");
        logger.info("     - Location: {}", basePath.resolve("java-backend-repo"));
        logger.info("     - Features: Multiple commits, branches, Git notes");
        logger.info("  2. python-api-repo");
        logger.info("     - Location: {}", basePath.resolve("python-api-repo"));
        logger.info("     - Features: Multiple commits, branches, Git notes");
        logger.info("  3. react-frontend-repo");
        logger.info("     - Location: {}", basePath.resolve("react-frontend-repo"));
        logger.info("     - Features: Multiple commits, branches, Git notes");
        logger.info("=================================================");
        logger.info("  All repositories initialized successfully!");
        logger.info("=================================================");

    } catch (Exception e) {
        logger.error("Failed to initialize mock repositories: {}", e.getMessage(), e);
        System.exit(1);
    }
}
