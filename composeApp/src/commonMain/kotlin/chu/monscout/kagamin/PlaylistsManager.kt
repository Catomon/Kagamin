package chu.monscout.kagamin

import chu.monscout.kagamin.audio.AudioTrack
import chu.monscout.kagamin.data.PlaylistData
import chu.monscout.kagamin.data.TrackData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val playlistsFolder get() = userDataFolder.path + "/playlists/"

fun removePlaylist(name: String) {
    val playlistsFolder = File(userDataFolder.path + "/playlists")
    if (!playlistsFolder.exists())
        return

    val file = File(userDataFolder.path + "/playlists/$name.pl")
    if (!file.exists())
        return
    file.delete()
}

fun savePlaylist(name: String, tracks: Array<AudioTrack>) {
    val playlistsFolder = File(userDataFolder.path + "/playlists")
    if (!playlistsFolder.exists())
        playlistsFolder.mkdirs()

    val file = File(userDataFolder.path + "/playlists/$name.pl")
    val playlistData = PlaylistData(tracks.map { TrackData(it.uri, it.name) }.toTypedArray())
    if (!file.exists()) {
        file.createNewFile()
    }
    file.writeText(Json.encodeToString(playlistData))
}

fun loadPlaylists(): List<Pair<String, PlaylistData>> {
    val playliststs = mutableListOf<Pair<String, PlaylistData>>()
    val playlistsFolder = File(playlistsFolder)

    for (file in playlistsFolder.listFiles() ?: return playliststs) {
        if (file.isFile)
            playliststs.add(file.nameWithoutExtension to Json.decodeFromString(file.readText()))
    }

    return playliststs
}

fun loadPlaylist(name: String): PlaylistData? {
    val file = File("$playlistsFolder$name.pl")
    if (!file.exists())
        return null

    return Json.decodeFromString(file.readText())
}