package com.dat.swipe_example.navigation_manager.base_fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.dat.swipe_back.fragment.model.SliderConfig
import com.dat.swipe_back.fragment.model.SliderInterface
import com.dat.swipe_back.fragment.model.SliderListener
import com.dat.swipe_back.fragment.slider.SliderPanel

import com.dat.swipe_example.navigation_manager.NavigationManager


abstract class BaseSwipeableFragment<T : ViewBinding>(bindingInflater: (layoutInflater: LayoutInflater) -> T) :
    BaseFragment<T>(bindingInflater), SliderListener {
    private var rootFrameLayout: FrameLayout? = null
    private var contentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contentView = super.onCreateView(inflater, container, savedInstanceState)
        rootFrameLayout = SliderPanel(requireContext(), contentView!!, getSliderConfig()).also {
            it.removeAllViews()
            it.addView(contentView)
        }
        return rootFrameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        rootFrameLayout?.removeAllViews()
        rootFrameLayout = null

        super.onDestroyView()
    }

    protected open fun getSliderConfig(): SliderConfig {
        return SliderConfig.Builder()
            .listener(this)
            .edgeSize(0.5f)
            .edge(false)
            .endScrimThreshHold(0.7f)
            .position(com.dat.swipe_back.fragment.model.SliderPosition.LEFT_FACEBOOK)
            .touchDisabledViews(getTouchDisabledViews())
            .scrimStartAlpha(1f)
            .build()
    }

    protected open fun getTouchDisabledViews(): List<View> {
        return emptyList()
    }

    override fun onSlideStateChanged(state: Int) {}

    override fun onSlideChange(percent: Float) {}

    override fun onSlideOpened() {}

    override fun onSlideClosed() {
        NavigationManager.getInstance().popBackStack()
    }

}

