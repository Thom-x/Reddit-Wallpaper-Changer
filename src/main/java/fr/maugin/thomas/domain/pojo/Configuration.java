package fr.maugin.thomas.domain.pojo;

import fr.maugin.thomas.domain.api.IConfiguration;

import java.util.List;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public class Configuration implements IConfiguration {
    private List<String> subreddits;
    private Long interval;
    private String clientId;
    private String clientSecret;

    public Configuration() {
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public Long getInterval() {
        return interval;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "subreddits=" + subreddits +
                ", interval=" + interval +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                '}';
    }
}
