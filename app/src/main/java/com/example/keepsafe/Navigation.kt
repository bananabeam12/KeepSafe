package com.example.keepsafe


import android.R.attr.type
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.keepsafe.screens.HistoryScreen
import com.example.keepsafe.screens.HomeScreen
import com.example.keepsafe.screens.ItemDetailScreen
import com.example.keepsafe.screens.ItemRetrievedScreen
import com.example.keepsafe.screens.LoginScreen
import com.example.keepsafe.screens.ManageProfileScreen
import com.example.keepsafe.screens.OnboardingScreen
import com.example.keepsafe.screens.ProfileScreen
import com.example.keepsafe.screens.PutAwayDetailsScreen
import com.example.keepsafe.screens.PutAwaySuccessScreen
import com.example.keepsafe.screens.RegisterScreen
import com.example.keepsafe.screens.RoomDetailScreen
import com.example.keepsafe.screens.SearchScreen
import com.example.keepsafe.viewmodel.AppPreferences
import com.example.keepsafe.viewmodel.HomeViewModel

// Centralized route names to prevent typos
object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SEARCH = "search"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val ADD_ITEM_DETAILS = "add_item_details"
    const val ITEM_DETAIL = "item_detail"
    fun itemDetail(itemId: String) = "$ITEM_DETAIL/$itemId"

    const val ITEM_RETRIEVED = "item_retrieved"
    fun itemRetrieved(itemId: String) = "$ITEM_RETRIEVED/$itemId"

    const val PUT_AWAY_SUCCESS = "put_away_success"

    const val ROOM_DETAIL = "room_detail/{roomName}"
    fun roomDetail(roomName: String) = "room_detail/$roomName"

    const val MANAGE_PROFILE = "manage_profile"

}


@Composable
fun KeepSafeNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()

    // Check if onboarding was already completed
    val hasSeenOnboarding = AppPreferences.isOnboardingShown(context)
    val startDest = if (hasSeenOnboarding) Routes.LOGIN else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

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

        composable(
            route = "${Routes.ITEM_RETRIEVED}/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemRetrievedScreen(
                itemId = itemId,
                navController = navController,
                viewModel = homeViewModel // Passed so you can call fastPutBack!
            )
        }

        composable(Routes.ADD_ITEM_DETAILS) {
            PutAwayDetailsScreen(navController = navController, viewModel = homeViewModel)
        }

        composable(Routes.PUT_AWAY_SUCCESS) {
            PutAwaySuccessScreen(navController = navController)
        }

        composable(Routes.SEARCH) {
            SearchScreen(navController, viewModel = homeViewModel)
        }

        composable(Routes.ROOM_DETAIL) { backStackEntry ->
            val roomName = backStackEntry.arguments?.getString("roomName") ?: "Unknown Room"
            RoomDetailScreen(
                navController = navController,
                viewModel = homeViewModel,
                roomName = roomName
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(navController)
        }

        composable(Routes.MANAGE_PROFILE) {
            ManageProfileScreen(navController = navController)
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}