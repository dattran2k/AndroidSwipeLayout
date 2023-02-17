package com.dat.swipe_back.fragment.slider

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.dat.swipe_back.fragment.model.SliderPosition

internal class ScrimRenderer(private val rootView: View, private val decorView: View) {
    private val dirtyRect: Rect = Rect()

    fun render(canvas: Canvas, position: SliderPosition, paint: Paint) {
        when (position) {
            SliderPosition.LEFT -> renderLeft(canvas, paint)
            SliderPosition.LEFT_FACEBOOK -> renderHorizontal(canvas, paint)
            SliderPosition.RIGHT -> renderRight(canvas, paint)
            SliderPosition.TOP -> renderTop(canvas, paint)
            SliderPosition.BOTTOM -> renderBottom(canvas, paint)
            SliderPosition.VERTICAL -> renderVertical(canvas, paint)
            SliderPosition.HORIZONTAL -> renderHorizontal(canvas, paint)
        }
    }

    fun getDirtyRect(position: SliderPosition): Rect {
        when (position) {
            SliderPosition.LEFT -> dirtyRect[0, 0, decorView.left] = rootView.measuredHeight
            SliderPosition.RIGHT -> dirtyRect[decorView.right, 0, rootView.measuredWidth] = rootView.measuredHeight
            SliderPosition.TOP -> dirtyRect[0, 0, rootView.measuredWidth] = decorView.top
            SliderPosition.BOTTOM -> dirtyRect[0, decorView.bottom, rootView.measuredWidth] = rootView.measuredHeight
            SliderPosition.VERTICAL -> if (decorView.top > 0) {
                dirtyRect[0, 0, rootView.measuredWidth] = decorView.top
            } else {
                dirtyRect[0, decorView.bottom, rootView.measuredWidth] = rootView.measuredHeight
            }
            SliderPosition.HORIZONTAL, SliderPosition.LEFT_FACEBOOK -> if (decorView.left > 0) {
                dirtyRect[0, 0, decorView.left] = rootView.measuredHeight
            } else {
                dirtyRect[decorView.right, 0, rootView.measuredWidth] = rootView.measuredHeight
            }
        }
        return dirtyRect
    }

    private fun renderLeft(canvas: Canvas, paint: Paint) {
        canvas.drawRect(0f, 0f, decorView.left.toFloat(), rootView.measuredHeight.toFloat(), paint)
    }

    private fun renderRight(canvas: Canvas, paint: Paint) {
        canvas.drawRect(
            decorView.right.toFloat(),
            0f,
            rootView.measuredWidth.toFloat(),
            rootView.measuredHeight.toFloat(),
            paint
        )
    }

    private fun renderTop(canvas: Canvas, paint: Paint) {
        canvas.drawRect(0f, 0f, rootView.measuredWidth.toFloat(), decorView.top.toFloat(), paint)
    }

    private fun renderBottom(canvas: Canvas, paint: Paint) {
        canvas.drawRect(
            0f,
            decorView.bottom.toFloat(),
            rootView.measuredWidth.toFloat(),
            rootView.measuredHeight.toFloat(),
            paint
        )
    }

    private fun renderVertical(canvas: Canvas, paint: Paint) {
        if (decorView.top > 0) {
            renderTop(canvas, paint)
        } else {
            renderBottom(canvas, paint)
        }
    }

    private fun renderHorizontal(canvas: Canvas, paint: Paint) {
        if (decorView.left > 0) {
            renderLeft(canvas, paint)
        } else {
            renderRight(canvas, paint)
        }
    }
}