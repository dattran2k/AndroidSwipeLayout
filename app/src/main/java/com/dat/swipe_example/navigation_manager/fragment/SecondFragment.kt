package com.dat.swipe_example.navigation_manager.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.dat.swipe_example.databinding.FragmentSecondBinding
import com.dat.swipe_example.navigation_manager.NavigationManager
import com.dat.swipe_example.navigation_manager.base_fragment.BaseSwipeableFragment

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : BaseSwipeableFragment<FragmentSecondBinding>(FragmentSecondBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSecond.setOnClickListener {
            NavigationManager.getInstance().popBackStack()
        }
    }
}