package com.dat.swipe_example.activity_navigation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener

import com.dat.swipe_back.fragment.model.SliderConfig
import com.dat.swipe_back.fragment.model.SliderListener
import com.dat.swipe_back.fragment.model.SliderPosition
import com.dat.swipe_example.R
import com.dat.swipe_example.activity_navigation.Activity1.Companion.currentPosition
import com.dat.swipe_example.databinding.Activity2Binding

class Activity2 : AppCompatActivity(), SliderListener {
    companion object {
        const val TAG = "Activity2"
    }
    private lateinit var binding: Activity2Binding
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Activity2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setLateConfig(getSliderConfig())
        viewPager = binding.viewPager
        viewPager.adapter = ImagePagerAdapter(this)
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        viewPager.currentItem = currentPosition
        viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
            }
        })
        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
//        if (savedInstanceState == null) {
//            postponeEnterTransition()
//        }
        prepareSharedElementTransition()
    }

    private fun prepareSharedElementTransition() {
        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // Locate the image view at the primary fragment (the ImageFragment that is currently
                    // visible). To locate the fragment, call instantiateItem with the selection position.
                    // At this stage, the method will simply return the fragment at the position and will
                    // not create a new one.
                    val currentFragment =
                        viewPager.adapter?.instantiateItem(viewPager, currentPosition) as Fragment
                    val view = currentFragment.view ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = view.findViewById(R.id.image)
                }
            })
    }

    fun getSliderConfig(): SliderConfig {
        return SliderConfig.Builder()
            .listener(this)
            .edgeSize(0.5f)
            .distanceThreshold(0.3f)
            .isDismissRightAway(true)
            .edge(false)
            .endScrimThreshHold(0.7f)
            .position(SliderPosition.TOP)
            .scrimStartAlpha(0f)
            .build()
    }

    override fun onSlideStateChanged(state: Int) {

    }

    override fun onSlideChange(percent: Float) {
        var alpha: Int
        if (percent < 0.99)
            alpha = 0
        else
            alpha = 255
        binding.viewPager.background.alpha = alpha
    }

    override fun onSlideOpened() {}

    override fun onSlideClosed() {
        binding.viewPager.background.alpha = 255
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
    }
}