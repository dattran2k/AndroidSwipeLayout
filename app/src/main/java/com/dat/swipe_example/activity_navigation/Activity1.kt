package com.dat.swipe_example.activity_navigation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.recyclerview.widget.RecyclerView
import com.dat.swipe_example.R
import com.dat.swipe_example.databinding.Activity1Binding

class Activity1 : AppCompatActivity() {
    companion object {
        const val TAG = "Activity1"
        var currentPosition = 0
    }
    private lateinit var binding: Activity1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Activity1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.adapter = GridAdapter(this)

        scrollToPosition()
        postponeEnterTransition()
        prepareTransitions()
    }
    private fun prepareTransitions() {
        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    // Locate the ViewHolder for the clicked position.
                    val selectedViewHolder: RecyclerView.ViewHolder =
                        binding.recyclerView
                            .findViewHolderForAdapterPosition(currentPosition)
                            ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] =
                        selectedViewHolder.itemView.findViewById(/* id = */ R.id.card_image)
                }
            })
    }

    private fun scrollToPosition() {
        binding.recyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                binding.recyclerView.removeOnLayoutChangeListener(this)
                val layoutManager: RecyclerView.LayoutManager = binding.recyclerView.layoutManager!!
                val viewAtPosition = layoutManager.findViewByPosition(currentPosition)
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)
                ) {
                    binding.recyclerView.post(Runnable { layoutManager.scrollToPosition(currentPosition) })
                }
            }
        })
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