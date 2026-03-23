package com.amarildoaliaj.spotifyfreshorcry.data.repository

import com.amarildoaliaj.spotifyfreshorcry.data.model.LocalTrack

interface TrackRepository {

    fun loadListenedSongs(): MutableSet<LocalTrack>
    fun saveAllSongs(localTracks: MutableSet<LocalTrack>)
}
