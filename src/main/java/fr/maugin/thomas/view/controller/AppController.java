package fr.maugin.thomas.view.controller;

import fr.maugin.thomas.App;
import fr.maugin.thomas.domain.api.IConfiguration;
import fr.maugin.thomas.domain.api.IWallpaper;
import fr.maugin.thomas.domain.pojo.Configuration;
import fr.maugin.thomas.service.api.IWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.RedditWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.WallpaperChanger;
import fr.maugin.thomas.utils.Utils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.dean.jraw.http.oauth.OAuthException;
import org.yaml.snakeyaml.Yaml;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * User: thoma
 * Date: 30/03/2016
 * Time: 18:59
 */
public class AppController implements Initializable {

    private static final Logger logger = Utils.getLogger(AppController.class);

    @FXML
    protected ImageView next;

    @FXML
    protected ImageView imageView;

    @FXML
    protected ImageView splash;

    @FXML
    protected Label subreddit;

    @FXML
    protected Label title;

    @FXML
    protected Label delay;

    private PublishSubject<Observable<IWallpaper>> wallpaperPublishSubject = PublishSubject.create();
    private Optional<URI> wallpaperLinkOpt = Optional.empty();
    private IWallpaperDownloaderService app;
    private IConfiguration config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        next.setVisible(false);
        delay.setVisible(false);
        subreddit.setVisible(false);
        title.setVisible(false);

        next.setImage(new Image(getClass().getClassLoader().getResourceAsStream("images/next.png")));
        splash.setImage(new Image(getClass().getClassLoader().getResourceAsStream("images/splash.png")));

        final WallpaperChanger wallpaperChanger = new WallpaperChanger();

        config = null;
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(Utils.getAppPath(App.class).getAbsolutePath() + "\\config.yaml"))) {
            config = yaml.loadAs(in, Configuration.class);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            app = new RedditWallpaperDownloaderService(config.getClientId(), config.getClientSecret());
        } catch (OAuthException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final Observable<IWallpaper> wallpaperObservable = getWallpaperObservable()
                .switchMap(wallpapers -> wallpapers)
                .retry()
                .share();

        wallpaperObservable
                .switchMap(nil -> Observable.interval(1, TimeUnit.SECONDS) //
                        .map(elapsed -> config.getInterval() - elapsed))
                .filter(remaining -> remaining >= 0)
                .map(Duration::ofSeconds)
                .map(duration -> String.format("%d:%02d%n", duration.toMinutes(), duration.minusMinutes(duration.toMinutes()).getSeconds()))
                .observeOn(JavaFxScheduler.getInstance())
                .subscribe(text -> {
                    delay.setVisible(true);
                    delay.setText(text);
                }, (e) -> {
                    logger.warning(e.toString());
                });

        wallpaperObservable
                .observeOn(JavaFxScheduler.getInstance())
                .subscribe(wallpaper -> {
                    final Image image = new Image("file:///" + wallpaper.getPath());
                    next.setVisible(true);
                    splash.setVisible(false);
                    subreddit.setVisible(true);
                    title.setVisible(true);
                    try {
                        wallpaperLinkOpt = Optional.of(new URL(wallpaper.getLink()).toURI());
                    } catch (URISyntaxException | MalformedURLException e) {
                    }
                    imageView.setImage(image);
                    subreddit.setText(wallpaper.getSubreddit());
                    subreddit.setTooltip(new Tooltip(wallpaper.getSubreddit()));
                    title.setText(wallpaper.getTitle());
                    title.setTooltip(new Tooltip(wallpaper.getTitle()));
                    wallpaperChanger.changeWallpaper(new File(wallpaper.getPath()));
                }, (e) -> {
                    logger.warning(e.toString());
                });

        wallpaperPublishSubject.onNext(app.getWallpaper(config).subscribeOn(Schedulers.io()));
    }

    @FXML
    public void handleNext(Event event) {
        wallpaperPublishSubject.onNext(app.getWallpaper(config).subscribeOn(Schedulers.io()));
    }

    @FXML
    public void handleLink(Event event) {
        wallpaperLinkOpt.ifPresent(wallpaperLink -> {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(wallpaperLink);
                } catch (Exception ignored) {
                    System.err.println(ignored);
                }
            }
        });
    }

    private Observable<Observable<IWallpaper>> getWallpaperObservable() {
        return wallpaperPublishSubject.share();
    }
}
