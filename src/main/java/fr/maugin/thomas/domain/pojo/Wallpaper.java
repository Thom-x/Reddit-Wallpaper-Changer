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
    private final String link;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubreddit() {
        return subreddit;
    }

    @Override
    public String getLink() {
        return link;
    }

    public Wallpaper(String path, String title, String subredit, String link) {
        this.path = path;
        this.title = title;
        this.subreddit = subredit;
        this.link = link;
    }
}
