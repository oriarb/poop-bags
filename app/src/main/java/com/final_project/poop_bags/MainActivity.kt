package com.final_project.poop_bags

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupWithNavController
import com.final_project.poop_bags.databinding.ActivityMainBinding
import com.final_project.poop_bags.models.FirebaseModel
import com.final_project.poop_bags.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.NavHostFragment
import com.final_project.poop_bags.repository.StationRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.final_project.poop_bags.modules.welcome.WelcomeActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var firebaseModel: FirebaseModel
    
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var stationRepository: StationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!firebaseModel.isLoggedIn()) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        syncUserDataFromFirebase()

        binding.root.post {
            val navView: BottomNavigationView = binding.navView
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val navController = navHostFragment.navController
            navView.setupWithNavController(navController)
        }
    }
    
    private fun syncUserDataFromFirebase() {
        lifecycleScope.launch {
            try {
                val currentUserId = firebaseModel.getAuth().currentUser?.uid ?: run {
                    Log.w("MainActivity", "No user logged in")
                    return@launch
                }
                
                val userUpdateResult = runCatching {
                    userRepository.updateUserFromFirebase(currentUserId)
                }
                
                if (userUpdateResult.isFailure) {
                    Log.e("MainActivity", "Error updating user data from Firebase", userUpdateResult.exceptionOrNull())
                    Toast.makeText(
                        this@MainActivity,
                        "Using local user data - could not sync with server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                val favoritesUpdateResult = runCatching {
                    stationRepository.updateStationFavoriteStatus()
                }
                
                if (favoritesUpdateResult.isFailure) {
                    Log.e("MainActivity", "Error updating favorites status", favoritesUpdateResult.exceptionOrNull())
                }
                
                val stationsRefreshResult = runCatching {
                    stationRepository.refreshStationsFromFirebase()
                }
                
                if (stationsRefreshResult.isFailure) {
                    Log.e("MainActivity", "Error refreshing stations from Firebase", stationsRefreshResult.exceptionOrNull())
                    Toast.makeText(
                        this@MainActivity,
                        "Using local stations data - could not sync with server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "General error syncing data", e)
                Toast.makeText(
                    this@MainActivity,
                    "Error syncing data: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}