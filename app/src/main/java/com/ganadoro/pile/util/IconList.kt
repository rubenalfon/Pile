package com.ganadoro.pile.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppIcon(val imageVector: ImageVector) {
    Home(Icons.Filled.Home),
    AddRoad(Icons.Filled.AddRoad),
    CircleNotifications(Icons.Filled.CircleNotifications),
    Work(Icons.Filled.Work),
    DirectionsCar(Icons.Filled.DirectionsCar),
    Bedtime(Icons.Filled.Bedtime),
    Photo(Icons.Filled.Photo),
    CameraAlt(Icons.Filled.CameraAlt),
    AttachFile(Icons.Filled.AttachFile)
}

