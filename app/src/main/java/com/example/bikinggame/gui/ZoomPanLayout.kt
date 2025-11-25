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

    // The current viewport. This rectangle represents the visible
    // chart domain and range.
    private val mCurrentViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)

    // The current destination rectangle, in pixel coordinates, into which the
// chart data must be drawn.
    private val mContentRect: Rect? = null
    private var scaleFactor = 1f
    var posX = 0f
    var posY = 0f
    var lastTouchX = 0f
    var lastTouchY = 0f

    private var mIsScrolling = false

    private val lines = mutableListOf<Pair<Button, Button>>()

    private val paint = Paint().apply { strokeWidth = 5f }

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withTranslation(posX, posY) {
            scale(scaleFactor, scaleFactor)
            // Draw lines
            for ((a, b) in lines) {
                val startX = a.x + a.width / 2f
                val startY = a.y + a.height / 2f
                val endX = b.x + b.width / 2f
                val endY = b.y + b.height / 2f
                canvas.drawLine(startX, startY, endX, endY, paint)
            }
            super.dispatchDraw(this)
        }
    }

//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        // Clone event
//        val transformed = MotionEvent.obtain(ev)
//
//        val histX = if (transformed.historySize != 0) transformed.getHistoricalX(0) else transformed.x
//        val histY = if (transformed.historySize != 0) transformed.getHistoricalY(0) else transformed.y
//
//        val m2 = Matrix()
//        m2.postScale(1f / scaleFactor, 1f / scaleFactor)
//        m2.postTranslate(-posX + transformed.x - histX, -posY + transformed.y - histY)
//        transformed.transform(m2)
//
//        val handled = super.dispatchTouchEvent(transformed)
//        transformed.recycle()
//        return handled
//    }

    // The "active pointer" is the one moving the object.
    private var mActivePointerId: Int = INVALID_POINTER_ID

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        scaleDetector.onTouchEvent(ev)

        val action = MotionEventCompat.getActionMasked(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                MotionEventCompat.getActionIndex(ev).also { pointerIndex ->
                    // Remember where you start for dragging.
                    lastTouchX = MotionEventCompat.getX(ev, pointerIndex)
                    lastTouchY = MotionEventCompat.getY(ev, pointerIndex)
                }

                // Save the ID of this pointer for dragging.
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position.
                val (x: Float, y: Float) =
                    MotionEventCompat.findPointerIndex(ev, mActivePointerId).let { pointerIndex ->
                        // Calculate the distance moved.
                        MotionEventCompat.getX(ev, pointerIndex) to
                                MotionEventCompat.getY(ev, pointerIndex)
                    }

                posX += x - lastTouchX
                posY += y - lastTouchY

                invalidate()

                // Remember this touch position for the next move event.
                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {

                MotionEventCompat.getActionIndex(ev).also { pointerIndex ->
                    MotionEventCompat.getPointerId(ev, pointerIndex)
                        .takeIf { it == mActivePointerId }
                        ?.run {
                            // This is the active pointer going up. Choose a new
                            // active pointer and adjust it accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            lastTouchX = MotionEventCompat.getX(ev, newPointerIndex)
                            lastTouchY = MotionEventCompat.getY(ev, newPointerIndex)
                            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                        }
                }
            }
        }
        return true
    }


//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (_lastEvent == null) _lastEvent = event
//
//        val action = event.action
//
//        val transformed = MotionEvent.obtain(event)

//        val dx: Float = event.x - lastEvent.x
//        val dy: Float = event.y - lastEvent.y
//        if (action == MotionEvent.ACTION_UP) {
//            transformed.offsetLocation(dx, dy)
//            performClick()
//        }

        // Undo translation
//        transformed.offsetLocation(-posX, -posY)
//
//        // Undo scaling
//        transformed.setLocation(
//            transformed.x / scaleFactor,
//            transformed.y / scaleFactor
//        )

//        scaleDetector.onTouchEvent(event)
//        gestureDetector.onTouchEvent(event)

//        _lastEvent = event
//        return true
//    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.3f, 3.5f)
            invalidate()
            return true
        }
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Scrolling uses math based on the viewport, as opposed to math using
            // pixels.


            mContentRect?.apply {
                // Pixel offset is the offset in screen pixels, while viewport offset is the
                // offset within the current viewport.
                val viewportOffsetX = distanceX * mCurrentViewport.width() / width
                val viewportOffsetY = -distanceY * mCurrentViewport.height() / height


                // Updates the viewport and refreshes the display.
                setViewportBottomLeft(
                    mCurrentViewport.left + viewportOffsetX,
                    mCurrentViewport.bottom + viewportOffsetY
                )
            }

            return true
        }

        /**
         * Sets the current viewport, defined by mCurrentViewport, to the given
         * X and Y positions. The Y value represents the topmost pixel position,
         * and thus the bottom of the mCurrentViewport rectangle.
         */
        private fun setViewportBottomLeft(x: Float, y: Float) {
            /*
             * Constrains within the scroll range. The scroll range is the viewport
             * extremes, such as AXIS_X_MAX, minus the viewport size. For example, if
             * the extremes are 0 and 10 and the viewport size is 2, the scroll range
             * is 0 to 8.
             */

            val curWidth: Float = mCurrentViewport.width()
            val curHeight: Float = mCurrentViewport.height()
            val newX: Float = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth))
            val newY: Float = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX))

            mCurrentViewport.set(newX, newY - curHeight, newX + curWidth, newY)

            // Invalidates the View to update the display.
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun addLine(pair: Pair<Button, Button>) {
        lines.add(pair)
        invalidate()
    }
}
