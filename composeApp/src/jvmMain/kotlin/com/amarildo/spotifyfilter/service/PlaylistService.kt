package com.amarildo.spotifyfilter.service

import com.amarildo.spotifyfilter.data.model.LocalTrack
import com.amarildo.spotifyfilter.data.model.toLocalTrack
import com.amarildo.spotifyfilter.data.repository.FileStorageRepository
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.specification.Paging
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import se.michaelthelin.spotify.model_objects.specification.Track
import kotlin.math.min

class PlaylistService(
    private val spotifyApi: SpotifyApi,
    private val fileStorageRepository: FileStorageRepository,
) {
    fun run(
        blockPlaylistId: String,
        listenPlaylistId: String,
    ) {
        val blockLocalTracks: MutableSet<LocalTrack> = fetchAllTracksFromPlaylist(blockPlaylistId)
        val listenedLocalTracks: MutableSet<LocalTrack> = fileStorageRepository.loadListenedSongs()
        val newLocalTracks: MutableSet<LocalTrack> = getNewTracks(blockLocalTracks, listenedLocalTracks)

        if (newLocalTracks.isEmpty()) {
            return
        }

        try {
            addTracksToPlaylist(listenPlaylistId, newLocalTracks)
        } catch (ex: Exception) {
            throw ex
        }

        fileStorageRepository.backupSongs(listenedLocalTracks)
        listenedLocalTracks.addAll(newLocalTracks)
        fileStorageRepository.saveAllSongs(listenedLocalTracks)
    }

    private fun fetchAllTracksFromPlaylist(playlistId: String): MutableSet<LocalTrack> {
        val result: MutableSet<LocalTrack> = mutableSetOf()
        var offset = 0

        while (true) {
            val page: Paging<PlaylistTrack>? = getPlaylistTracks(playlistId, offset)
            if (page == null || page.items == null || page.items.size == 0) {
                break
            }

            val itemsBefore: Int = result.size

            page.items.asSequence()
                .map { it.track }
                .filter { t -> t is Track }
                .map { t -> (t as Track).toLocalTrack() }
                .forEach { result.add(it) }

            val itemAfter: Int = result.size

            offset += page.items.size
            if (offset >= page.total) {
                break
            }
        }
        return result
    }

    private fun getPlaylistTracks(
        playlistId: String,
        offset: Int,
    ): Paging<PlaylistTrack>? {
        try {
            return spotifyApi.getPlaylistsItems(playlistId)
                .offset(offset)
                .build()
                .execute()
        } catch (ex: Exception) {
            return null
        }
    }

    private fun getNewTracks(
        blockLocalTracks: MutableSet<LocalTrack>,
        listenedLocalTracks: MutableSet<LocalTrack>,
    ): MutableSet<LocalTrack> = blockLocalTracks.asSequence()
        .filter { track -> !listenedLocalTracks.contains(track) }
        .toSet() as MutableSet<LocalTrack>

    private fun addTracksToPlaylist(
        playlistId: String,
        localTracks: MutableSet<LocalTrack>,
    ) {
        val batchSize = 100
        val uris: List<String> = localTracks.asSequence()
            .map { it.spotifySongUri }
            .toList()

        for (i in 0 until uris.size step batchSize) {
            val batch: List<String> = uris.subList(i, min(i + batchSize, uris.size))
            val jsonArray: JsonArray = JsonArray()
            batch.forEach { uri -> jsonArray.add(JsonPrimitive(uri)) }

            spotifyApi.addItemsToPlaylist(playlistId, jsonArray)
                .build()
                .execute()
        }
    }
}
