package io.indices.tvplaylistgenerator.television;

import java.util.ArrayList;
import java.util.List;

public class Season {

    private String showId;
    private String seasonId;
    private List<Episode> episodes;

    public Season(String showId, String seasonId) {
        this.showId = showId;
        this.seasonId = seasonId;
        this.episodes = new ArrayList<>();
    }

    public String getShowId() {
        return showId;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void addEpisode(Episode episode) {
        episodes.add(episode);
    }
}
