package fr.maugin.thomas;

import com.google.common.base.Throwables;
import fr.maugin.thomas.domain.api.IConfiguration;
import fr.maugin.thomas.domain.pojo.Configuration;
import fr.maugin.thomas.service.api.IWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.RedditWallpaperDownloaderService;
import fr.maugin.thomas.service.pojo.WallpaperChanger;
import fr.maugin.thomas.utils.Utils;
import net.dean.jraw.http.oauth.OAuthException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public class App {

    public static void main(String[] args) throws OAuthException, IOException {

        final WallpaperChanger wallpaperChanger = new WallpaperChanger();

        IConfiguration config = null;
        Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream(Paths.get(Utils.getAppPath(App.class).getAbsolutePath() + "\\config.yaml") ) ) {
            config = yaml.loadAs(in, Configuration.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final IWallpaperDownloaderService app = new RedditWallpaperDownloaderService(config.getClientId(), config.getClientSecret());
        app.getWallpaper(config)
                .subscribe(wallpaper -> wallpaperChanger.changeWallpaper(new File(wallpaper.getPath())), (e) -> {
                    e.printStackTrace();
                    Throwables.propagate(e);
                });

        //waitInput();
        Thread daemonThread = new Thread(() -> {
            while(true) try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        daemonThread.setDaemon(true);
        daemonThread.run();
    }

}