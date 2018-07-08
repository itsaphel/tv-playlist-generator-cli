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
     * Get Spotify song IDs for a given episode.
     *
     * @param episodeUrlComponent URL component after the show title, must include season and
     * episode number
     * @return array of Spotify song IDs (for an episode)
     */
    List<String> getSongIdsFromEpisode(String episodeUrlComponent) throws IOException;
}
