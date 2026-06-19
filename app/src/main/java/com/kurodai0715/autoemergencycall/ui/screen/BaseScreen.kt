package com.kurodai0715.autoemergencycall.ui.screen

import androidx.compose.runtime.Composable
import com.kurodai0715.autoemergencycall.ui.navigation.AppNavGraph
import com.kurodai0715.autoemergencycall.ui.navigation.Home

@Composable
fun AppBaseScreen() {
    AppNavGraph(
        startDestination = Home
    )
}