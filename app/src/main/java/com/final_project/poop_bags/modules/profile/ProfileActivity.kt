package com.final_project.poop_bags.modules.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.final_project.poop_bags.R
import com.final_project.poop_bags.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    override fun getBottomNavItemId(): Int = R.id.navigation_profile

    override fun setupContent() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_container, ProfileFragment())
            .commit()
    }
} 