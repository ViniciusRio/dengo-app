package com.lidiannevinicius.tamagotchi.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.lidiannevinicius.tamagotchi.R

enum class AppDestination(
    val labelRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
) {
    Home(R.string.nav_home, R.string.home_placeholder, Icons.Outlined.Home),
    Mural(R.string.nav_mural, R.string.mural_placeholder, Icons.Outlined.Edit),
    History(R.string.nav_history, R.string.history_placeholder, Icons.Outlined.History),
    Profile(R.string.nav_profile, R.string.profile_placeholder, Icons.Outlined.Person),
}
