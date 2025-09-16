package com.example.tone

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

object MusicLoader {

    fun getAllAudioFromDevice(context: Context, folderPath: String? = null): List<Song> {
        val tempAudioList = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        val selectionArgs: Array<String>? = if (!folderPath.isNullOrEmpty()) {
            arrayOf("$folderPath%")
        } else {
            null
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            if (folderPath != null) "$selection AND ${MediaStore.Audio.Media.DATA} LIKE ?" else selection,
            selectionArgs,
            sortOrder
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                if (idIndex == -1 || titleIndex == -1 || pathIndex == -1) continue

                val title: String = cursor.getString(titleIndex)
                val id: Long = cursor.getLong(idIndex)
                val artist: String = if (artistIndex != -1) cursor.getString(artistIndex) ?: "Unknown Artist" else "Unknown Artist"
                val album: String = if (albumIndex != -1) cursor.getString(albumIndex) ?: "Unknown Album" else "Unknown Album"
                val duration: Long = if (durationIndex != -1) cursor.getLong(durationIndex) else 0
                val path: String = cursor.getString(pathIndex)

                // Get album art URI
                val albumId = if (albumIdIndex != -1) cursor.getLong(albumIdIndex) else 0
                val albumArtUri = getAlbumArtUri(albumId)

                // Only add valid audio files
                if (path.endsWith(".mp3") || path.endsWith(".m4a") || path.endsWith(".wav") || path.endsWith(".flac")) {
                    tempAudioList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            path = path,
                            albumArt = albumArtUri.toString()
                        )
                    )
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        return tempAudioList
    }

    fun getAvailableMusicFolders(context: Context): List<String> {
        val folders = mutableSetOf<String>()
        val contentResolver: ContentResolver = context.contentResolver

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DATA} IS NOT NULL",
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                if (pathIndex != -1) {
                    val path = cursor.getString(pathIndex)
                    if (path != null) {
                        val file = File(path)
                        val parent = file.parent
                        if (parent != null && file.exists()) {
                            folders.add(parent)
                        }
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        }

        // Add common music folders if none found
        // Add common music folders if none found
        if (folders.isEmpty()) {
            val commonFolders = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
                "/sdcard/Music",
                "/sdcard/Download",
                "/storage/emulated/0/Music",
                "/storage/emulated/0/Download"
            )

            val audioExtensions = listOf("mp3", "wav", "m4a", "flac", "ogg", "aac")

            commonFolders.forEach { folderPath ->
                val folder = File(folderPath)
                if (folder.exists() && folder.isDirectory) {
                    // Add folder path
                    folders.add(folderPath)

                    // Scan for audio files inside
                    folder.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            val ext = file.extension.lowercase()
                            if (ext in audioExtensions) {
                                Log.d("AudioScanner", "Found audio: ${file.absolutePath}")
                                // audioFiles.add(file.absolutePath)
                            }
                        }
                    }
                }
            }
        }

//        if (folders.isEmpty()) {
//            val commonFolders = listOf(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
//                "/sdcard/Music",
//                "/sdcard/Download",
//                "/storage/emulated/0/Music",
//                "/storage/emulated/0/Download"
//            )
//
//            commonFolders.forEach { folderPath ->
//                val folder = File(folderPath)
//                if (folder.exists() && folder.isDirectory) {
//                    folders.add(folderPath)
//                }
//            }
//        }

        return folders.toList().sorted()
    }

    private fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
}