import com.kalynx.lwdi.DependencyInjector;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.Git;
import com.kalynx.serverlessreviewtool.git.GitImpl;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.mockdata.RepositoryMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.ReviewContextMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.ReviewItemMockData_Old;
import com.kalynx.serverlessreviewtool.mockdata.UserMockData_Old;
import com.kalynx.serverlessreviewtool.ui.MainFrame;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] ignored) {
        System.out.println("ServerlessReviewTool - Java Application");
        System.out.println("Launching application...");

        try {
            // Initialize DI container
            DependencyInjector di = new DependencyInjector();

            // Register core services
            di.inject(SettingsManager.class);

            // Register managers
            UserManager userManager = di.inject(UserManager.class);
            RepositoryManager repositoryManager = di.inject(RepositoryManager.class);
            ReviewItemManager reviewItemManager = di.inject(ReviewItemManager.class);
            ReviewContextManager reviewContextManager = di.inject(ReviewContextManager.class);

            // Register Git service
            di.inject(Git.class, GitImpl.class);

            // Register UI models
            di.inject(ReviewFormModels.class);

            // Load mock data
            UserMockData_Old.loadMockData(userManager);
            RepositoryMockData_Old.loadMockData(repositoryManager);
            ReviewItemMockData_Old.loadMockData(reviewItemManager);
            ReviewContextMockData_Old.loadMockData(reviewContextManager);

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

