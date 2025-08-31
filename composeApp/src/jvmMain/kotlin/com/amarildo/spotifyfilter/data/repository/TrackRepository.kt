package com.amarildo.spotifyfilter.data.repository

import com.amarildo.spotifyfilter.data.model.LocalTrack

interface TrackRepository {

    fun loadListenedSongs(): MutableSet<LocalTrack>
    fun saveAllSongs(localTracks: MutableSet<LocalTrack>)
    fun backupSongs(localTracks: MutableSet<LocalTrack>)
}
