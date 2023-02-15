package com.dat.swipe.slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.customview.widget.ViewDragHelper
import com.dat.swipe.model.SliderConfig
import com.dat.swipe.model.SliderInterface
import com.dat.swipe.model.SliderPosition
import kotlin.math.abs

class SliderPanel : FrameLayout {
    companion object {
        private const val MIN_FLING_VELOCITY = 400 // dips per second
        const val TAG = "SliderPanel"
        private fun clamp(value: Int, min: Int, max: Int): Int {
            return Math.max(min, Math.min(max, value))
        }

        private fun toAlpha(percentage: Float): Int {
            return (percentage * 255).toInt()
        }
    }
    private var screenWidth = 0
    private var screenHeight = 0
    private lateinit var decorView: View
    private lateinit var dragHelper: ViewDragHelper
    private var listener: OnPanelSlideListener? = null
    private lateinit var scrimPaint: Paint
    private lateinit var scrimRenderer: ScrimRenderer
    private var isLocked = false
    private var isEdgeTouched = false
    private var edgePosition = 0
    var config: SliderConfig = SliderConfig.Builder().build()
    private var decorViewLeftOffset = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, decorView: View, config: SliderConfig?) : super(context) {
        this.decorView = decorView
        this.config = config ?: SliderConfig.Builder().build()
        init()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val views = config.touchDisabledViews
        if (views != null) {
            for (view in views) {
                if (isPointInsideView(ev.rawX, ev.rawY, view)) {
                    Log.i("PPPP", "not intercepting event")
                    return false
                }
            }
        }
        if (isLocked) {
            return false
        }
        if (config.isEdgeOnly) {
            isEdgeTouched = canDragFromEdge(ev)
        }

        // Fix for pull request #13 and issue #12
        val interceptForDrag: Boolean = try {
            dragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            false
        }
        return interceptForDrag && !isLocked
    }

    private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewX = location[0]
        val viewY = location[1]

        //point is inside view bounds
        return x > viewX && x < viewX + view.width && y > viewY && y < viewY + view.height
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLocked) {
            return false
        }
        try {
            dragHelper.processTouchEvent(event)
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            decorViewLeftOffset = decorView.left
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        decorView.offsetLeftAndRight(decorViewLeftOffset)
    }

    override fun onDraw(canvas: Canvas) {
        scrimRenderer.render(canvas, config.position, scrimPaint)
    }

    /**
     * Set the panel slide listener that gets called based on slider changes
     *
     * @param listener callback implementation
     */
    fun setOnPanelSlideListener(listener: OnPanelSlideListener?) {
        this.listener = listener
    }

    /**
     * Get the default [SliderInterface] from which to control the panel with after attachment
     */
    val defaultInterface: SliderInterface = object : SliderInterface {
        override fun lock() {
            this@SliderPanel.lock()
        }

        override fun unlock() {
            this@SliderPanel.unlock()
        }
    }

    /**
     * The drag helper callback interface for the Left position
     */
    private val leftCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val edgeCase = !config.isEdgeOnly || dragHelper.isEdgeTouched(edgePosition, pointerId)
            return child.id == decorView.id && edgeCase
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return clamp(left, 0, screenWidth)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return screenWidth
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            var settleLeft = 0
            val leftThreshold = (width * config.distanceThreshold).toInt()
            val isVerticalSwiping = abs(yvel) > config.velocityThreshold
            if (xvel > 0) {
                if (abs(xvel) > config.velocityThreshold && !isVerticalSwiping) {
                    settleLeft = screenWidth
                } else if (left > leftThreshold) {
                    settleLeft = screenWidth
                }
            } else if (xvel == 0f) {
                if (left > leftThreshold) {
                    settleLeft = screenWidth
                }
            }
            dragHelper.settleCapturedViewAt(settleLeft, releasedChild.top)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - left.toFloat() / screenWidth.toFloat()
            if (listener != null) listener?.onSlideChange(percent)
            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView.left == 0) {
                    // State Open
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    /**
     * The drag helper callbacks for dragging the slidr attachment from the right of the screen
     */
    private val rightCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val edgeCase = !config.isEdgeOnly || dragHelper.isEdgeTouched(edgePosition, pointerId)
            return child.id == decorView.id && edgeCase
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return clamp(left, -screenWidth, 0)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return screenWidth
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            var settleLeft = 0
            val leftThreshold = (width * config.distanceThreshold).toInt()
            val isVerticalSwiping = abs(yvel) > config.velocityThreshold
            if (xvel < 0) {
                if (abs(xvel) > config.velocityThreshold && !isVerticalSwiping) {
                    settleLeft = -screenWidth
                } else if (left < -leftThreshold) {
                    settleLeft = -screenWidth
                }
            } else if (xvel == 0f) {
                if (left < -leftThreshold) {
                    settleLeft = -screenWidth
                }
            }
            dragHelper.settleCapturedViewAt(settleLeft, releasedChild.top)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(left).toFloat() / screenWidth.toFloat()
            if (listener != null) listener!!.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            if (listener != null) listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView!!.left == 0) {
                    // State Open
                    if (listener != null) listener?.onOpened()
                } else {
                    // State Closed
                    if (listener != null) listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    /**
     * The drag helper callbacks for dragging the slidr attachment from the top of the screen
     */
    private val topCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == decorView!!.id && (!config.isEdgeOnly || isEdgeTouched)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return clamp(top, 0, screenHeight)
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return screenHeight
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val top = releasedChild.top
            var settleTop = 0
            val topThreshold = (height * config.distanceThreshold).toInt()
            val isSideSwiping = abs(x = xvel) > config.velocityThreshold
            if (yvel > 0) {
                if (abs(yvel) > config.velocityThreshold && !isSideSwiping) {
                    settleTop = screenHeight
                } else if (top > topThreshold) {
                    settleTop = screenHeight
                }
            } else if (yvel == 0f) {
                if (top > topThreshold) {
                    settleTop = screenHeight
                }
            }
            dragHelper.settleCapturedViewAt(releasedChild.left, settleTop)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(top).toFloat() / screenHeight.toFloat()
            if (listener != null) listener!!.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView!!.top == 0) {
                    // State Open
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    /**
     * The drag helper callbacks for dragging the slidr attachment from the bottom of hte screen
     */
    private val bottomCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == decorView!!.id && (!config.isEdgeOnly || isEdgeTouched)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return clamp(top, -screenHeight, 0)
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return screenHeight
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val top = releasedChild.top
            var settleTop = 0
            val topThreshold = (height * config.distanceThreshold).toInt()
            val isSideSwiping = abs(xvel) > config.velocityThreshold
            if (yvel < 0) {
                if (abs(yvel) > config.velocityThreshold && !isSideSwiping) {
                    settleTop = -screenHeight
                } else if (top < -topThreshold) {
                    settleTop = -screenHeight
                }
            } else if (yvel == 0f) {
                if (top < -topThreshold) {
                    settleTop = -screenHeight
                }
            }
            dragHelper.settleCapturedViewAt(releasedChild.left, settleTop)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(top).toFloat() / screenHeight.toFloat()
            listener?.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView!!.top == 0) {
                    // State Open
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    /**
     * The drag helper callbacks for dragging the slidr attachment in both vertical directions
     */
    private val verticalCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child.id == decorView?.id && (!config.isEdgeOnly || isEdgeTouched)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return clamp(top, -screenHeight, screenHeight)
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return screenHeight
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val top = releasedChild.top
            var settleTop = 0
            val topThreshold = (height * config.distanceThreshold).toInt()
            val isSideSwiping = abs(xvel) > config.velocityThreshold
            if (yvel > 0) {

                // Being slinged down
                if (abs(yvel) > config.velocityThreshold && !isSideSwiping) {
                    settleTop = screenHeight
                } else if (top > topThreshold) {
                    settleTop = screenHeight
                }
            } else if (yvel < 0) {
                // Being slinged up
                if (abs(yvel) > config.velocityThreshold && !isSideSwiping) {
                    settleTop = -screenHeight
                } else if (top < -topThreshold) {
                    settleTop = -screenHeight
                }
            } else {
                if (top > topThreshold) {
                    settleTop = screenHeight
                } else if (top < -topThreshold) {
                    settleTop = -screenHeight
                }
            }
            dragHelper.settleCapturedViewAt(releasedChild.left, settleTop)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(top).toFloat() / screenHeight.toFloat()
            if (listener != null) listener!!.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView!!.top == 0) {
                    // State Open
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    /**
     * The drag helper callbacks for dragging the slidr attachment in both horizontal directions
     */
    private val horizontalCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val edgeCase = !config.isEdgeOnly || dragHelper.isEdgeTouched(edgePosition, pointerId)
            return child.id == decorView?.id && edgeCase
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return clamp(left, -screenWidth, screenWidth)
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return screenWidth
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            var settleLeft = 0
            val leftThreshold = (width * config.distanceThreshold).toInt()
            val isVerticalSwiping = abs(yvel) > config.velocityThreshold
            if (xvel > 0) {
                if (abs(xvel) > config.velocityThreshold && !isVerticalSwiping) {
                    settleLeft = screenWidth
                } else if (left > leftThreshold) {
                    settleLeft = screenWidth
                }
            } else if (xvel < 0) {
                if (abs(xvel) > config.velocityThreshold && !isVerticalSwiping) {
                    settleLeft = -screenWidth
                } else if (left < -leftThreshold) {
                    settleLeft = -screenWidth
                }
            } else {
                if (left > leftThreshold) {
                    settleLeft = screenWidth
                } else if (left < -leftThreshold) {
                    settleLeft = -screenWidth
                }
            }
            dragHelper.settleCapturedViewAt(settleLeft, releasedChild.top)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(left).toFloat() / screenWidth.toFloat()
            listener?.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView!!.left == 0) {
                    // State Open 
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }
    private var isEnableScrollRight = false
    private val leftFacebookCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val edgeCase = !config.isEdgeOnly || dragHelper.isEdgeTouched(edgePosition, pointerId)
            return child.id == decorView.id && edgeCase
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (isEnableScrollRight) clamp(left, -(screenWidth / 2), screenWidth) else clamp(
                left,
                0,
                screenWidth
            )
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return screenWidth
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val left = releasedChild.left
            var settleLeft = 0
            val leftThreshold = (width * config.distanceThreshold).toInt()
            val isVerticalSwiping = abs(yvel) > config.velocityThreshold
            if (xvel > 0) {
                if (abs(xvel) > config.velocityThreshold && !isVerticalSwiping) {
                    settleLeft = screenWidth
                } else if (left > leftThreshold) {
                    settleLeft = screenWidth
                }
            } else if (xvel == 0f) {
                if (left > leftThreshold) {
                    settleLeft = screenWidth
                }
            }
            isEnableScrollRight = false
            dragHelper.settleCapturedViewAt(settleLeft, releasedChild.top)
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val percent = 1f - abs(left).toFloat() / screenWidth.toFloat()
            if (left > 0) isEnableScrollRight = true
            listener?.onSlideChange(percent)

            // Update the dimmer alpha
            applyScrim(percent)
        }

        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)
            listener?.onStateChanged(state)
            when (state) {
                ViewDragHelper.STATE_IDLE -> if (decorView.left == 0) {
                    // State Open
                    listener?.onOpened()
                } else {
                    // State Closed
                    listener?.onClosed()
                }
                ViewDragHelper.STATE_DRAGGING -> {}
                ViewDragHelper.STATE_SETTLING -> {}
            }
        }
    }

    private fun init() {
        setWillNotDraw(false)
        screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val minVel = MIN_FLING_VELOCITY * density
        val callback: ViewDragHelper.Callback
        when (config.position) {
            SliderPosition.LEFT -> {
                callback = leftCallback
                edgePosition = ViewDragHelper.EDGE_LEFT
            }
            SliderPosition.RIGHT -> {
                callback = rightCallback
                edgePosition = ViewDragHelper.EDGE_RIGHT
            }
            SliderPosition.TOP -> {
                callback = topCallback
                edgePosition = ViewDragHelper.EDGE_TOP
            }
            SliderPosition.BOTTOM -> {
                callback = bottomCallback
                edgePosition = ViewDragHelper.EDGE_BOTTOM
            }
            SliderPosition.VERTICAL -> {
                callback = verticalCallback
                edgePosition = ViewDragHelper.EDGE_TOP or ViewDragHelper.EDGE_BOTTOM
            }
            SliderPosition.HORIZONTAL -> {
                callback = horizontalCallback
                edgePosition = ViewDragHelper.EDGE_LEFT or ViewDragHelper.EDGE_RIGHT
            }
            SliderPosition.LEFT_FACEBOOK -> {
                callback = leftFacebookCallback
                edgePosition = ViewDragHelper.EDGE_LEFT or ViewDragHelper.EDGE_RIGHT
            }
            else -> {
                callback = leftCallback
                edgePosition = ViewDragHelper.EDGE_LEFT
            }
        }
        dragHelper = ViewDragHelper.create(this, config.sensitivity, callback)
        dragHelper.minVelocity = minVel
        dragHelper.setEdgeTrackingEnabled(edgePosition)
        ViewGroupCompat.setMotionEventSplittingEnabled(this, false)

        // Setup the dimmer view
        scrimPaint = Paint()
        scrimPaint.color = config.scrimColor
        scrimPaint.alpha = toAlpha(config.scrimStartAlpha)
        scrimRenderer = ScrimRenderer(this, decorView!!)

        /*
         * This is so we can get the height of the view and
         * ignore the system navigation that would be included if we
         * retrieved this value from the DisplayMetrics
         */post { screenHeight = height }
    }

    private fun lock() {
        dragHelper.abort()
        isLocked = true
    }

    private fun unlock() {
        dragHelper.abort()
        isLocked = false
    }

    private fun canDragFromEdge(ev: MotionEvent): Boolean {
        val x = ev.x
        val y = ev.y
        return when (config.position) {
            SliderPosition.LEFT -> x < config.getEdgeSize(width.toFloat())
            SliderPosition.RIGHT -> x > width - config.getEdgeSize(width.toFloat())
            SliderPosition.BOTTOM -> y > height - config.getEdgeSize(height.toFloat())
            SliderPosition.TOP -> y < config.getEdgeSize(height.toFloat())
            SliderPosition.HORIZONTAL -> x < config.getEdgeSize(width.toFloat()) || x > width - config.getEdgeSize(
                width.toFloat()
            )
            SliderPosition.VERTICAL -> y < config.getEdgeSize(height.toFloat()) || y > height - config.getEdgeSize(
                height.toFloat()
            )
            SliderPosition.LEFT_FACEBOOK -> x < config.getEdgeSize(width.toFloat())
        }
    }

    private fun applyScrim(percent: Float) {
        var realPercent = (percent - config.scrimThreshHold  ) / (1 - config.scrimThreshHold)
        if(realPercent < 0)
            realPercent = 0.0F
        Log.e(TAG, "applyScrim: $realPercent", )
        val alpha = realPercent * (config.scrimStartAlpha - config.scrimEndAlpha) + config.scrimEndAlpha
        scrimPaint.alpha = toAlpha(alpha)
        invalidate(scrimRenderer.getDirtyRect(config.position))
    }

    /**
     * The panel sliding interface that gets called
     * whenever the panel is closed or opened
     */
    interface OnPanelSlideListener {
        fun onStateChanged(state: Int)
        fun onClosed()
        fun onOpened()
        fun onSlideChange(percent: Float)
    }


}