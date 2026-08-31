package com.example.keepsafe.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.keepsafe.R
import com.example.keepsafe.Routes
import com.example.keepsafe.viewmodel.AppPreferences
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int? = null,
    val isSplash: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    val context = LocalContext.current // Add this import: androidx.compose.ui.platform.LocalContext
    val pages = listOf(
        OnboardingPage(title = "KeepSafe", description = "", isSplash = true),
        OnboardingPage(
            title = "Offload your memory.",
            description = "Keep a precise mental map of every key, document, and charger in your house. KeepSafe remembers exactly where they are so you don't have to.",
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = "Just drop and speak.",
            description = "Put an item away, hold the mic, and say where you left it. Our AI takes care of the logging, categorizing, and sorting in seconds.",
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = "One search for the whole house.",
            description = "Whether it's you, your family, or your roommates, anyone can search the app and instantly find what they need. No more asking, \"Where is the...?\"",
            imageRes = R.drawable.onboarding_3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val finishOnboardingAction = {
        AppPreferences.setOnboardingShown(context, true)
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.ONBOARDING) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (!pages[pagerState.currentPage].isSplash) {
                OnboardingBottomBar(
                    currentPage = pagerState.currentPage,
                    pageCount = pages.size,
                    onSkip = finishOnboardingAction,
                    onNext = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            finishOnboardingAction()
                        }
                    }
                )
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { position ->
            val page = pages[position]
            if (page.isSplash) {
                SplashScreen(onGetStarted = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                })
            } else {
                OnboardingPageContent(page)
            }
        }
    }
}

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD6EB9B)), // Brand Green
        contentAlignment = Alignment.Center
    ) {
        // Only the logo image remains, centered on the green background
        Image(
            painter = painterResource(id = R.drawable.keepsafe_logo),
            contentDescription = "KeepSafe Logo",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        // Invisible clickable area to let the user tap anywhere to proceed
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {}
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (page.imageRes != null) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun OnboardingBottomBar(
    currentPage: Int,
    pageCount: Int,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onSkip) {
            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0 until pageCount) {
                Box(
                    modifier = Modifier
                        .size(if (i == currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        TextButton(onClick = onNext) {
            Text(
                text = if (currentPage == pageCount - 1) "Finish" else "Next",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}