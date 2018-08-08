package io.indices.tvplaylistgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import io.indices.tvplaylistgenerator.scraper.TunefindScraper;
import io.indices.tvplaylistgenerator.streaming.Spotify;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static final Logger logger = Logger.getLogger(App.class.getName());

    private Gson gson;
    private File configLocation;
    private Config config;

    public void run(String showId) {
        initialise();
        List<String> songIds = new ArrayList<>();

        //songIds.addAll(Arrays.asList(spots.split(",")));

        try {
            songIds = new TunefindScraper(showId).getSongIds();
            System.out.println(String.join(",", songIds));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error getting song IDs from TunefindScraper", e);
            System.exit(4);
        }

        Spotify spotify = new Spotify(this, config.getClientId(), config.getClientSecret());

        try {
            String[] newData = spotify
                .authenticate(config.getAccessToken(), config.getRefreshToken(),
                    config.getTokenCreationDate(), config.getTokenValidityDuration());
            if (newData.length > 0) {
                config.setAccessToken(newData[0]);
                config.setRefreshToken(newData[1]);
                config.setTokenCreationDate(newData[2]);
                config.setTokenValidityDuration(newData[3]);
                saveJsonToFile(configLocation, config, Config.class);
            }
        } catch (IOException | SpotifyWebApiException e) {
            logger.log(Level.SEVERE, "Error authenticating with Spotify", e);
            System.exit(4);
        }

        try {
            Playlist playlist = spotify.createPlaylist(showId);
            spotify.addTracksToPlaylist(playlist, songIds);
        } catch (IOException | SpotifyWebApiException e) {
            logger.log(Level.SEVERE, "Error creating/adding songs to playlist", e);
            System.exit(4);
        }
    }

    public Config getConfig() {
        return config;
    }

    private void initialise() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        Path currentDir = Paths.get(".").toAbsolutePath().normalize();

        try {
            configLocation = currentDir.resolve("config.json").toFile();
            config = createOrLoadJsonFile(configLocation, Config.class);
        } catch (IllegalAccessException | InstantiationException | FileNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading config file", e);
            System.exit(2);
        }
    }

    private <T> T createOrLoadJsonFile(File file, Class<T> clazz)
        throws FileNotFoundException, IllegalAccessException, InstantiationException {
        if (file.exists()) {
            return gson.fromJson(new FileReader(file), clazz);
        } else {
            try (PrintWriter writer = new PrintWriter(file)) {
                T defaultClazz = clazz.newInstance();
                writer.println(gson.toJson(defaultClazz, clazz));
                return defaultClazz;
            }
        }
    }

    private <T> void saveJsonToFile(File location, Object clazz, Type type) throws IOException {
        try (Writer writer = new FileWriter(location, false)) {
            gson.toJson(clazz, writer);
        }
    }
}
