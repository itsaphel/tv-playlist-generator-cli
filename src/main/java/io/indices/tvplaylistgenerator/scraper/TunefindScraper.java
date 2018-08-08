package io.indices.tvplaylistgenerator.scraper;

import io.indices.tvplaylistgenerator.App;
import io.indices.tvplaylistgenerator.television.Episode;
import io.indices.tvplaylistgenerator.television.Season;
import java.io.IOException;
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
                    List<String> episodeSongIds = getSongIdsFromEpisode(
                        season.getSeasonId() + "/" + episode.getEpisodeId());
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
        Document doc = Jsoup.connect(URL_BASE + showId + "/" + episodeUrlComponent).get();

        Elements elements = doc.select(".SongList__container___2EXi7").get(0).child(1).children();
        for (Element element : elements) {
            String redirectUrl = element.getElementsByClass("StoreLinks__spotify___2k5Xi").get(0)
                .absUrl("href");
            String url = getRedirect(redirectUrl, false);

            if (url.contains("open.spotify.com") && !url
                .contains("open.spotify.com/search/results")) {
                songIds.add(url.replaceAll("https://open.spotify.com/track/",
                    "spotify:track:")); // Spotify URI formatting
            }
        }

        return songIds;
    }

    private void populateSeasonUrls() throws IOException {
        Document doc = Jsoup.connect(URL_BASE + showId).get();
        Elements elements = doc.select(".MainList__container___hFURG").get(0).children();
        for (Element element : elements) {
            String seasonId = element.getElementsByClass("EpisodeListItem__title___32XUR").get(0)
                .child(0).attr("href").split("/")[3];
            Season season = new Season(this.showId, seasonId);
            seasons.add(season);
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
