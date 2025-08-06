package de.luh.hci.mi.myrecorder.ui.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.myrecorder.ui.IconButton
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun RecordScreen(
    viewModel: RecordViewModel
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recording", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))
        if (viewModel.place == "") {
            Text("${viewModel.startDate} - ${viewModel.startTime}")
        } else {
            Text("${viewModel.place} - ${viewModel.startDate} - ${viewModel.startTime}")
        }
        if (viewModel.latitude != "" || viewModel.longitude != "") {
            Text("lat = ${viewModel.latitude}, lon = ${viewModel.longitude}")
        } else {
            Text("")
        }
        Spacer(modifier = Modifier.height(24.dp))
        ElapsedTime()
        Spacer(modifier = Modifier.height(48.dp))
        IconButton("Stop", Icons.Filled.Stop, 48.dp, viewModel::stopRecording)
    }
}

// Problem: Rotation change resets elapsed time
// Problem: Rotation change prevents stop button to end recording
@Composable
fun ElapsedTime(fontSize: TextUnit = 32.sp) {
    var elapsedTime by remember { mutableStateOf("00:00") }
    LaunchedEffect(Unit) {
        var min = 0
        var sec = 0
        while (true) {
            delay(1000L)
            sec++
            if (sec >= 60) {
                min++
                sec = 0
            }
            elapsedTime = String.format(Locale.getDefault(), "%02d:%02d", min, sec)
        }
    }
    Text(elapsedTime, fontSize = fontSize)
}
