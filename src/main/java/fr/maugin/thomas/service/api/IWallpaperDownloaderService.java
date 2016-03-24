package fr.maugin.thomas.service.api;

import fr.maugin.thomas.domain.api.IConfiguration;
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
    Observable<String> getWallpaperPath(IConfiguration config);

}
