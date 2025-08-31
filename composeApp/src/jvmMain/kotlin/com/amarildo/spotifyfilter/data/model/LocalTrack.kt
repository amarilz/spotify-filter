package com.amarildo.spotifyfilter.data.model

import se.michaelthelin.spotify.model_objects.specification.Track

class LocalTrack(
    val singer: String,
    val title: String,
    val spotifySongUri: String,
) {
    var uniqueId: String

    init {
        this.uniqueId = (singer + title).replace("\\s+", "")
    }

    constructor(uniqueId: String) : this("", "", "") {
        this.uniqueId = uniqueId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalTrack

        return uniqueId == other.uniqueId
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }

    override fun toString(): String {
        return "LocalTrack(uniqueId=$uniqueId)"
    }
}

fun Track.toLocalTrack(): LocalTrack {
    return LocalTrack(
        this.artists[0].name,
        this.name,
        this.uri
    )
}
