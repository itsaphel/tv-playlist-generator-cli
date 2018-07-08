package io.indices.tvplaylistgenerator.television;

import java.util.ArrayList;
import java.util.List;

public class Episode {

    private Season season;
    private String episodeId;
    private List<String> songIds;

    public Episode(Season season, String episodeId) {
        this.season = season;
        this.episodeId = episodeId;
        this.songIds = new ArrayList<>();
    }

    public Season getSeason() {
        return season;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void addSongId(String songId) {
        songIds.add(songId);
    }

    public void addSongIds(List<String> songIds) {
        this.songIds.addAll(songIds);
    }
}
