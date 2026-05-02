import com.kalynx.lwdi.DependencyInjector;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitImpl;
import com.kalynx.serverlessreviewtool.git.RepositoryLoader;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.ui.MainFrame;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] ignored) {
        System.out.println("ServerlessReviewTool - Java Application");
        System.out.println("Launching application...");

        try {
            // Initialize DI container
            DependencyInjector di = new DependencyInjector();

            // Register all models first as it is what stitches all comms in the app together.
            di.inject(ReviewFormModels.class);

            // Create and register Git service
            String userHome = System.getProperty("user.home");
            Path gitLocalPath = Paths.get(userHome, ".serverless-review-tool", "repositories");
            GitImpl gitImpl = new GitImpl(gitLocalPath);
            di.add(Git.class, gitImpl);

            UserManager userManager = di.inject(UserManager.class);
            di.inject(RepositoryLoader.class);
            RepositoryManager repositoryManager = di.inject(RepositoryManager.class);
            SettingsManager settingsManager = di.inject(SettingsManager.class);

            ReviewItemManager reviewItemManager = di.inject(ReviewItemManager.class);
            ReviewContextManager reviewContextManager = di.inject(ReviewContextManager.class);




            // Create and show main frame
            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame frame = di.inject(MainFrame.class);
                    frame.setVisible(true);
                } catch (Exception e) {
                    logger.error("Failed to create MainFrame: {}", e.getMessage(), e);
                    System.exit(1);
                }
            });

        } catch (Exception e) {
            logger.error("Failed to initialize application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}

