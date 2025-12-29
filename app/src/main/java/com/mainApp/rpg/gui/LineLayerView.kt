package com.mainApp.rpg.gui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.Button

class LineLayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val lines = mutableListOf<Pair<Button, Button>>()

    private val paint = Paint().apply { strokeWidth = 5f }

    override fun onDraw(canvas: Canvas) {
        for ((a, b) in lines) {
            val startX = a.x + a.width / 2f
            val startY = a.y + a.height / 2f
            val endX = b.x + b.width / 2f
            val endY = b.y + b.height / 2f

            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }

    fun addLine(p: Pair<Button, Button>) {
        lines.add(p)
        invalidate()
    }
}
