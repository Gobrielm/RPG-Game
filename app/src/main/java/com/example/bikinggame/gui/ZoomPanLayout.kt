package com.example.bikinggame.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.graphics.withTranslation
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import kotlin.math.abs

class ZoomPanLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        isClickable = true
        isFocusable = true
    }

    private var scaleFactor = 1f
    var posX = 0f
    var posY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false

    private var mIsScrolling = false

    private val lines = mutableListOf<Pair<Button, Button>>()

    private val paint = Paint().apply { strokeWidth = 5f }

//    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // If the user is moving → we want to pan
        if (ev.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withTranslation(posX, posY) {
            super.dispatchDraw(this)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Create a copy so we do not mutate the original MotionEvent
        val transformed = MotionEvent.obtain(ev)

        // Shift coordinates *opposite* of the canvas translation
        transformed.offsetLocation(-posX, -posY)

        // Dispatch the modified event to children
        val handled = super.dispatchTouchEvent(transformed)

        transformed.recycle()
        return handled
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY

                posX += dx
                posY += dy

                lastX = x
                lastY = y

                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return true
    }


//    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            scaleFactor *= detector.scaleFactor
//            scaleFactor = scaleFactor.coerceIn(0.3f, 3.5f)
//            invalidate()
//            return true
//        }
//    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float
        ): Boolean {
            Log.d("AAA", "AAA")
            posX -= dx
            posY -= dy

            invalidate()

            return true
        }
    }

    fun addLine(pair: Pair<Button, Button>) {
        lines.add(pair)
        invalidate()
    }
}
