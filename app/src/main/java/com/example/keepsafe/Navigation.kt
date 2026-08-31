package com.example.keepsafe


import android.R.attr.type
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.keepsafe.screens.HomeScreen
import com.example.keepsafe.screens.ItemDetailScreen
import com.example.keepsafe.viewmodel.HomeViewModel

// Centralized route names to prevent typos
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val PUT_AWAY = "put_away" // Your camera flow

    const val ITEM_DETAIL = "item_detail"

    // Helper function to build the path with a specific item ID
    fun itemDetail(itemId: String) = "$ITEM_DETAIL/$itemId"
}


@Composable
fun KeepSafeNavigation() {
    val navController = rememberNavController()

    // Shared ViewModel instance so HomeScreen and ItemDetailScreen share the same item list
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }

        // Dynamic Item Detail Route definition with an 'itemId' argument
        composable(
            route = "${Routes.ITEM_DETAIL}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(
                itemId = itemId,
                navController = navController,
                viewModel = homeViewModel
            )
        }

        composable(Routes.SEARCH) {
            DummyScreen("Search Screen")
        }
        composable(Routes.HISTORY) {
            DummyScreen("History Screen")
        }
        composable(Routes.PROFILE) {
            DummyScreen("Profile Screen")
        }
        composable(Routes.PUT_AWAY) {
            DummyScreen("Camera / Put-Away Flow")
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}