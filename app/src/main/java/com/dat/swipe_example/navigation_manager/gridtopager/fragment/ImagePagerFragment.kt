package com.dat.swipe_example.navigation_manager.gridtopager.fragment

import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.dat.swipe_example.R
import com.dat.swipe_example.databinding.FragmentPagerBinding
import com.dat.swipe_example.navigation_manager.base_fragment.BaseSwipeableFragment
import com.dat.swipe_example.navigation_manager.gridtopager.MainActivity.Companion.currentPosition
import com.dat.swipe_example.navigation_manager.gridtopager.adapter.ImagePagerAdapter
import androidx.activity.addCallback

class ImagePagerFragment : BaseSwipeableFragment<FragmentPagerBinding>(FragmentPagerBinding::inflate) {
    private var viewPager: ViewPager? = null

    override fun onCreateView(binding: FragmentPagerBinding) {
        super.onCreateView(binding)
        viewPager = binding.viewPager
        viewPager!!.adapter = ImagePagerAdapter(this)
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        viewPager!!.currentItem = currentPosition
        viewPager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
            }
        })
        prepareSharedElementTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
        val fragmentManager = requireActivity().supportFragmentManager
        val backStackEntryCount = fragmentManager.backStackEntryCount
        for (index in 0 until backStackEntryCount) {
            val fragmentName = fragmentManager.getBackStackEntryAt(index).name
            Log.d("Fragment Name", fragmentName ?: "Unknown")
        }
        requireActivity().onBackPressedDispatcher.addCallback(this@ImagePagerFragment) {
            val firstFragment =
                requireActivity().supportFragmentManager.fragments.find { it is GridFragment }
            requireActivity().supportFragmentManager.popBackStack()
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(this@ImagePagerFragment) // Remove secondFragment when back from it
                .commit()
//            requireActivity().supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container,firstFragment!!)
//                .commit()
//            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(
            context
        ).inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

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
                    val currentFragment = viewPager!!.adapter
                        ?.instantiateItem(viewPager!!, currentPosition) as Fragment
                    val view = currentFragment.view ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = view.findViewById(R.id.image)
                }
            })
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                // Show firstFragment lên khi transition hoàn thành
                val firstFragment =
                    requireActivity().supportFragmentManager.fragments.find { it is GridFragment }
                firstFragment?.let {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .show(it)
                        .commit()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
    }
}