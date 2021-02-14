package io.indices.tvplaylistgenerator.streaming;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SnapshotResult;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.requests.data.playlists.AddTracksToPlaylistRequest;
import io.indices.tvplaylistgenerator.App;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class Spotify {

    private App app;
    private SpotifyApi spotifyApi;
    private String userId;

    public Spotify(App app, String clientId, String clientSecret) {
        this.app = app;

        spotifyApi = SpotifyApi.builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(URI.create(app.getConfig().getAppUrl()))
            .build();
    }

    public String[] authenticate(String accessToken, String refreshToken, String tokenDate,
        String tokenValidityDuration) throws IOException, SpotifyWebApiException {
        if (!accessToken.isEmpty() && !refreshToken.isEmpty()) {
            if (System.currentTimeMillis() > (Long.parseLong(tokenDate)
                + Long.parseLong(tokenValidityDuration) * 1000)) {
                AuthorizationCodeCredentials authCreds = spotifyApi.authorizationCodeRefresh()
                    .refresh_token(refreshToken).build().execute();
                spotifyApi.setAccessToken(authCreds.getAccessToken());
                spotifyApi.setRefreshToken(authCreds.getRefreshToken());

                String[] newData = new String[4];
                newData[0] = authCreds.getAccessToken();
                newData[1] = authCreds.getRefreshToken();
                newData[2] = System.currentTimeMillis() + "";
                newData[3] = authCreds.getExpiresIn() + "";

                return newData;
            } else {
                spotifyApi.setAccessToken(accessToken);
                spotifyApi.setRefreshToken(refreshToken);

                return new String[0];
            }
        }

        URI authUrl = spotifyApi.authorizationCodeUri()
            .state(RandomStringUtils.randomAlphanumeric(12))
            .scope("playlist-modify-private,playlist-read-private")
            .build().execute();

        Scanner scanner = new Scanner(System.in);
        System.out.println("============================");
        System.out.println("Please follow the following URL and provide the authorisation code:");
        System.out.print("Authorisation URL: " + authUrl.toString());
        System.out.println(" ");
        System.out.println("Authorisation code: ");
        String authCode = scanner.nextLine();

        AuthorizationCodeCredentials authCreds = spotifyApi.authorizationCode(authCode).build().execute();
        spotifyApi.setAccessToken(authCreds.getAccessToken());
        spotifyApi.setRefreshToken(authCreds.getRefreshToken());

        System.out.println("Spotify authentication successful.");

        String[] newData = new String[4];
        newData[0] = authCreds.getAccessToken();
        newData[1] = authCreds.getRefreshToken();
        newData[2] = System.currentTimeMillis() + "";
        newData[3] = authCreds.getExpiresIn() + "";

        return newData;
    }

    public String getUserId() throws IOException, SpotifyWebApiException {
        return spotifyApi.getCurrentUsersProfile().build().execute().getId();
    }

    public Playlist createPlaylist(String name) throws IOException, SpotifyWebApiException {
        _getUserId();
        String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

        return spotifyApi.createPlaylist(this.userId,
            StringUtils.capitalize(name) + " (generated by TVPG on " + date + ")")
            .collaborative(false)
            .public_(false)
            .description(
                "Playlist containing music from " + StringUtils.capitalize(name) + " up to " + date
                    +
                    ". Generated by tv-playlist-generator (https://tvpg.indices.io).")
            .build()
            .execute();
    }

    public void addTracksToPlaylist(Playlist playlist, List<String> trackIds)
        throws IOException, SpotifyWebApiException {
        _getUserId();

        int batchSize = 99;
        int numberBatchesRequired = (int) Math.floor(trackIds.size() / batchSize);
        for (int batch = 0; batch < numberBatchesRequired; batch++) {
            int fromIndex = batch * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, trackIds.size() - 1);
            List<String> subList = trackIds.subList(fromIndex, toIndex);
            String[] trackIdArray = new String[subList.size()];
            subList.toArray(trackIdArray);

            AddTracksToPlaylistRequest addTracksToPlaylistRequest = spotifyApi
                    .addTracksToPlaylist(this.userId, playlist.getId(), trackIdArray)
                    .build();

            SnapshotResult snapshotResult = addTracksToPlaylistRequest.execute();
            System.out.println(snapshotResult.getSnapshotId());
        }
    }

    private void _getUserId() throws IOException, SpotifyWebApiException {
        if (this.userId == null || this.userId.isEmpty()) {
            this.userId = getUserId();
        }
    }
}
