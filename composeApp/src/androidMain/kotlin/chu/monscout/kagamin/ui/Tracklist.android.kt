package chu.monscout.kagamin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import chu.monscout.kagamin.ui.theme.Colors
import chu.monscout.kagamin.audio.AudioPlayer
import chu.monscout.kagamin.audio.AudioTrack
import chu.monscout.kagamin.ui.viewmodel.KagaminViewModel
import kagamin.composeapp.generated.resources.Res
import kagamin.composeapp.generated.resources.pause
import kagamin.composeapp.generated.resources.play
import kagamin.composeapp.generated.resources.selected
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun Tracklist(viewModel: KagaminViewModel, tracks: List<AudioTrack>, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val tracklistManager = remember { TracklistManager(coroutineScope) }
    val index =
        remember(tracks) { tracks.mapIndexed { i, track -> (track.uri to i) }.toMap() }
    val currentTrack = viewModel.currentTrack

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index[currentTrack?.uri] ?: 0)
    }

    Column(modifier) {
        if (currentTrack != null) {
            TrackItem(
                -1,
                viewModel.currentTrack!!,
                tracklistManager,
                viewModel = viewModel,
                onClick = onClick@{
                    val curTrackIndex = index[currentTrack.uri] ?: return@onClick
                    coroutineScope.launch {
                        listState.animateScrollToItem(curTrackIndex)
                    }
                }
            )
        } else {
            Box(modifier = Modifier.background(Colors.backgroundTransparent).height(32.dp).fillMaxWidth())
        }

        LazyColumn(Modifier.fillMaxWidth(), state = listState) {
            items(tracks.size, key = {
                tracks[it].uri
            }) { index ->
                val track = tracks[index]
                TrackItem(
                    index,
                    track,
                    tracklistManager,
                    viewModel = viewModel,
                    onClick = onClick@{
                        if (tracklistManager.isAnySelected) {
                            if (tracklistManager.isSelected(index, track))
                                tracklistManager.deselect(index, track)
                            else tracklistManager.select(index, track)
                            return@onClick
                        }
                        if (viewModel.isLoadingSong != null) return@onClick

                        if (track.uri.startsWith("http")) {
                            viewModel.videoUrl = track.uri
                        } else {
                            viewModel.videoUrl = ""
                            viewModel.viewModelScope.launch {
                                viewModel.isLoadingSong = track
                                viewModel.audioPlayer.play(track)
                                viewModel.isLoadingSong = null
                            }
                        }
                    },
                    modifier = Modifier
                )
            }
        }

        Box(Modifier.fillMaxSize().background(Colors.theme.listItemB))
    }
}

@Composable
actual fun TrackItem(
    index: Int,
    track: AudioTrack,
    tracklistManager: TracklistManager,
    viewModel: KagaminViewModel,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val clipboard = LocalClipboardManager.current
    val isHeader = index == -1
    val backColor = if (isHeader) Colors.backgroundTransparent else
        if (index % 2 == 0) Colors.theme.listItemA else Colors.theme.listItemB

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        if (index > -1 && viewModel.currentTrack == track) {
            Box(
                Modifier
                    .height(32.dp)
                    .clip(
                        RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
                    )
                    .background(Colors.backgroundTransparent)
                    .clickable {
                        viewModel.onPlayPause()
                    }, contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(if (viewModel.playState == AudioPlayer.PlayState.PAUSED) Res.drawable.pause else Res.drawable.play),
                    "track playback state icon",
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(Colors.theme.buttonIcon)
                )
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(color = backColor)
                .clickable {
                    onClick()
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                track.name,
                fontSize = 12.sp,
                color = Colors.text,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterStart),
                overflow = TextOverflow.Ellipsis,
            )



            Row(
                Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tracklistManager.selected.contains(index))
                    Icon(painterResource(Res.drawable.selected), null)
            }
        }
    }
}