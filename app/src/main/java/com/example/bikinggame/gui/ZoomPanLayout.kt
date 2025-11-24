package com.example.bikinggame.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.graphics.withTranslation

class ZoomPanLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var scaleFactor = 1f
    var posX = 0f
    var posY = 0f

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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.3f, 3.5f)
            invalidate()
            return true
        }
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
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
