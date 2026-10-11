package com.tercad.zwyka.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconOption(
    val id: String,
    val imageVector: ImageVector,
)

val habitIconOptions = listOf(
    HabitIconOption("favorite", Icons.Filled.Favorite),
    HabitIconOption("star", Icons.Filled.Star),
    HabitIconOption("home", Icons.Filled.Home),
    HabitIconOption("check", Icons.Filled.Check),
    HabitIconOption("build", Icons.Filled.Build),
    HabitIconOption("person", Icons.Filled.Person),
)

fun habitIcon(id: String?): ImageVector? =
    habitIconOptions.firstOrNull { it.id == id }?.imageVector
