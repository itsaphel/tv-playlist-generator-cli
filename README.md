# TV Playlist Generator

This tool is to create playlists from songs from TV shows and movies.

I stumbled across wanting to make this being unable to find an app to do this.

## Usage

I've tried to make this as easy to use for a layman as possible, but due to constraints with external services (specifically the Spotify API) it's still a little complex and unlikely to be too accessible. At some point I'd like to make this into a SaaS project, allowing users to just punch in a name for a show and follow a playlist maintained by a bot. This would also allow for automatic updates when new episodes come out, would be pretty cool!

In the meantime, you'll need to generate Spotify token credentials and pop them into config.gson. Running this tool will generate a private playlist on your Spotify account. If you wish to update this, you'll need to delete the existing playlist and regenerate a new one by re-running the app. This is a Java application, so you'll need Java 8+ to run this, and it needs to be ran from a terminal (CLI).

#### Example usage

Download the latest tv-playlist-generator.jar from releases and put this in a folder.

Generate Spotify credentials by [following this guide](https://developer.spotify.com/documentation/general/guides/authorization-guide/). In short, you'll need to create a Spotify developer app which will give you a client ID and secret. Put these into your config.json file.

Open a terminal and execute: ``java -jar tv-playlist-generator.jar <showId>`` replacing <showId> with a show from [Tunefind](https://www.tunefind.com/), e.g. show ID for `https://www.tunefind.com/show/power` is `power`.

A playlist will be created in your Spotify account. A message will be printed in the CLI confirming when it's created this.

## Exit codes

There is an human-readable error message, stacktrace and error code with every error.
Numeric error codes:

* 1: General runtime error
* 2: Configuration error (invalid values, error reading config, etc.)
* 4: Error communicating with external API

## Roadmap

* Add support for movies
* Create a SaaS application to make it easier to use this service, only having to enter a show ID on a website and having the playlist maintained and updated automatically by a bot account
* Extend support to other solutions (Apple Music, Amazon Music, Tidal, YouTube)

## Contributing

Contributions are welcome. It's advised you open an issue to discuss first, though. Please ensure your code follows the [Google style guidelines](https://google.github.io/styleguide/javaguide.html).

## License

This code is released under the MIT license. See: LICENSE