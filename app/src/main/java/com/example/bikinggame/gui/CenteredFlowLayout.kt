package com.example.bikinggame.gui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class CenteredFlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    var maxRowWidthPx = 0   // Set this externally

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)

        // Let the max width default to the parent's width if 0
        val maxWidth = if (maxRowWidthPx == 0) parentWidth else maxRowWidthPx

        // Measure one child to know required size
        var childWidth = 0
        var childHeight = 0
        if (childCount > 0) {
            measureChildren(widthMeasureSpec, heightMeasureSpec)
            val c = getChildAt(0)
            childWidth = c.measuredWidth
            childHeight = c.measuredHeight
        }

        val rows = mutableListOf<Int>() // number of items per row
        var usedWidth = 0
        var itemsInRow = 0

        // First pass: decide row lengths
        for (i in 0 until childCount) {
            if (usedWidth + childWidth > maxWidth && itemsInRow > 0) {
                rows.add(itemsInRow)
                usedWidth = 0
                itemsInRow = 0
            }
            usedWidth += childWidth
            itemsInRow++
        }
        if (itemsInRow > 0) rows.add(itemsInRow)

        val totalHeight = rows.size * childHeight
        val resolvedWidth = maxWidth

        setMeasuredDimension(
            resolveSize(resolvedWidth, widthMeasureSpec),
            resolveSize(totalHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentWidth = r - l
        val maxWidth = if (maxRowWidthPx == 0) parentWidth else maxRowWidthPx

        if (childCount == 0) return

        val c = getChildAt(0)
        val cw = c.measuredWidth
        val ch = c.measuredHeight

        // Determine rows again
        var rows = mutableListOf<Int>()
        var usedWidth = 0
        var itemsInRow = 0
        for (i in 0 until childCount) {
            if (usedWidth + cw > maxWidth && itemsInRow > 0) {
                rows.add(itemsInRow)
                itemsInRow = 0
                usedWidth = 0
            }
            usedWidth += cw
            itemsInRow++
        }
        if (itemsInRow > 0) rows.add(itemsInRow)

        // total content height
        val totalH = rows.size * ch
        val topOffset = (height - totalH) / 2   // center vertically

        var childIndex = 0
        var y = topOffset

        for (rowCount in rows) {
            val rowWidth = rowCount * cw
            val xStart = (maxWidth - rowWidth) / 2

            var x = xStart
            for (i in 0 until rowCount) {
                val child = getChildAt(childIndex++)
                child.layout(x, y, x + cw, y + ch)
                x += cw
            }
            y += ch
        }
    }
}