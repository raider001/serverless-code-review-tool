package com.kalynx.serverlessreviewtool.mockdata.repositories.javabackend.branches;

import com.kalynx.serverlessreviewtool.mockdata.repositories.BaseRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class FeatureAuthBranch extends BaseRepository {
    
    public static void create(Path repoPath) throws Exception {
        createAndCheckoutBranch(repoPath, "feature/oauth-integration");
        
        Path authFile = repoPath.resolve("src/OAuth2Provider.java");
        Files.createDirectories(authFile.getParent());
        Files.writeString(authFile, """
            public class OAuth2Provider {
                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize";
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add OAuth2 provider");

        Files.writeString(authFile, """
            public class OAuth2Provider {
                private final String clientId;

                public OAuth2Provider(String clientId) {
                    this.clientId = clientId;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId;
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add client ID to OAuth2 provider");
        
        checkoutMain(repoPath);
    }
}

