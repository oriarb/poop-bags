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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var firebaseModel: FirebaseModel
    
    @Inject
    lateinit var userRepository: UserRepository

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
                val currentUserId = firebaseModel.getAuth().currentUser?.uid
                if (currentUserId != null) {
                    userRepository.updateUserFromFirebase(currentUserId)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "שגיאה בסנכרון נתוני המשתמש: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    fun navigateToBottomNavDestination(destinationId: Int) {
        binding.navView.selectedItemId = destinationId
    }
}