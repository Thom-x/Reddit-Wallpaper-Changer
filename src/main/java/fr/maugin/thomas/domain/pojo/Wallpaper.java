package fr.maugin.thomas.domain.pojo;

import fr.maugin.thomas.domain.api.IWallpaper;

/**
 * User: thoma
 * Date: 30/03/2016
 * Time: 20:18
 */
public class Wallpaper implements IWallpaper {
    private final String path;
    private final String title;
    private final String subreddit;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public Wallpaper(String path, String title, String subredit) {
        this.path = path;
        this.title = title;
        this.subreddit = subredit;
    }
}
