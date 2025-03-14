package chu.monscout.kagamin.feature

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chu.monscout.kagamin.Colors

@Composable
fun VolumeSlider(volume: Float, volumeChanged: (Float) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = MutableInteractionSource()
    val colors = SliderDefaults.colors(
        Colors.currentYukiTheme.playerButtonIcon,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIcon,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent,
        Colors.currentYukiTheme.playerButtonIconTransparent
    )
    Slider(
        value = volume,
        onValueChange = {
            volumeChanged(it)
        },
        colors = colors,
        modifier = modifier,
        interactionSource = interactionSource,
    )
}