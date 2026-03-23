package com.amarildoaliaj.spotifyfreshorcry.service

import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.SpotifyHttpManager
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import java.net.URI

class SpotifyHandler(
    private val clientId: String?,
    private val clientSecret: String?,
    private val redirectUri: String = "https://www.google.com/",
    private val scope: String = "playlist-modify-public,playlist-modify-private,playlist-read-private",
) {
    private val spotifyApi: SpotifyApi

    init {
        val redirect: URI? = SpotifyHttpManager.makeUri(redirectUri)

        spotifyApi = SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirect)
            .build()
    }

    fun openLinkOnBrowser() {
        val uriRequest: AuthorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
            .scope(scope)
            .build()
        val userRedirect: URI = uriRequest.execute()

        openBrowser(userRedirect.toString())
    }

    fun getTokenApi(browserUrl: String): SpotifyApi {
        val code: String = extractCodeFromUrl(browserUrl)
        val tokenRequest: AuthorizationCodeRequest = spotifyApi.authorizationCode(code).build()
        val credentials: AuthorizationCodeCredentials = tokenRequest.execute()

        spotifyApi.accessToken = credentials.accessToken
        spotifyApi.refreshToken = credentials.refreshToken
        return spotifyApi
    }

    private fun openBrowser(url: String) {
        val os = System.getProperty("os.name").lowercase()
        val pb = when {
            os.contains("mac") -> ProcessBuilder("open", url)
            os.contains("win") -> ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url)
            os.contains("nix") || os.contains("nux") -> ProcessBuilder("xdg-open", url)
            else -> throw UnsupportedOperationException("OS not supported: $os")
        }
        pb.start()
    }

    private fun extractCodeFromUrl(redirectUrl: String): String {
        val uri = URI(redirectUrl)
        return uri.query.split("&").asSequence()
            .filter { param -> param.startsWith("code=") }
            .map { param -> param.substring("code=".length) }
            .first()
    }
}
