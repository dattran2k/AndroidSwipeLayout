package com.dat.swipe_example.navigation_manager.gridtopager.adapter

import android.graphics.drawable.Drawable
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dat.swipe_example.R
import com.dat.swipe_example.navigation_manager.gridtopager.MainActivity.Companion.currentPosition
import com.dat.swipe_example.navigation_manager.gridtopager.fragment.ImagePagerFragment
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A fragment for displaying a grid of images.
 */
class GridAdapter(fragment: Fragment) :
    RecyclerView.Adapter<GridAdapter.ImageViewHolder?>() {
    /**
     * A listener that is attached to all ViewHolders to handle image loading events and clicks.
     */
    interface ViewHolderListener {
        fun onLoadCompleted(view: ImageView?, adapterPosition: Int)
        fun onItemClicked(view: View?, adapterPosition: Int)
    }

    private val requestManager: RequestManager
    private val viewHolderListener: ViewHolderListener

    /**
     * Constructs a new grid adapter for the given [Fragment].
     */
    init {
        requestManager = Glide.with(fragment)
        viewHolderListener = ViewHolderListenerImpl(fragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_card, parent, false)
        return ImageViewHolder(view, requestManager, viewHolderListener)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return ImageData.IMAGE_DRAWABLES.size
    }

    /**
     * Default [ViewHolderListener] implementation.
     */
    class ViewHolderListenerImpl constructor(private val fragment: Fragment) :
        ViewHolderListener {
        private val enterTransitionStarted: AtomicBoolean = AtomicBoolean()

        override fun onLoadCompleted(view: ImageView?, position: Int) {
            // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
            if (currentPosition != position) {
                return
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return
            }
            fragment.startPostponedEnterTransition()
        }

        /**
         * Handles a view click by setting the current position to the given `position` and
         * starting a [ImagePagerFragment] which displays the image at the position.
         *
         * @param view     the clicked [ImageView] (the shared element view will be re-mapped at the
         * GridFragment's SharedElementCallback)
         * @param position the selected view position
         */
        override fun onItemClicked(view: View?, position: Int) {
            // Update the position.
            currentPosition = position
            val transitioningView = view!!.findViewById<ImageView>(R.id.card_image)


            val secondFragment = ImagePagerFragment()
            fragment.requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, secondFragment)
                .commit()
            val transition = TransitionInflater.from(fragment.context)
                .inflateTransition(android.R.transition.move)
            fragment.sharedElementReturnTransition = transition
            fragment.exitTransition = null // Remove exit transition to keep firstFragment visible
            secondFragment.sharedElementEnterTransition = transition
            secondFragment.enterTransition = null // Remove enter transition to make secondFragment transparent
            fragment.allowEnterTransitionOverlap = true
            fragment.allowReturnTransitionOverlap = true
            secondFragment.allowEnterTransitionOverlap = true
            secondFragment.allowReturnTransitionOverlap = true
            fragment.requireActivity().supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true) // Optimize for shared element transition
                .addSharedElement(transitioningView!!, transitioningView.transitionName)
                .show(secondFragment)
                .hide(fragment)
                .addToBackStack(secondFragment.TAG)
                .commit()


//            (fragment.exitTransition as TransitionSet?)!!.excludeTarget(view, true)
//            fragment.requireFragmentManager()
//                .beginTransaction()
//                .setReorderingAllowed(true) // Optimize for shared element transition
//                .addSharedElement(transitioningView!!, transitioningView.transitionName)
//                .replace(
//                    R.id.fragment_container, ImagePagerFragment(), ImagePagerFragment::class.java
//                        .simpleName
//                )
//                .addToBackStack(null)
//                .commit()

        }
    }

    /**
     * ViewHolder for the grid's images.
     */
    inner class ImageViewHolder(
        itemView: View, requestManager: RequestManager,
        viewHolderListener: ViewHolderListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val image: ImageView
        private val requestManager: RequestManager
        private val viewHolderListener: ViewHolderListener

        init {
            image = itemView.findViewById<ImageView>(R.id.card_image)
            this.requestManager = requestManager
            this.viewHolderListener = viewHolderListener
            itemView.findViewById<View>(R.id.card_view).setOnClickListener(this)
        }

        /**
         * Binds this view holder to the given adapter position.
         *
         *
         * The binding will load the image into the image view, as well as set its transition name for
         * later.
         */
        fun onBind() {
            val adapterPosition = adapterPosition
            setImage(adapterPosition)
            // Set the string value of the image resource as the unique transition name for the view.
            image.transitionName = ImageData.IMAGE_DRAWABLES[adapterPosition].toString()
        }

        fun setImage(adapterPosition: Int) {
            // Load the image with Glide to prevent OOM error when the image drawables are very large.
            requestManager
                .load(ImageData.IMAGE_DRAWABLES[adapterPosition])
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any,
                        target: Target<Drawable?>, isFirstResource: Boolean
                    ): Boolean {
                        viewHolderListener.onLoadCompleted(image, adapterPosition)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        viewHolderListener.onLoadCompleted(image, adapterPosition)
                        return false
                    }
                })
                .into(image)
        }

        override fun onClick(view: View) {
            // Let the listener start the ImagePagerFragment.
            viewHolderListener.onItemClicked(view, adapterPosition)
        }
    }
}