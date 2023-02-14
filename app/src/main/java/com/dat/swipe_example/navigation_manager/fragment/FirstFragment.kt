package com.dat.swipe_example.navigation_manager.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dat.swipe_example.databinding.FragmentFirstBinding
import com.dat.swipe_example.navigation_manager.NavigationManager
import com.dat.swipe_example.navigation_manager.base_fragment.BaseFragment
import com.dat.swipe_example.navigation_manager.base_fragment.BaseSwipeableFragment

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : BaseFragment<FragmentFirstBinding>(FragmentFirstBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFirst.setOnClickListener {
            NavigationManager.getInstance().openFragment(SecondFragment())
        }
    }

}