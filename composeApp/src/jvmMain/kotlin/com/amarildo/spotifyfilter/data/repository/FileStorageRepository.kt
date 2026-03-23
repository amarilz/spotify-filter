package com.amarildo.spotifyfilter.data.repository

import com.amarildo.spotifyfilter.data.model.LocalTrack
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.bufferedReader
import kotlin.io.path.name
import kotlin.io.path.notExists

class FileStorageRepository(
    dbFilePath: String,
) : TrackRepository {

    private val logger = LoggerFactory.getLogger(FileStorageRepository::class.java)
    private val dbPath: Path = Path.of(dbFilePath)

    override fun loadListenedSongs(): MutableSet<LocalTrack> {
        require(dbPath.toString().isNotBlank()) {
            "Database file path cannot be blank"
        }

        if (dbPath.notExists()) {
            logger.error("Database file does not exist. path={}", dbPath)
            throw IllegalStateException("The database file does not exist: $dbPath")
        }

        return runCatching {
            dbPath.bufferedReader().useLines { lines ->
                lines
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .map(::LocalTrack)
                    .toMutableSet()
            }
        }.onSuccess { tracks ->
            logger.info("Loaded listened songs from database. path={} trackCount={}", dbPath, tracks.size)
        }.onFailure { ex ->
            logger.error("Failed to load listened songs from database. path={}", dbPath, ex)
        }.getOrElse { ex ->
            throw IOException("Failed to load listened songs from database: $dbPath", ex)
        }
    }

    override fun saveAllSongs(localTracks: MutableSet<LocalTrack>) {
        writeSongsAtomically(localTracks.toSet())
    }

    private fun writeSongsAtomically(localTracks: Set<LocalTrack>) {
        val parentDir = dbPath.parent
            ?: throw IllegalStateException("Database file must have a parent directory: $dbPath")

        val sortedTrackIds = localTracks.asSequence()
            .map(LocalTrack::uniqueId)
            .distinct()
            .sorted()
            .toList()

        val tempFile = Files.createTempFile(parentDir, "${dbPath.name}.", ".tmp")

        logger.info(
            "Starting atomic database write. targetPath={} tempPath={} trackCount={}",
            dbPath,
            tempFile,
            sortedTrackIds.size,
        )

        try {
            Files.write(
                tempFile,
                sortedTrackIds,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
            )

            moveReplacingTarget(tempFile, dbPath)

            logger.info(
                "Atomic database write completed successfully. targetPath={} trackCount={}",
                dbPath,
                sortedTrackIds.size,
            )
        } catch (ex: Exception) {
            logger.error(
                "Atomic database write failed. targetPath={} tempPath={} trackCount={}",
                dbPath,
                tempFile,
                sortedTrackIds.size,
                ex,
            )

            runCatching {
                Files.deleteIfExists(tempFile)
            }.onFailure { cleanupEx ->
                logger.warn(
                    "Failed to delete temp file after write failure. tempPath={}",
                    tempFile,
                    cleanupEx,
                )
            }

            throw IOException("Failed to write database atomically: $dbPath", ex)
        }
    }

    private fun moveReplacingTarget(source: Path, target: Path) {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (ex: AtomicMoveNotSupportedException) {
            logger.warn(
                "Atomic move not supported. Falling back to non-atomic replace. source={} target={}",
                source,
                target,
            )

            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}
