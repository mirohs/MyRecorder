package de.luh.hci.mi.myrecorder.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class UnitSignal(private val scope: CoroutineScope) {
    private val channel = Channel<Unit>(Channel.BUFFERED)
    val flow: Flow<Unit> = channel.receiveAsFlow()
    fun trigger() {
        scope.launch {
            channel.send(Unit)
        }
    }
}
