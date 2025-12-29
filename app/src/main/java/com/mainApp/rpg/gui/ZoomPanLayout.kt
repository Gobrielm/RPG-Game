package com.mainApp.rpg.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.graphics.withTranslation

class ZoomPanLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        isClickable = true
        isFocusable = true
    }

    private var scaleFactor = 0.7f

    val offsetX = 160f
    val offsetY = 120f
    var posX = 300f / scaleFactor
    var posY = 500f / scaleFactor
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private val lines = mutableListOf<Pair<Button, Button>>()

    private val paint = Paint().apply { strokeWidth = 10f }

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // If the user is moving → we want to pan
        if (ev.action == MotionEvent.ACTION_MOVE) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withTranslation(posX, posY) {
            scale(scaleFactor, scaleFactor)
            canvas.withTranslation(offsetX, offsetY) {
                for (linePair in lines) {
                    drawLine(linePair.first.x, linePair.first.y,
                        linePair.second.x, linePair.second.y,
                        paint
                    )
                }
            }
            super.dispatchDraw(canvas)
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Create a copy so we do not mutate the original MotionEvent
        val transformed = MotionEvent.obtain(ev)

        // Shift coordinates *opposite* of the canvas translation
        transformed.offsetLocation(-posX, -posY)

        val matrix = Matrix().apply {
            setScale(1f / scaleFactor, 1f / scaleFactor)
        }
        transformed.transform(matrix)

        // Dispatch the modified event to children
        val handled = super.dispatchTouchEvent(transformed)

        transformed.recycle()
        return handled
    }

    private fun MotionEvent.toRaw(): MotionEvent {
        val newEvent = MotionEvent.obtain(this)

        val matrix = Matrix().apply {
            setScale(scaleFactor, scaleFactor)
        }
        newEvent.transform(matrix)

        newEvent.offsetLocation(posX, posY)

        return newEvent
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY



        if (event.pointerCount != 1) {
            val raw = event.toRaw()
            scaleDetector.onTouchEvent(raw)
            isDragging = false
            raw.recycle()
            return true
        }

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                isDragging = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) {
                    isDragging = true
                    lastX = x
                    lastY = y
                }
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


    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val before = scaleFactor
            scaleFactor *= detector.scaleFactor
            if (before == scaleFactor) {
                return false
            }
            scaleFactor = scaleFactor.coerceIn(0.3f, 3.5f)
            invalidate()
            return true
        }
    }

    fun addLine(pair: Pair<Button, Button>) {
        lines.add(pair)
        invalidate()
    }
}
