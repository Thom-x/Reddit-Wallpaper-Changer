package fr.maugin.thomas.domain.pojo;

import com.google.common.base.Preconditions;
import net.dean.jraw.RedditClient;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;

import static java.util.UUID.randomUUID;
import static net.dean.jraw.http.UserAgent.of;

/**
 * User: thoma
 * Date: 01/04/2016
 * Time: 18:59
 */
public class FluentRedditClientSingleton{

    private static final FluentRedditClientSingleton ourInstance = new FluentRedditClientSingleton();
    private FluentRedditClient client = null;

    private FluentRedditClientSingleton() {
    }

    public static FluentRedditClientSingleton getInstance() {
        return ourInstance;
    }

    public void authenticate(final String clientId, final String clientSecret) throws OAuthException {
        Preconditions.checkState(clientId != null, "clientId can't be null");
        Preconditions.checkState(clientSecret != null, "clientSecret can't be null");

        System.out.println("Connecting to  Redddit...");
        UserAgent myUserAgent = of("desktop", "fr.maugin.thomas.reddit-wallpaper-changer", "1.0", "Thom-x");
        RedditClient redditClient = new RedditClient(myUserAgent);
        Credentials credentials = Credentials.userless(clientId, clientSecret, randomUUID());
        OAuthData authData = redditClient.getOAuthHelper().easyAuth(credentials);
        redditClient.authenticate(authData);
        this.client = new FluentRedditClient(redditClient);
    }

    public FluentRedditClient getClient(){
        Preconditions.checkState(client != null, "FluentRedditClient must be initialized");

        return client;
    }
}
