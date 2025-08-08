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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.luh.hci.mi.myrecorder.ui.IconButton

@Composable
fun RecordScreen(
    viewModel: RecordViewModel,
    navigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.navigateBackFlow.collect {
            navigateBack()
        }
    }

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
        Text(viewModel.elapsedTime, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(48.dp))
        IconButton("Stop", Icons.Filled.Stop, 48.dp, viewModel::stopRecording)
    }
}
