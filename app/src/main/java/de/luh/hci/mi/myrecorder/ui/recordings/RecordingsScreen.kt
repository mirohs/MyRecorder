package de.luh.hci.mi.myrecorder.ui.recordings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.myrecorder.data.Recording
import de.luh.hci.mi.myrecorder.ui.Button
import de.luh.hci.mi.myrecorder.ui.IconButton

@Composable
fun RecordingsScreen(
    navigateToPlay: (recordingId: Long) -> Unit,
    viewModel: RecordingsViewModel
) {

    val recordings by viewModel.recordings.collectAsState(initial = emptyList())

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${recordings.size} recordings", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        // https://developer.android.com/develop/ui/compose/lists
        LazyColumn {
            items(recordings) { r: Recording ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (r == viewModel.selectedRecording) Color.Yellow else Color.Unspecified),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(r.place, Modifier.weight(1f), textAlign = TextAlign.Center)
                    Button("${r.startDate} - ${r.startTime}") {
                        viewModel.selectedRecording = r
                        navigateToPlay(r.id)
                    }
                    Text(r.duration, Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton("Delete", Icons.Filled.Delete, 24.dp, onClick = {
                        viewModel.selectedRecording = null
                        viewModel.deleteRecording(r.id)
                    })
                }
            }
        }
    }
}
