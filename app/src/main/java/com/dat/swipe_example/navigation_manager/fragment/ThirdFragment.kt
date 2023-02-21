package com.dat.swipe_example.navigation_manager.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.dat.swipe_back.fragment.model.SliderConfig
import com.dat.swipe_back.fragment.model.SliderListener
import com.dat.swipe_back.fragment.slider.SliderPanel
import com.dat.swipe_example.databinding.FragmentThirdBinding
import com.dat.swipe_example.navigation_manager.NavigationManager
import com.dat.swipe_example.navigation_manager.base_fragment.BaseFragment

/**
 * check layout file
 */
class ThirdFragment : BaseFragment<FragmentThirdBinding>(FragmentThirdBinding::inflate),
    SliderListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.root.setLateConfig(getSliderConfig())
    }

    fun getSliderConfig(): SliderConfig {
        return SliderConfig.Builder()
            .listener(this)
            .edgeSize(0.5f)
            .edge(false)
            .endScrimThreshHold(0.7f)
            .position(com.dat.swipe_back.fragment.model.SliderPosition.FREE)
            .scrimStartAlpha(1f)
            .build()
    }
    override fun onSlideStateChanged(state: Int) {}

    override fun onSlideChange(percent: Float) {}

    override fun onSlideOpened() {}

    override fun onSlideClosed() {
        NavigationManager.getInstance().popBackStack()
    }

}