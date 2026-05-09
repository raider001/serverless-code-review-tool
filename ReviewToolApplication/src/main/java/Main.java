import com.kalynx.lwdi.DependencyInjectionException;
import com.kalynx.lwdi.DependencyInjector;
import com.kalynx.serverlessreviewtool.configuration.SettingsManager;
import com.kalynx.serverlessreviewtool.git.*;
import com.kalynx.serverlessreviewtool.managers.PollingService;
import com.kalynx.serverlessreviewtool.managers.PluginManager;
import com.kalynx.serverlessreviewtool.managers.RepositoryManager;
import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.managers.ReviewItemManager;
import com.kalynx.serverlessreviewtool.managers.UserManager;
import com.kalynx.serverlessreviewtool.models.Repository;
import com.kalynx.serverlessreviewtool.models.User;
import com.kalynx.serverlessreviewtool.plugin.UserPlugin;
import com.kalynx.serverlessreviewtool.ui.MainFrame;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewpanel.ReviewPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.mainpanels.reviewselectionpanel.ReviewSelectionPanelModel;
import com.kalynx.serverlessreviewtool.ui.models.reviewpanel.reviewformdialog.ReviewFormModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final DependencyInjector DI = new DependencyInjector();
    private static final String userHome = System.getProperty("user.home");
    private static final Path gitLocalPath = Paths.get(userHome, ".serverless-review-tool", "repositories");

    private static final RepositoryManager REPOSITORY_MANAGER;
    private static final SettingsManager SETTINGS_MANAGER;
    private static final ReviewItemManager REVIEW_ITEM_MANAGER;
    private static final ReviewContextManager REVIEW_CONTEXT_MANAGER;
    private static final UserManager USER_MANAGER;
    private static final PluginManager PLUGIN_MANAGER;
    private static final ReviewFormModels REVIEW_FORM_MODELS;
    private static final ReviewSelectionPanelModel REVIEW_SELECTION_PANEL_MODEL;
    private static final ReviewPanelModel REVIEW_PANEL_MODEL;

    static {
        try {
            DI.add(Git.class, new GitImpl(gitLocalPath));
            DI.inject(RepositoryLoader.class);
            REPOSITORY_MANAGER = DI.inject(RepositoryManager.class);
            SETTINGS_MANAGER = DI.inject(SettingsManager.class);
            DI.inject(ReviewItemLoader.class);
            REVIEW_ITEM_MANAGER = DI.inject(ReviewItemManager.class);
            REVIEW_CONTEXT_MANAGER = DI.inject(ReviewContextManager.class);
            DI.inject(PollingService.class);
            USER_MANAGER = DI.inject(UserManager.class);
            PLUGIN_MANAGER = DI.inject(PluginManager.class);
            REVIEW_FORM_MODELS = DI.inject(ReviewFormModels.class);
            REVIEW_SELECTION_PANEL_MODEL = DI.inject(ReviewSelectionPanelModel.class);
            REVIEW_PANEL_MODEL = DI.inject(ReviewPanelModel.class);
        } catch (DependencyInjectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] ignored) {
        logger.info("ServerlessReviewTool - Java Application");
        logger.info("Launching application...");



        USER_MANAGER.addListener(users -> REVIEW_FORM_MODELS.availableReviewers.setValue(users.stream().map(User::getName).toList()));
        REPOSITORY_MANAGER.addListener(ignore -> REVIEW_ITEM_MANAGER.refresh());

        PLUGIN_MANAGER.addListenerToUserPlugins(UserPlugin.NotificationType.USER_ADDED, usernames ->
                USER_MANAGER.addUsers(Arrays.stream(usernames).map(u -> new User(u, "", u)).toList())
        );

        PLUGIN_MANAGER.addListenerToUserPlugins(UserPlugin.NotificationType.USER_REMOVED,
                USER_MANAGER::removeUsers
        );

        PLUGIN_MANAGER.initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(PLUGIN_MANAGER::shutdown));


        setupReviewFormModelUpdaters();
        setupReviewSelectionPanelModelUpdaters();
        setupReviewPanelModelUpdaters();


        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = DI.inject(MainFrame.class);
                frame.setVisible(true);
                SETTINGS_MANAGER.addRepositoryNameListener(REPOSITORY_MANAGER::updateRepositories);

            } catch (Exception e) {
                logger.error("Failed to create MainFrame: {}", e.getMessage(), e);
                System.exit(1);
            }
        });

    }

    private static void setupReviewPanelModelUpdaters() {
        REVIEW_CONTEXT_MANAGER.addListener(reviewContext -> {
            if (reviewContext == null) return;
            REVIEW_PANEL_MODEL.setCurrentReview(reviewContext.getReviewId());
            REVIEW_PANEL_MODEL.reviewDetailModel.title.setValue(reviewContext.title);
            REVIEW_PANEL_MODEL.reviewDetailModel.summary.setValue(reviewContext.summary);
            REVIEW_PANEL_MODEL.reviewDetailModel.author.setValue(reviewContext.author);
            REVIEW_PANEL_MODEL.reviewDetailModel.status.setValue(reviewContext.status);
            REVIEW_PANEL_MODEL.reviewDetailModel.reviewers.setValue(reviewContext.reviewers);
            REVIEW_PANEL_MODEL.commentsPanelModel.setComments(reviewContext.comments);
        });
    }

    private static void setupReviewFormModelUpdaters() {
        REPOSITORY_MANAGER.addListener(repositories -> REVIEW_FORM_MODELS.availableRepositories.setValue(repositories.stream().map(Repository::getName).toList()));
        REPOSITORY_MANAGER.addListener(repositories -> REVIEW_FORM_MODELS.availableBranches.setValue(repositories.stream().flatMap(r -> r.getBranches().stream()).toList()));
        SETTINGS_MANAGER.addUserNameListener(REVIEW_FORM_MODELS.author::setValue);
        REVIEW_FORM_MODELS.author.setValue(SETTINGS_MANAGER.getCurrentUserName());
    }

    private static void setupReviewSelectionPanelModelUpdaters() {
        REVIEW_ITEM_MANAGER.addListener(REVIEW_SELECTION_PANEL_MODEL::setAllReviews);
        REVIEW_SELECTION_PANEL_MODEL.setCurrentUser(SETTINGS_MANAGER.getCurrentUserEmail(), SETTINGS_MANAGER.getCurrentUserName());
        SETTINGS_MANAGER.addUserNameListener(userName ->
                REVIEW_SELECTION_PANEL_MODEL.setCurrentUser(SETTINGS_MANAGER.getCurrentUserEmail(), userName)
        );
    }
}

