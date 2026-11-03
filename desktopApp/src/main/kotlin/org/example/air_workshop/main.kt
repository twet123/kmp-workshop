package org.example.air_workshop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.example.air_workshop.di.initKoin
import java.awt.Dimension

fun main() = application {
    // Initialize Koin
    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Expense Tracker",
        state = WindowState(
            width = 1200.dp,
            height = 800.dp
        ),
    ) {
        window.minimumSize = Dimension(900, 700)
        App(isDesktop = true)
    }
}
