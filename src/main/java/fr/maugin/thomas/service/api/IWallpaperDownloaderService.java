package fr.maugin.thomas.service.api;

import fr.maugin.thomas.domain.api.IConfiguration;
import fr.maugin.thomas.domain.api.IWallpaper;
import rx.Observable;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public interface IWallpaperDownloaderService {

    /**
     *
     * @param config
     * @return
     */
    Observable<IWallpaper> getWallpaper(IConfiguration config);

}
