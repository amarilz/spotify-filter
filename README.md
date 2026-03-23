### SpotifyFreshOrCry

Short description:
 - The application manages two playlists, `BLOCK` and `LISTEN`, created by the user and configured via `application.properties`. It scans `BLOCK`, ignores anything you’ve already heard, and dumps only the fresh stuff into `LISTEN` because life’s too short to keep replaying the same songs.

Quick start (development)
1. Run the Compose Desktop app using the Gradle wrapper: `./gradlew :composeApp:run`. Or you can install this application locally.
2. Configuration:
    - The app expects a Java properties file that contains Spotify credentials and two playlist IDs.
    - Create a file (for example `config.properties`) containing the following keys:
    ```
    spotify.clientId=YOUR_SPOTIFY_CLIENT_ID
    spotify.clientSecret=YOUR_SPOTIFY_CLIENT_SECRET
    spotify.playlistId.block=BLOCK_PLAYLIST_ID
    spotify.playlistId.listen=LISTEN_PLAYLIST_ID
    ```
3. Using the app (first run)
    1. Start the application (`./gradlew :composeApp:run`).
    2. In the UI, pick the `config.properties` file you created when prompted for the configuration path.
    3. Follow the app flow to open the Spotify authorization URL in your browser, complete the OAuth flow, and paste back the redirected URL into the app when requested.
    4. Provide any required database file path (the app asks for it, you can start with an empty `.txt` file) and proceed to run the playlist-processing step.
