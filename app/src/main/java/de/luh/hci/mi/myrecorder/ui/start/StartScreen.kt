package de.luh.hci.mi.myrecorder.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.luh.hci.mi.myrecorder.ui.Button
import de.luh.hci.mi.myrecorder.ui.IconButton

@Composable
fun StartScreen(
    navigateToRecord: () -> Unit,
    navigateToPlay: (recordingId: Long) -> Unit,
    navigateToRecordings: () -> Unit,
    navigateToPlaces: () -> Unit,
    viewModel: StartViewModel
) {
    val latestRecording by viewModel.latestRecording.collectAsState(initial = null)
    val recordingsCount by viewModel.recordingsCount.collectAsState(initial = 0)
    // val recordingsCount2 by viewModel.recordingsCount2.collectAsState()
    val placesCount by viewModel.placesCount.collectAsState(initial = 0)

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton("Start recording", Icons.Filled.Mic, 48.dp, navigateToRecord)
        latestRecording?.let {
            Spacer(modifier = Modifier.height(48.dp))
            IconButton("Start playing", Icons.Filled.PlayArrow, 48.dp) { navigateToPlay(it.id) }
            if (it.place == "") {
                Text("${it.startDate} - ${it.startTime}")
            } else {
                Text("${it.place} - ${it.startDate} - ${it.startTime}")
            }
        }
        if (recordingsCount > 0) {
            Spacer(modifier = Modifier.height(48.dp))
            Button("$recordingsCount recordings", navigateToRecordings)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button("$placesCount places", navigateToPlaces)
    }
}
