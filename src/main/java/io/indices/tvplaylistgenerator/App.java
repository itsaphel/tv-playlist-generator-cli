package io.indices.tvplaylistgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.indices.tvplaylistgenerator.scraper.TextFile;
import io.indices.tvplaylistgenerator.streaming.Spotify;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;

public class App {

    public static final Logger logger = Logger.getLogger(App.class.getName());

    private Gson gson;
    private File configLocation;
    private Config config;

    public void run(String name, String identifier) {
        initialise();
        List<String> songIds = new ArrayList<>();

        Spotify spotify = new Spotify(this, config.getClientId(), config.getClientSecret());
        authenticateWithSpotify(spotify);

        try {
            //songIds = new TunefindScraper(showId).getSongIds();
            songIds = new TextFile(spotify).getSongIdsFromIdentifier(identifier);
            System.out.println(String.join(",", songIds));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error getting song IDs from source", e);
            System.exit(4);
        }

        try {
            Playlist playlist = spotify.createPlaylist(name);
            spotify.addTracksToPlaylist(playlist, songIds);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
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
        } catch (IllegalAccessException | InstantiationException | FileNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Error loading config file", e);
            System.exit(2);
        }
    }

    private <T> T createOrLoadJsonFile(File file, Class<T> clazz)
        throws FileNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (file.exists()) {
            return gson.fromJson(new FileReader(file), clazz);
        } else {
            try (PrintWriter writer = new PrintWriter(file)) {
                T defaultClazz = clazz.getDeclaredConstructor().newInstance();
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

    private void authenticateWithSpotify(Spotify spotify) {
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
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.log(Level.SEVERE, "Error authenticating with Spotify", e);
            System.exit(4);
        }
    }
}
