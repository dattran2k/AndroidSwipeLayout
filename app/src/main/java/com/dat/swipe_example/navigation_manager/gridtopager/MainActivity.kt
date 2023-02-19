package com.dat.swipe_example.navigation_manager.gridtopager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dat.swipe_example.R
import com.dat.swipe_example.navigation_manager.NavigationManager
import com.dat.swipe_example.navigation_manager.gridtopager.fragment.GridFragment

class MainActivity : AppCompatActivity() {
    companion object {
        /**
         * Holds the current image position to be shared between the grid and the pager fragments. This
         * position updated when a grid item is clicked, or when paging the pager.
         *
         * In this demo app, the position always points to an image index at the [ ] class.
         */
        var currentPosition = 0
        private const val KEY_CURRENT_POSITION =
            "com.dat.swipe_example.navigation_manager.gridtopager.key.currentPosition"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_demo)
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0)
            // Return here to prevent adding additional GridFragments when changing orientation.
            return
        }
        NavigationManager.getInstance().init(this, supportFragmentManager, R.id.fragment_container)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, GridFragment(),"GridFragment")
            .addToBackStack("GridFragment")
            .commit()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_POSITION, currentPosition)
    }

}