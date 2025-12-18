package com.example.bikinggame.gui

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class BottomCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyBottomCrop()
    }

    private fun applyBottomCrop() {
        val d = drawable ?: return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()

        val scale = maxOf(
            viewWidth / dw,
            viewHeight / dh
        )

        val dx = (viewWidth - dw * scale) / 2f
        val dy = viewHeight - dh * scale

        imageMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }

        scaleType = ScaleType.MATRIX
    }
}