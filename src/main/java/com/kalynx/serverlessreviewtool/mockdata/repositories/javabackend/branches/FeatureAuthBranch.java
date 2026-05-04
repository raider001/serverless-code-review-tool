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
        
        Files.writeString(authFile, """
            public class OAuth2Provider {
                private final String clientId;
                private final String clientSecret;

                public OAuth2Provider(String clientId, String clientSecret) {
                    this.clientId = clientId;
                    this.clientSecret = clientSecret;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId;
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add client secret parameter");

        Files.writeString(authFile, """
            public class OAuth2Provider {
                private final String clientId;
                private final String clientSecret;
                private final String redirectUri;

                public OAuth2Provider(String clientId, String clientSecret, String redirectUri) {
                    this.clientId = clientId;
                    this.clientSecret = clientSecret;
                    this.redirectUri = redirectUri;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId 
                        + "&redirect_uri=" + redirectUri;
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add redirect URI support");

        Files.writeString(authFile, """
            public class OAuth2Provider {
                private final String clientId;
                private final String clientSecret;
                private final String redirectUri;

                public OAuth2Provider(String clientId, String clientSecret, String redirectUri) {
                    this.clientId = clientId;
                    this.clientSecret = clientSecret;
                    this.redirectUri = redirectUri;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId 
                        + "&redirect_uri=" + redirectUri + "&response_type=code";
                }
                
                public String getTokenUrl() {
                    return "https://auth.example.com/oauth/token";
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add token endpoint URL");

        Files.writeString(authFile, """
            import java.util.HashMap;
            import java.util.Map;
            
            public class OAuth2Provider {
                private final String clientId;
                private final String clientSecret;
                private final String redirectUri;

                public OAuth2Provider(String clientId, String clientSecret, String redirectUri) {
                    this.clientId = clientId;
                    this.clientSecret = clientSecret;
                    this.redirectUri = redirectUri;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId 
                        + "&redirect_uri=" + redirectUri + "&response_type=code";
                }
                
                public String getTokenUrl() {
                    return "https://auth.example.com/oauth/token";
                }
                
                public Map<String, String> getTokenRequestParameters(String authorizationCode) {
                    Map<String, String> params = new HashMap<>();
                    params.put("grant_type", "authorization_code");
                    params.put("code", authorizationCode);
                    params.put("client_id", clientId);
                    params.put("client_secret", clientSecret);
                    params.put("redirect_uri", redirectUri);
                    return params;
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add token request parameters builder");

        Files.writeString(authFile, """
            import java.util.HashMap;
            import java.util.Map;
            
            public class OAuth2Provider {
                private final String clientId;
                private final String clientSecret;
                private final String redirectUri;
                private String accessToken;

                public OAuth2Provider(String clientId, String clientSecret, String redirectUri) {
                    this.clientId = clientId;
                    this.clientSecret = clientSecret;
                    this.redirectUri = redirectUri;
                }

                public String getAuthorizationUrl() {
                    return "https://auth.example.com/oauth/authorize?client_id=" + clientId 
                        + "&redirect_uri=" + redirectUri + "&response_type=code";
                }
                
                public String getTokenUrl() {
                    return "https://auth.example.com/oauth/token";
                }
                
                public Map<String, String> getTokenRequestParameters(String authorizationCode) {
                    Map<String, String> params = new HashMap<>();
                    params.put("grant_type", "authorization_code");
                    params.put("code", authorizationCode);
                    params.put("client_id", clientId);
                    params.put("client_secret", clientSecret);
                    params.put("redirect_uri", redirectUri);
                    return params;
                }
                
                public void setAccessToken(String token) {
                    this.accessToken = token;
                }
                
                public String getAccessToken() {
                    return accessToken;
                }
                
                public boolean isAuthenticated() {
                    return accessToken != null && !accessToken.isEmpty();
                }
            }
            """);
        commitFile(repoPath, "src/OAuth2Provider.java", "feat: Add token storage and authentication status");

        checkoutMain(repoPath);
    }
}

