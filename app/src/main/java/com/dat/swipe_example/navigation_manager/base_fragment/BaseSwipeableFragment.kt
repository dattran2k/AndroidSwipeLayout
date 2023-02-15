package com.dat.swipe_example.navigation_manager.base_fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.dat.swipe.model.SliderConfig
import com.dat.swipe.model.SliderInterface
import com.dat.swipe.model.SliderListener
import com.dat.swipe.model.SliderPosition
import com.dat.swipe.slider.SliderPanel
import com.dat.swipe_example.navigation_manager.NavigationManager


abstract class BaseSwipeableFragment<T : ViewBinding>(bindingInflater: (layoutInflater: LayoutInflater) -> T) :
    BaseFragment<T>(bindingInflater), SliderListener {
    private var rootFrameLayout: FrameLayout? = null
    private var contentView: View? = null

    protected var sliderInterface: SliderInterface? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contentView = super.onCreateView(inflater, container, savedInstanceState)
        rootFrameLayout = FrameLayout(requireContext()).also {
            it.setBackgroundColor(Color.TRANSPARENT)
            it.removeAllViews()
            it.addView(contentView)
        }
        return rootFrameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSliderPanel()
    }

    private fun setUpSliderPanel() {
        contentView?.let { content ->
            if (sliderInterface == null) {
                val parent = content.parent as? ViewGroup ?: return
                val params = content.layoutParams
                parent.removeView(content)
                // Setup the slider panel and attach it
                val panel = SliderPanel(requireContext(), content, getSliderConfig())

                panel.addView(content)
                parent.addView(panel, 0, params)

                panel.setOnPanelSlideListener(FragmentPanelSlideListener(getSliderConfig()))

                sliderInterface = panel.defaultInterface
            }
        }
    }

    override fun onDestroyView() {
        rootFrameLayout?.removeAllViews()
        rootFrameLayout = null
        sliderInterface = null
        super.onDestroyView()
    }

    protected open fun getSliderConfig(): SliderConfig {
        return SliderConfig.Builder()
            .listener(this)
            .edgeSize(0.5f)
            .edge(false)
            .endScrimThreshHold(0.7f)
            .position(SliderPosition.LEFT_FACEBOOK)
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

    override fun onSlideClosed(): Boolean {
        return false
    }

    inner class FragmentPanelSlideListener(
        private val config: SliderConfig
    ) : SliderPanel.OnPanelSlideListener {
        override fun onStateChanged(state: Int) {
            config.listener?.onSlideStateChanged(state)
        }

        override fun onClosed() {
            if (config.listener != null) {
                if (config.listener?.onSlideClosed() == true) {
                    return
                }
            }
            Log.e(TAG, "onClosed: ")
            NavigationManager.getInstance().popBackStack()
        }

        override fun onOpened() {
            Log.e(TAG, "onOpened: ")
            config.listener?.onSlideOpened()
        }

        override fun onSlideChange(percent: Float) {
//            Log.e(TAG, "onSlideChange: $percent")
            config.listener?.onSlideChange(percent)
        }
    }
}

