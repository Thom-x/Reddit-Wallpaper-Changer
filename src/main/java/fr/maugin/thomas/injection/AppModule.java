package fr.maugin.thomas.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fr.maugin.thomas.App;
import fr.maugin.thomas.domain.api.IConfiguration;
import fr.maugin.thomas.domain.pojo.Configuration;
import fr.maugin.thomas.service.api.IWallpaperChanger;
import fr.maugin.thomas.service.api.IWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.RedditWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.WallpaperChanger;
import fr.maugin.thomas.utils.Utils;
import fr.maugin.thomas.view.controller.AppController;
import net.dean.jraw.http.oauth.OAuthException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: thoma
 * Date: 14/04/2016
 * Time: 23:34
 */
public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IWallpaperChanger.class).to(WallpaperChanger.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("Ctrl")
    public AppController provideController() {
        return new AppController();
    }

    @Provides
    @Singleton
    public IConfiguration provideConfiguration() {
        IConfiguration config = null;
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(Utils.getAppPath(App.class).getAbsolutePath() + "\\config.yaml"))) {
            config = yaml.loadAs(in, Configuration.class);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return config;
    }
    @Provides
    @Singleton
    public IWallpaperDownloaderService provideWallpaperDownloaderService(IConfiguration config) {
        IWallpaperDownloaderService redditWallpaperDownloaderService = null;
        try {
            redditWallpaperDownloaderService = new RedditWallpaperDownloaderService(config.getClientId(), config.getClientSecret());
            return redditWallpaperDownloaderService;
        } catch (OAuthException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return redditWallpaperDownloaderService;
    }


}