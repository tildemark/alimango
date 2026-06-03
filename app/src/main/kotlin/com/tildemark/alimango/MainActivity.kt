package com.tildemark.alimango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tildemark.alimango.domain.repository.UserRepository
import com.tildemark.alimango.ui.navigation.AlimangoNavGraph
import com.tildemark.alimango.ui.navigation.Screen
import com.tildemark.alimango.ui.theme.AlimangoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Blockingly get PAT to determine starting route without UI flash
        val savedPat = runBlocking { userRepository.getSavedPat() }
        val startRoute = if (!savedPat.isNullOrBlank()) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            AlimangoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    AlimangoNavGraph(
                        navController = navController,
                        startDestination = startRoute
                    )
                }
            }
        }
    }
}
