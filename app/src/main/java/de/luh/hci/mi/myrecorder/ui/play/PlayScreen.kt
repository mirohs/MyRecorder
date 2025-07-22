package de.luh.hci.mi.myrecorder.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.myrecorder.ui.IconButton

@Composable
fun PlayScreen(
    viewModel: PlayViewModel
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Play", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))
        viewModel.recording?.let {
            if (it.place == "") {
                Text("${it.startDate} - ${it.startTime}")
            } else {
                Text("${it.place} - ${it.startDate} - ${it.startTime}")
            }
            if (it.latitude != "" || it.longitude != "") {
                Text("lat = ${it.latitude}, lon = ${it.longitude}")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(viewModel.currentPlaybackTime)
            Slider(
                value = viewModel.sliderPosition,
                onValueChange = viewModel::sliderChange,
                onValueChangeFinished = viewModel::sliderChangeFinished,
                valueRange = 0f..viewModel.recordingDuration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (viewModel.isPlaying) {
                IconButton("Pause", Icons.Filled.Pause, 48.dp, viewModel::pausePlaying)
            } else {
                IconButton("Resume", Icons.Filled.PlayArrow, 48.dp, viewModel::resumePlaying)
            }
            Spacer(modifier = Modifier.height(48.dp))
            IconButton("Stop", Icons.Filled.Stop, 48.dp, viewModel::stopPlaying)
        }
    }
}
