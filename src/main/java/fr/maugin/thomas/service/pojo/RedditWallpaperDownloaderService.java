package fr.maugin.thomas.service.pojo;

import com.google.common.collect.Lists;
import fr.maugin.thomas.App;
import fr.maugin.thomas.domain.api.IConfiguration;
import fr.maugin.thomas.domain.pojo.FluentRedditClientSingleton;
import fr.maugin.thomas.domain.pojo.Wallpaper;
import fr.maugin.thomas.domain.api.IWallpaper;
import fr.maugin.thomas.service.api.IWallpaperDownloaderService;
import fr.maugin.thomas.utils.Utils;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import rx.Observable;
import rx.functions.Func1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.Observable.empty;
import static rx.Observable.just;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public class RedditWallpaperDownloaderService implements IWallpaperDownloaderService {

    private static final ArrayList<String> IMGUR = Lists.newArrayList("https://i.imgur.com/", "https://imgur.com/",
            "http://imgur.com/",
            "http://i.imgur.com/");
    private static final ArrayList<String> DISALLOWED_EXTENSIONS = Lists.newArrayList("gif", "gifv");
    private static final ArrayList<String> ALLOWED_EXTENSIONS = Lists.newArrayList("png", "jpg");
    private static final String TMP_WALLPAPER_FOLDER = "/wallpapers";
    private static final int MAX_IMAGE_FETCH = 50;
    private static final Logger logger = Utils.getLogger(RedditWallpaperDownloaderService.class);
    private static final int TIMEOUT = 60;
    private static final IWallpaper DUMMY_WALLPAPER = new Wallpaper("dummy", "dummy", "dummy", "dummy");

    private final String clientId;
    private final String clientSecret;
    private final Func1<BufferedImage, Boolean> FILTER_BY_SIZE =
            imgBuffer -> {
                GraphicsDevice gd = getLocalGraphicsEnvironment().getDefaultScreenDevice();
                int width = gd.getDisplayMode().getWidth();
                int height = gd.getDisplayMode().getHeight();
                final boolean isNotTooSmall = imgBuffer.getHeight() >= height && imgBuffer.getWidth() >= width;
                if (!isNotTooSmall)
                    System.out.println("Image is too small");
                return isNotTooSmall;
            };
    private final Func1<String, Observable<BufferedImage>> DOWNLOAD_IMAGE =
            imgLink -> {
                try {
                    System.out.println("Downloading " + imgLink);
                    URL url = new URL(imgLink);
                    final BufferedImage image = ImageIO.read(url);
                    return Observable.just(image);
                } catch (IOException e) {
                    e.printStackTrace();
                    return empty();
                }
            };
    private final Func1<String, String> PARSE_URL = v -> {
        if (ALLOWED_EXTENSIONS.stream().anyMatch(v::endsWith)) {
            return v;
        }
        if (IMGUR.stream() //
                .anyMatch(v::startsWith)) {
            return v + ".png";
        }
        return v;
    };

    public RedditWallpaperDownloaderService(String clientId, String clientSecret) throws OAuthException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        FluentRedditClientSingleton.getInstance().authenticate(clientId, clientSecret);
    }

    @Override
    public Observable<IWallpaper> getWallpaper(final IConfiguration config) {
        final List<String> subredditsList = config.getSubreddits();
        final Random randomGenerator = new Random();
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Observable.error(e);
        }

        return Observable.interval(config.getInterval(), SECONDS)
                .startWith(-1L)
                .flatMap(getWallpaperPath(subredditsList, randomGenerator, sha));
    }

    private Func1<Long, Observable<IWallpaper>> getWallpaperPath(List<String> subredditsList, Random randomGenerator, MessageDigest sha) {
        return i -> Observable.just(subredditsList.get(randomGenerator.nextInt(subredditsList.size())))
                .doOnNext(subreddit -> System.out.println("Choosen subreddit : " + subreddit))
                .concatMap(subreddit -> Observable.from(FluentRedditClientSingleton.getInstance().getClient().subreddit(subreddit)
                        .hot()
                        .limit(MAX_IMAGE_FETCH)
                        .fetch())
                        .skip(randomGenerator.nextInt(25))) // Random img in the 25 hottest
                .concatMap(submission -> Observable.just(submission)
                        .map(Submission::getUrl)
                        .filter(url -> DISALLOWED_EXTENSIONS.stream().noneMatch(url::endsWith))
                        .map(PARSE_URL)
                        .filter(url -> ALLOWED_EXTENSIONS.stream().anyMatch(url::endsWith))
                        .flatMap(DOWNLOAD_IMAGE)
                        .filter(FILTER_BY_SIZE)
                        .flatMap(writeFile(sha, submission)))
                .startWith(DUMMY_WALLPAPER)
                .timeout(TIMEOUT, SECONDS)
                .skip(1)
                .onErrorResumeNext((e) -> {
                    if (e instanceof TimeoutException) {
                        logger.warning("Timeout fetching wallpaper");
                    } else if (e instanceof NetworkException) {
                        try {
                            FluentRedditClientSingleton.getInstance().authenticate(clientId, clientSecret);
                        } catch (OAuthException e1) {
                            e1.printStackTrace();
                            System.exit(1);
                        }
                    } else {
                        logger.warning(e.toString());
                    }
                    return getWallpaperPath(subredditsList, randomGenerator, sha).call(1L);
                })
                .take(1);
    }

    private static Func1<BufferedImage, Observable<IWallpaper>> writeFile(MessageDigest sha, Submission submission) {
        return imgBuffer -> {
            try {
                final File appPath = Utils.getAppPath(App.class);
                final File wallpapersPath = new File(appPath.getAbsolutePath() + TMP_WALLPAPER_FOLDER);
                if (!wallpapersPath.exists())
                    wallpapersPath.mkdir();
                final String imagePath = appPath.getAbsolutePath() + TMP_WALLPAPER_FOLDER + "/" + Utils.hexEncode(sha.digest(submission.getUrl().getBytes())) + ".jpg";
                final File imageFile = new File(imagePath);
                if(!imageFile.exists()) {
                    ImageIO.write(imgBuffer, "jpg", imageFile);
                }
                logger.info("Changing wallpaper : " + submission.getUrl());
                logger.info("Title : " + submission.getTitle());
                logger.info("Subreddit : " + submission.getSubredditName());
                return just(new Wallpaper(imagePath, submission.getTitle(), submission.getSubredditName(), submission.getUrl()));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                return empty();
            }
        };
    }

    private static void waitInput() {
        System.out.println("Press ENTER to exit");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Observable<String> getImageLink(Listing<Submission> submission) {
        return Observable.from(submission)
                .map(Submission::getUrl)
                .filter(url -> DISALLOWED_EXTENSIONS.stream().noneMatch(url::endsWith))
                .map(PARSE_URL)
                .filter(url -> ALLOWED_EXTENSIONS.stream().anyMatch(url::endsWith));
    }

}