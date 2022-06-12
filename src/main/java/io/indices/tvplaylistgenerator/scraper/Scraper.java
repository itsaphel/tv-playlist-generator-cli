package io.indices.tvplaylistgenerator.scraper;

import java.io.IOException;
import java.util.List;

public interface Scraper {

    /**
     * Get Spotify song IDs for all episodes in a show
     *
     * @return array of Spotify song IDs (for entire TV show)
     */
    List<String> getSongIds() throws IOException;

    /**
     * Get Spotify song IDs for a given relevant identifier to the scraper in question
     * (eg a title of a TV show, the name of a radio station in a game, etc)
     *
     * @param identifier identifier as understood by the relevant scraper
     * @return array of corresponding Spotify song IDs
     */
    List<String> getSongIdsFromIdentifier(String identifier) throws IOException;
}
