package io.indices.tvplaylistgenerator;

public class Config {

    private String clientId = "";
    private String clientSecret = "";
    private String accessToken = "";
    private String refreshToken = "";
    private String tokenCreationDate = "";
    private String tokenValidityDuration = "";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenCreationDate() {
        return tokenCreationDate;
    }

    public void setTokenCreationDate(String tokenCreationDate) {
        this.tokenCreationDate = tokenCreationDate;
    }

    public String getTokenValidityDuration() {
        return tokenValidityDuration;
    }

    public void setTokenValidityDuration(String tokenValidityDuration) {
        this.tokenValidityDuration = tokenValidityDuration;
    }
}
