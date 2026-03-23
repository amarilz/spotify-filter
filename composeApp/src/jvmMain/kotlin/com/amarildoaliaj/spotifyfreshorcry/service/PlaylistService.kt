package com.amarildoaliaj.spotifyfreshorcry.service

import com.amarildoaliaj.spotifyfreshorcry.data.model.LocalTrack
import com.amarildoaliaj.spotifyfreshorcry.data.model.toLocalTrack
import com.amarildoaliaj.spotifyfreshorcry.data.repository.FileStorageRepository
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import org.slf4j.LoggerFactory
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.specification.Paging
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import se.michaelthelin.spotify.model_objects.specification.Track

class PlaylistService(
    private val spotifyApi: SpotifyApi,
    private val fileStorageRepository: FileStorageRepository,
) {

    private val logger = LoggerFactory.getLogger(PlaylistService::class.java)

    fun run(
        blockPlaylistId: String,
        listenPlaylistId: String,
    ): String {
        logger.info(
            "Playlist sync started. blockPlaylistId={} listenPlaylistId={}",
            blockPlaylistId,
            listenPlaylistId,
        )

        val blockLocalTracks = fetchAllTracksFromPlaylist(blockPlaylistId)
        val listenedLocalTracks = fileStorageRepository.loadListenedSongs()
        val newLocalTracks = blockLocalTracks - listenedLocalTracks

        logger.info(
            "Computed playlist delta. blockTrackCount={} listenedTrackCount={} newTrackCount={} blockPlaylistId={} listenPlaylistId={}",
            blockLocalTracks.size,
            listenedLocalTracks.size,
            newLocalTracks.size,
            blockPlaylistId,
            listenPlaylistId,
        )

        if (newLocalTracks.isEmpty()) {
            val message = "No new tracks out of ${blockLocalTracks.size} from BLOCK playlist"
            logger.info(
                "Playlist sync completed with no changes. blockPlaylistId={} listenPlaylistId={}",
                blockPlaylistId,
                listenPlaylistId,
            )
            return message
        }

        addTracksToPlaylist(listenPlaylistId, newLocalTracks)

        val updatedLocalTracks = (listenedLocalTracks + newLocalTracks).toMutableSet()
        fileStorageRepository.saveAllSongs(updatedLocalTracks)

        val message = "New tracks: ${newLocalTracks.size} out of ${blockLocalTracks.size} from BLOCK playlist"

        logger.info(
            "Playlist sync completed successfully. newTrackCount={} finalTrackCount={} blockPlaylistId={} listenPlaylistId={}",
            newLocalTracks.size,
            updatedLocalTracks.size,
            blockPlaylistId,
            listenPlaylistId,
        )

        return message
    }

    private fun fetchAllTracksFromPlaylist(playlistId: String): Set<LocalTrack> {
        logger.info(
            "Fetching all tracks from playlist. playlistId={}",
            playlistId,
        )

        val tracks = buildSet {
            var offset = 0

            while (true) {
                val page = getPlaylistTracks(playlistId, offset)

                val itemsOfCurrentPage = page?.items.orEmpty()
                if (itemsOfCurrentPage.isEmpty()) {
                    break
                }

                itemsOfCurrentPage.asSequence()
                    .mapNotNull { it.track as? Track }
                    .map(Track::toLocalTrack)
                    .forEach(::add)

                offset += itemsOfCurrentPage.size

                logger.debug(
                    "Fetched playlist page. playlistId={} pageSize={} accumulatedTrackCount={} offset={} total={}",
                    playlistId,
                    itemsOfCurrentPage.size,
                    size,
                    offset,
                    page?.total,
                )

                if (page == null || offset >= page.total) {
                    break
                }
            }
        }

        logger.info(
            "Completed playlist fetch. playlistId={} trackCount={}",
            playlistId,
            tracks.size,
        )

        return tracks
    }

    private fun getPlaylistTracks(
        playlistId: String,
        offset: Int,
    ): Paging<PlaylistTrack>? = runCatching {
        spotifyApi.getPlaylistsItems(playlistId)
            .offset(offset)
            .build()
            .execute()
    }.onFailure { ex ->
        logger.error(
            "Failed to fetch playlist page. playlistId={} offset={}",
            playlistId,
            offset,
            ex,
        )
    }.getOrThrow()

    private fun addTracksToPlaylist(
        playlistId: String,
        localTracks: Set<LocalTrack>,
    ) {
        val batchSize = 100
        val uris = localTracks
            .asSequence()
            .map(LocalTrack::spotifySongUri)
            .toList()

        logger.info(
            "Adding tracks to playlist. playlistId={} trackCount={} batchSize={}",
            playlistId,
            uris.size,
            batchSize,
        )

        uris.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            val jsonArray = JsonArray().apply {
                batch.forEach { add(JsonPrimitive(it)) }
            }

            runCatching {
                spotifyApi.addItemsToPlaylist(playlistId, jsonArray)
                    .build()
                    .execute()
            }.onSuccess {
                logger.info(
                    "Added playlist batch successfully. playlistId={} batchIndex={} batchSize={}",
                    playlistId,
                    batchIndex,
                    batch.size,
                )
            }.onFailure { ex ->
                logger.error(
                    "Failed to add playlist batch. playlistId={} batchIndex={} batchSize={}",
                    playlistId,
                    batchIndex,
                    batch.size,
                    ex,
                )
            }.getOrThrow()
        }

        logger.info(
            "Completed adding tracks to playlist. playlistId={} trackCount={}",
            playlistId,
            uris.size,
        )
    }
}
