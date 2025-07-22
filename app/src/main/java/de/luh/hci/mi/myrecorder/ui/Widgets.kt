package de.luh.hci.mi.myrecorder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


@Composable
fun IconButton(description: String, icon: ImageVector, size: Dp, onClick: () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun Button(label: String, click: () -> Unit) {
    androidx.compose.material3.Button(onClick = click) {
        Text(label)
    }
}

@Composable
fun DiscreteSlider(tickValues: List<Int>, sliderPosition: MutableIntState, setValue: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            value = sliderPosition.intValue.toFloat(),
            onValueChange = { sliderPosition.intValue = it.roundToInt() },
            onValueChangeFinished = setValue,
            valueRange = 0f..(tickValues.lastIndex.toFloat()),
            steps = tickValues.size - 2, // number of points in addition to end points
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tickValues.forEach { label ->
                Text(label.toString(), fontSize = 12.sp)
            }
        }
    }
}
