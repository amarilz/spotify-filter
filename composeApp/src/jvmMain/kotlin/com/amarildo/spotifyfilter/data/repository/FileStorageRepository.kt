package com.amarildo.spotifyfilter.data.repository

import com.amarildo.spotifyfilter.data.model.LocalTrack
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.notExists

class FileStorageRepository(private var dbFilePath: String) : TrackRepository {

    override fun loadListenedSongs(): MutableSet<LocalTrack> {
        val path = Path(dbFilePath)
        if (path.notExists()) {
            throw IllegalStateException("The database file does not exist: $path")
        }

        return Files.readAllLines(path)
            .asSequence()
            .map { line -> line.trim() }
            .filter { it.isNotBlank() }
            .map { LocalTrack(it) }
            .toMutableSet()
    }

    override fun saveAllSongs(localTracks: MutableSet<LocalTrack>) {
        writeSongsToFile(localTracks, dbFilePath);
    }

    override fun backupSongs(localTracks: MutableSet<LocalTrack>) {
        writeSongsToFile(localTracks, createBackupFilePath());
    }

    private fun createBackupFilePath(): String {
        if (!dbFilePath.endsWith(".txt")) {
            throw IllegalArgumentException("The file path must end with '.txt'")
        }
        val dotIndex: Int = dbFilePath.lastIndexOf(".")
        return dbFilePath.take(dotIndex) + "_backup.txt";
    }

    private fun writeSongsToFile(localTracks: MutableSet<LocalTrack>, path: String) {
        val sortedTracks: List<String> = localTracks.stream()
            .map { t -> t.uniqueId }
            .sorted()
            .toList();

        Files.write(
            Path(path),
            sortedTracks,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
}
