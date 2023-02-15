package com.dat.swipe.model

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

class SliderConfig private constructor() {
    /**
     * Get the primary color that the slider will interpolate. That is this color is the color
     * of the status bar of the Activity you are returning to
     *
     * @return the primary status bar color
     */
    var primaryColor = -1
        private set

    /**
     * Get the secondary color that the slider will interpolatel That is the color of the Activity
     * that you are making slidable
     *
     * @return the secondary status bar color
     */
    var secondaryColor = -1
        private set

    /**
     * Get the touch 'width' to be used in the gesture detection. This value should incorporate with
     * the device's touch slop
     *
     * @return the touch area size
     */
    var touchSize = -1f

    /**
     * Get the touch sensitivity set in the [ViewDragHelper] when
     * creating it.
     *
     * @return the touch sensitivity
     */
    var sensitivity = 1f

    /***********************************************************************************************
     *
     * Getters
     *
     */
    /**
     * Get the color of the background scrim
     *
     * @return the scrim color integer
     */
    var scrimColor = Color.BLACK

    /**
     * Get teh start alpha value for when the activity is not swiped at all
     *
     * @return the start alpha value (0.0 to 1.0)
     */
    var scrimStartAlpha = 0.8f

    /**
     * Get the end alpha value for when the user almost swipes the activity off the screen
     *
     * @return the end alpha value (0.0 to 1.0)
     */
    var scrimEndAlpha = 0f

    /**
     * Get the velocity threshold at which the slide action is completed regardless of offset
     * distance of the drag
     *
     * @return the velocity threshold
     */
    var velocityThreshold = 5f

    /**
     * Get at what % of the screen is the minimum viable distance the activity has to be dragged
     * in-order to be slinged off the screen
     *
     * @return the distant threshold as a percentage of the screen size (width or height)
     */
    var distanceThreshold = 0.4f

    /**
     * Has the user configured slidr to only catch at the edge of the screen ?
     *
     * @return true if is edge capture only
     */
    var isEdgeOnly = false
        private set

    /**
     * Get the size of the edge field that is catchable
     *
     * @return the size of the edge that is grabable
     * @see .isEdgeOnly
     */
    var edgeSize = 0.18f

    fun getEdgeSize(size: Float): Float {
        return edgeSize * size
    }

    var touchDisabledViews: List<View>? = null

    /**
     * Get the position of the slidable mechanism for this configuration. This is the position on
     * the screen that the user can swipe the activity away from
     *
     * @return the slider position
     */
    var position = SliderPosition.LEFT
        private set

    /**
     * Get the slidr listener set by the user to respond to certain events in the sliding
     * mechanism.
     *
     * @return the slidr listener
     */
    var listener: SliderListener? = null
        private set

    /**
     *
     * */
    var scrimThreshHold: Float = 0f

    /**
     * The Builder for this configuration class. This is the only way to create a
     * configuration
     */

    class Builder {
        private val config: SliderConfig = SliderConfig()

        fun primaryColor(@ColorInt color: Int): Builder {
            config.primaryColor = color
            return this
        }

        fun secondaryColor(@ColorInt color: Int): Builder {
            config.secondaryColor = color
            return this
        }

        fun position(position: SliderPosition): Builder {
            config.position = position
            return this
        }

        fun touchSize(size: Float): Builder {
            config.touchSize = size
            return this
        }

        fun sensitivity(sensitivity: Float): Builder {
            config.sensitivity = sensitivity
            return this
        }

        fun scrimColor(@ColorInt color: Int): Builder {
            config.scrimColor = color
            return this
        }

        fun scrimStartAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Builder {
            config.scrimStartAlpha = alpha
            return this
        }

        fun scrimEndAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Builder {
            config.scrimEndAlpha = alpha
            return this
        }

        fun endScrimThreshHold(@FloatRange(from = 0.0, to = 1.0) threshold: Float): Builder {
            config.scrimThreshHold = threshold
            return this
        }

        fun velocityThreshold(threshold: Float): Builder {
            config.velocityThreshold = threshold
            return this
        }

        fun distanceThreshold(@FloatRange(from = 0.1, to = 0.9) threshold: Float): Builder {
            config.distanceThreshold = threshold
            return this
        }

        fun edge(flag: Boolean): Builder {
            config.isEdgeOnly = flag
            return this
        }

        fun edgeSize(@FloatRange(from = 0.0, to = 1.0) edgeSize: Float): Builder {
            config.edgeSize = edgeSize
            return this
        }

        fun listener(listener: SliderListener?): Builder {
            config.listener = listener
            return this
        }

        fun touchDisabledViews(views: List<View>?): Builder {
            config.touchDisabledViews = views
            return this
        }

        fun build(): SliderConfig {
            return config
        }
    }
}