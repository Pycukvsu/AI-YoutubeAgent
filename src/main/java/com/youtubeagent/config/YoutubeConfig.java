package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "youtube")
public class YoutubeConfig {

    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String channelId;
    private String defaultCategory = "22";
    private String defaultVisibility = "PRIVATE";

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getDefaultCategory() { return defaultCategory; }
    public void setDefaultCategory(String defaultCategory) { this.defaultCategory = defaultCategory; }

    public String getDefaultVisibility() { return defaultVisibility; }
    public void setDefaultVisibility(String defaultVisibility) { this.defaultVisibility = defaultVisibility; }
}
