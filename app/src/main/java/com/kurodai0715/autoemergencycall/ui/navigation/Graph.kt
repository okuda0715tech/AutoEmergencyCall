package com.kurodai0715.autoemergencycall.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.ui.screen.home.Screen as HomeScreen
import com.kurodai0715.autoemergencycall.ui.screen.register_contact.Screen as RegisterContactScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier,
    onChangeTitle: (Int) -> Unit,
    startDestination: Any,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Home> {
            HomeScreen()

            onChangeTitle(R.string.home_screen_title)
        }

        composable<RegisterContact> {
            RegisterContactScreen()
        }
    }
}
