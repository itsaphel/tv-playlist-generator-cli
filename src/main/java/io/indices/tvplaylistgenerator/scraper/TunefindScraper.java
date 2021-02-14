package io.indices.tvplaylistgenerator.scraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.indices.tvplaylistgenerator.App;
import io.indices.tvplaylistgenerator.television.Episode;
import io.indices.tvplaylistgenerator.television.Season;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TunefindScraper implements Scraper {

    private static String URL_BASE = "https://www.tunefind.com/show/";
    private String showId;
    private List<Season> seasons;

    /**
     * Create a new TunefindScraper.
     *
     * @param showId ID of the show on the website
     */
    public TunefindScraper(String showId) {
        this.showId = showId;
        this.seasons = new ArrayList<>();
    }

    /**
     * @see Scraper#getSongIds()
     */
    public List<String> getSongIds() throws IOException {
        List<String> songIds = new ArrayList<>();
        populateSeasonUrls();
        populateEpisodeUrls();

        seasons.forEach(season -> {
            season.getEpisodes().forEach(episode -> {
                try {
                    List<String> episodeSongIds = getSongIdsFromEpisode(episode.getEpisodeId());
                    episode.addSongIds(episodeSongIds);
                    songIds.addAll(episodeSongIds);
                } catch (IOException e) {
                    App.logger.log(Level.SEVERE, "Error getting episode info from Tunefind", e);
                }
            });
        });

        return songIds;
    }

    /**
     * @see Scraper#getSongIdsFromEpisode(String)
     */
    public List<String> getSongIdsFromEpisode(String episodeUrlComponent) throws IOException {
        List<String> songIds = new ArrayList<>();

        InputStream input = new URL("https://www.tunefind.com/api/frontend/episode/" + episodeUrlComponent + "?fields=song-events,questions,nextPrev").openStream();
        Reader reader = new InputStreamReader(input, "UTF-8");
        JsonParser parser = new JsonParser();
        JsonObject rootObj = parser.parse(reader).getAsJsonObject().getAsJsonObject("episode");
        input.close();
        JsonArray songEvents = rootObj.getAsJsonArray("song_events");

        for (JsonElement elm : songEvents) {
            JsonObject song = elm.getAsJsonObject().getAsJsonObject("song");
            JsonElement forwardUrl = song.get("spotify");
            if (!(forwardUrl instanceof JsonNull)) {
                String redirectUrl = "https://www.tunefind.com" + forwardUrl.getAsString();
                String url = getRedirect(redirectUrl, false);

                if (url.contains("open.spotify.com") && !url.contains("open.spotify.com/search/results")) {
                    songIds.add(url.replaceAll("https://open.spotify.com/track/", "spotify:track:")); // Spotify URI formatting
                }
            }
        }

        return songIds;
    }

    private void populateSeasonUrls() throws IOException {
        Document doc = Jsoup.connect(URL_BASE + showId).get();
        Elements elements = doc.select(".MainList__container___hFURG").get(0).children();
        for (Element element : elements) {
            Elements seasonElm = element.getElementsByClass("EpisodeListItem__title___32XUR");
            if (!seasonElm.isEmpty()) {
                String seasonId = seasonElm.get(0).child(0).attr("href").split("/")[3];
                Season season = new Season(this.showId, seasonId);
                seasons.add(season);
            }
        }
    }

    private void populateEpisodeUrls() throws IOException {
        for (Season season : seasons) {
            Document doc = Jsoup.connect(URL_BASE + season.getShowId() + "/" + season.getSeasonId())
                .get();
            Elements elements = doc.select(".MainList__container___hFURG").get(0).children();
            for (Element element : elements) {
                Elements episodeElm = element.getElementsByClass("EpisodeListItem__title___32XUR");
                if (episodeElm.hasText() && !episodeElm.get(0).children().isEmpty()) {
                    String episodeId = episodeElm.get(0).child(0).attr("href").split("/")[4];
                    Episode episode = new Episode(season, episodeId);
                    season.addEpisode(episode);
                }
            }
        }
    }

    private String getRedirect(String url, boolean repeatFollow) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(false);
        conn.addRequestProperty("User-Agent", "Mozilla");
        conn.connect();

        int status = conn.getResponseCode();
        conn.disconnect();

        String redirectUrl = conn.getHeaderField("Location");

        if (repeatFollow) {
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    return getRedirect(redirectUrl, true);
                } else {
                    return "";
                }
            }

            return url;
        }

        return redirectUrl;
    }
}
