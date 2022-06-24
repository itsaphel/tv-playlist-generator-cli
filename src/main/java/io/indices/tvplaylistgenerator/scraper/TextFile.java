package io.indices.tvplaylistgenerator.scraper;

import io.indices.tvplaylistgenerator.streaming.Spotify;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class TextFile implements Scraper {

    private Spotify spotify;

    public TextFile(Spotify spotify) {
        this.spotify = spotify;
    }

    @Override
    public List<String> getSongIds() throws IOException {
        return Collections.emptyList();
    }

    /**
     * The text file must contain tracks in a list, new entries on new lines.
     *
     * @param path relative file path containing tracks
     * @return list of song IDs
     */
    @Override
    public List<String> getSongIdsFromIdentifier(String path) throws IOException {
        List<String> trackList = applyReplacements(getTracks(path));
        List<String> songIds = new ArrayList<>();

        trackList.forEach(track -> {
            try {
                Paging<Track> tracks = spotify.searchForSong(track);

                if (tracks.getTotal() > 0) {
                    songIds.add(tracks.getItems()[0].getUri());
                }
            } catch (ParseException | SpotifyWebApiException | IOException e) {
                e.printStackTrace();
            }
        });

        return songIds;
    }

    private List<String> applyReplacements(List<String> songs) {
        return songs.stream()
          .map(str -> str.replace("ft.", "feat."))
          .toList();
    }

    // replacements:
    // ^\d+\s+
    // \s+\d+$
    // \t -> " "
    private List<String> getTracks(String sourceFilePath) throws IOException {
        File file = new File(sourceFilePath);
        List<String> contents = Files.readAllLines(file.toPath());

        return contents.stream()
          .map(str -> str.replaceAll("^\\d+\\s+", ""))
          .map(str -> str.replaceAll("\\s+\\d+$", ""))
          .map(str -> str.replaceAll("\t", " "))
          .toList();
    }
}
