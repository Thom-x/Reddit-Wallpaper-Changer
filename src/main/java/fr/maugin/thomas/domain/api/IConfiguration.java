package fr.maugin.thomas.domain.api;

import java.util.List;

/**
 * User: thoma
 * Date: 22/03/2016
 * Time: 20:09
 */
public interface IConfiguration {

    /**
     *
     * @return
     */
    List<String> getSubreddits();

    /**
     *
     * @return
     */
    Long getInterval();

    /**
     *
     * @return
     */
    String getClientId();

    /**
     *
     * @return
     */
    String getClientSecret();
}
