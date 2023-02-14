package com.dat.swipe_example.navigation_manager

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.dat.swipe_example.R
import com.dat.swipe_example.databinding.ActivityMainBinding
import com.dat.swipe_example.databinding.ActivityMainNavigationManagerBinding

class MainActivityNavigationManager : AppCompatActivity() {

    private lateinit var binding: ActivityMainNavigationManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainNavigationManagerBinding.inflate(layoutInflater)
        NavigationManager.getInstance().init(this, supportFragmentManager, R.id.fragment_container)
        setContentView(binding.root)

    }


}