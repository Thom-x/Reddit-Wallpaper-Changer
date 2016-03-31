package fr.maugin.thomas.view.controller;

import com.google.common.base.Throwables;
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
import javafx.scene.layout.Pane;
import org.yaml.snakeyaml.Yaml;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * User: thoma
 * Date: 30/03/2016
 * Time: 18:59
 */
public class AppController implements Initializable {

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

    private PublishSubject<Observable<IWallpaper>> wallpaperPublishSubject = PublishSubject.create();
    private IWallpaperDownloaderService app;
    private IConfiguration config;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        next.setVisible(false);
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

        subreddit.setVisible(false);
        title.setVisible(false);

        app = new RedditWallpaperDownloaderService(config.getClientId(), config.getClientSecret());

        wallpaperPublishSubject
                .switchMap(wallpapers -> wallpapers)
                .observeOn(JavaFxScheduler.getInstance())
                .subscribe(wallpaper -> {
                    final Image image = new Image("file:///" + wallpaper.getPath());
                    next.setVisible(true);
                    splash.setVisible(false);
                    subreddit.setVisible(true);
                    title.setVisible(true);
                    imageView.setImage(image);
                    subreddit.setText(wallpaper.getSubreddit());
                    subreddit.setTooltip(new Tooltip(wallpaper.getSubreddit()));
                    title.setText(wallpaper.getTitle());
                    title.setTooltip(new Tooltip(wallpaper.getTitle()));
                    wallpaperChanger.changeWallpaper(new File(wallpaper.getPath()));
                }, (e) -> {
                    e.printStackTrace();
                    Throwables.propagate(e);
                });

        wallpaperPublishSubject.onNext(app.getWallpaper(config).subscribeOn(Schedulers.io()));
    }

    @FXML
    public void handleNext(Event event) {
        wallpaperPublishSubject.onNext(app.getWallpaper(config).subscribeOn(Schedulers.io()));
    }
}
