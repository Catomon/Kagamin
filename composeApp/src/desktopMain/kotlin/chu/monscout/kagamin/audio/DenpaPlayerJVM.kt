package chu.monscout.kagamin.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import chu.monscout.kagamin.loadSettings
import java.io.File
import java.net.URI
import java.util.UUID

class DenpaTrackJVM(
    override val uri: String,
    override val id: String = UUID.randomUUID().toString(),
    override var author: String = "",
    override var name: String = "",
    override var duration: Long = Long.MAX_VALUE
) : DenpaTrack {

    var audioTrack: AudioTrack? = null
        set(value) {
            field = value
            author = audioTrack?.info?.author ?: ""
            name = audioTrack?.trackName ?: ""
            duration = audioTrack?.duration ?: Long.MAX_VALUE
        }

    constructor(audioTrack: AudioTrack) : this(
        audioTrack.info.uri,
        audioTrack.info.identifier,
        audioTrack.info.author,
        audioTrack.trackName
    )

    override fun equals(other: Any?): Boolean {
        return other is DenpaTrack && uri == other.uri
    }
}

class DenpaPlayerJVM : BaseDenpaPlayer<DenpaTrackJVM>() {
    private val loader = DenpaLoader(DenpaLoadResulHandler())
    private val stream = DenpaStream(loader.createAudioInputStream())
    private val audioEventListener = DenpaAudioEventListener()
    override val position: Long get() = loader.player.playingTrack?.position ?: 0L

    init {
        startDiscordRich()
        discordRich(Rich.IDLE, null)
        loader.addAudioEventListener(audioEventListener)

//        val nipah = ClassLoader.getSystemClassLoader().getResource("nipah.mp3")?.toURI().toString()
//        loader.playerManager.loadItem(nipah, EmptyAudioResultHandler())

        val settings = loadSettings()
        fade.value = settings.fade
        if (settings.random)
            playMode.value = DenpaPlayer.PlayMode.RANDOM
        else
            if (settings.repeat)
                playMode.value = DenpaPlayer.PlayMode.REPEAT_TRACK
        setVolume(settings.volume)
    }

    override fun create() {
        super.create()
    }

    override fun load(uris: List<String>) {
        uris.forEach {
            loader.loadItem(it)
        }
    }

    override fun play(track: DenpaTrackJVM): Boolean {
        if (track.audioTrack == null) {
            super.play(track)
            load(listOf(track.uri))
            return true
        }

        return if (!loader.player.startTrack(track.audioTrack!!.makeClone(), false) and super.play(track)) nextTrack() != null else true
    }

    override fun prevTrack(): DenpaTrackJVM? {
        val nextDenpaTrack = super.prevTrack()

        loader.player.stopTrack()
        play(nextDenpaTrack ?: return null)

        return nextDenpaTrack
    }

    private var filePlayTried = 0

    override fun nextTrack(): DenpaTrackJVM? {
        val nextDenpaTrack = super.nextTrack()

        nextDenpaTrack ?: let {
            stop()
            return null
        }

        loader.player.stopTrack()

        if (!nextDenpaTrack.uri.startsWith("http")) {
            if (filePlayTried >= playlist.value.size) {
                println("playlist has no files to play")
                return null
            }
            filePlayTried++

            if (!File(nextDenpaTrack.uri).exists()) {
                println("track file does not exist: ${nextDenpaTrack.uri}")

                return nextTrack()
            }
        }

        filePlayTried = 0

        play(nextDenpaTrack)

        return nextDenpaTrack
    }

    override fun queue(track: DenpaTrackJVM) {
        super.queue(track)

        //scheduler.queue(track.uri)
    }

    override fun freeQueue() {
        super.freeQueue()
    }

    override fun addToPlaylist(track: DenpaTrackJVM) {
        super.addToPlaylist(track)
    }

    override fun removeFromPlaylist(track: DenpaTrackJVM) {
        super.removeFromPlaylist(track)
    }

    override fun pause() {
        loader.player.isPaused = true

        super.pause()
    }

    override fun resume() {
        loader.player.isPaused = false

        if (currentTrack.value == null)
            nextTrack()

        super.resume()
    }

    override fun stop() {
        super.stop()

        loader.player.stopTrack()
    }

    override fun setVolume(volume: Float) {
        super.setVolume(volume)

        loader.player.volume = (110 * volume).toInt()
    }

    override fun seek(position: Long) {
        val audio = loader.player.playingTrack ?: return
        if (!audio.isSeekable) return
        audio.position = position
    }

    override fun shutdown() {
        super.shutdown()

        stopDiscordRich()
        loader.player.stopTrack()
        loader.playerManager.shutdown()
        stream.stop = true
    }

    inner class DenpaLoadResulHandler : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            if (currentTrack.value != null && currentTrack.value?.uri == track.info.uri) {
                currentTrack.value?.audioTrack = track
                play(currentTrack.value!!)

                return
            }

            addToPlaylist(DenpaTrackJVM(track))
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            playlist.tracks.forEach {
                addToPlaylist(DenpaTrackJVM(it))
            }
        }

        override fun noMatches() {

        }

        override fun loadFailed(throwable: FriendlyException) {
            throwable.printStackTrace()
        }
    }

    inner class DenpaAudioEventListener : AudioEventListener {

        private val player = loader.player

        private fun updateRich() {
            val track = player.playingTrack
            val paused = player.isPaused

            val rich = when {
                track == null -> Rich.IDLE
                paused -> Rich.PAUSED
                !paused -> Rich.LISTENING
                else -> Rich.IDLE
            }

            discordRich(rich, track)
        }

        override fun onEvent(event: AudioEvent) {
            when (event) {
                is PlayerPauseEvent -> {

                }

                is PlayerResumeEvent -> {

                }

                is TrackStartEvent -> {

                }

                is TrackEndEvent -> {
                    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                    when (event.endReason) {
                        AudioTrackEndReason.FINISHED -> {
                            if (playMode.value != DenpaPlayer.PlayMode.ONCE)
                                nextTrack()
                        }
                        AudioTrackEndReason.LOAD_FAILED -> {
                            nextTrack()
                        }

                        AudioTrackEndReason.STOPPED -> {}
                        AudioTrackEndReason.REPLACED -> {}
                        AudioTrackEndReason.CLEANUP -> {}
                    }
                }

                is TrackStuckEvent -> {
                    nextTrack()
                }

                is TrackExceptionEvent -> {
                    //nextTrack() TrackEndEvent
                }
            }

            updateRich()
        }
    }

    private class EmptyAudioResultHandler : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack?) {}
        override fun playlistLoaded(playlist: AudioPlaylist?) {}
        override fun noMatches() {}
        override fun loadFailed(exception: FriendlyException?) {}
    }
}