package com.dat.swipe_example.navigation_manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dat.swipe_example.R
import com.dat.swipe_example.databinding.ActivityMainNavigationManagerBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainNavigationManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainNavigationManagerBinding.inflate(layoutInflater)
        NavigationManager.getInstance().init(this, supportFragmentManager, R.id.fragment_container)
        setContentView(binding.root)

    }


}