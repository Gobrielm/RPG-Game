package com.mainApp.rpg.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot

class PointEditorView(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {

    data class Point(var x: Float, var y: Float)

    private val points = mutableListOf<Point>()
    private var activePoint: Point? = null

    private val pointPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 1f
    }

    private val pointRadius = 20f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawGrid(canvas)

        for (p in points) {
            canvas.drawCircle(p.x, p.y, pointRadius, pointPaint)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val step = 100f
        for (x in 0..width step step.toInt()) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
        }
        for (y in 0..height step step.toInt()) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activePoint = findPoint(x, y)
                if (activePoint == null) {
                    points.add(Point(x, y))
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                activePoint?.let {
                    it.x = x
                    it.y = y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                activePoint = null
            }
        }
        return true
    }

    private fun findPoint(x: Float, y: Float): Point? {
        return points.find {
            hypot(it.x - x, it.y - y) <= pointRadius * 1.5f
        }
    }

    fun exportPointsAsJson(): String {
        val jsonArray = JSONArray()
        for (p in points) {
            val obj = JSONObject()
            obj.put("x", p.x)
            obj.put("y", p.y)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }
}
