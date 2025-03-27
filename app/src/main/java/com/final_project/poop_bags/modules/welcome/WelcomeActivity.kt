package com.final_project.poop_bags.modules.welcome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.final_project.poop_bags.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }
}