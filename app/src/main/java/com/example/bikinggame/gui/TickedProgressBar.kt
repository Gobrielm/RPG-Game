package com.example.bikinggame.gui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.bikinggame.R
import kotlin.math.min

class TickedProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var max = 100
        set(value) { field = value; invalidate() }

    var progress = 23
        set(value) { field = value.coerceIn(0, max); invalidate() }

    var secondaryProgress = 0
        set(value) { field = value.coerceIn(0, max); invalidate() }

    var tickInterval = 5
        set(value) { field = value; invalidate() }

    private var progressSlant = dp(6f)
    private val cornerRadius = dp(5f)


    private var progressColor: Int = Color.parseColor("#FFD300")
    private var secondaryProgressColor: Int = Color.parseColor("#80FFD300")
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val tickFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 255, 255, 255)
        style = Paint.Style.FILL
    }

    private val tickStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
        strokeJoin = Paint.Join.MITER
    }


    private val rect = RectF()

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.TickedProgressBar)

        max = a.getInt(R.styleable.TickedProgressBar_max, max)
        progress = a.getInt(R.styleable.TickedProgressBar_progress, progress)
        secondaryProgress = a.getInt(
            R.styleable.TickedProgressBar_secondaryProgress,
            secondaryProgress
        )

        tickInterval = a.getInt(R.styleable.TickedProgressBar_tickInterval, tickInterval)

        progressSlant = a.getDimension(
            R.styleable.TickedProgressBar_progressSlant,
            progressSlant
        )

        progressColor = a.getColor(
            R.styleable.TickedProgressBar_progressColor,
            Color.parseColor("#FFD300") // default
        )

        secondaryProgressColor = a.getColor(
            R.styleable.TickedProgressBar_secondaryProgressColor,
            Color.parseColor("#00FF00")
        )

        tickStrokePaint.color = a.getColor(
            R.styleable.TickedProgressBar_tickColor,
            tickStrokePaint.color
        )

        a.recycle()

    }

    private fun lighten(color: Int, factor: Float): Int {
        return Color.argb(
            Color.alpha(color),
            min(255, (Color.red(color) * (1 + factor)).toInt()),
            min(255, (Color.green(color) * (1 + factor)).toInt()),
            min(255, (Color.blue(color) * (1 + factor)).toInt())
        )
    }

    private fun darken(color: Int, factor: Float): Int {
        return Color.argb(
            Color.alpha(color),
            (Color.red(color) * (1 - factor)).toInt(),
            (Color.green(color) * (1 - factor)).toInt(),
            (Color.blue(color) * (1 - factor)).toInt()
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        /* Background gradient (matches XML) */
        backgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#ff9d9e9d"),
                Color.parseColor("#ff5a5d5a"),
                Color.parseColor("#ff747674")
            ),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )

        /* Secondary progress gradient */
        secondaryPaint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                darken(secondaryProgressColor, 0.1f),
                secondaryProgressColor,
                lighten(secondaryProgressColor, 0.1f)
            ),
            floatArrayOf(0f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )

        /* Main progress gradient */
        progressPaint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            intArrayOf(
                lighten(progressColor, 0.15f),
                progressColor,
                darken(progressColor, 0.15f)
            ),
            floatArrayOf(0f, 0.35f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val barHeight = height * 0.6f
        val top = (height - barHeight) / 2f
        val bottom = top + barHeight

        /* ---------- Background ---------- */
        rect.set(0f, top, width.toFloat(), bottom)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

        /* ---------- Secondary Progress ---------- */
        drawAngledSecondaryProgress(canvas, top, bottom)

        /* ---------- Main Progress ---------- */
        drawAngledProgress(canvas, top, bottom)

        drawTicks(canvas, top, bottom)
    }

    private fun drawAngledProgress(
        canvas: Canvas,
        top: Float,
        bottom: Float
    ) {
        val widthPx = width * (progress / max.toFloat())
        if (widthPx <= 0f) return

        if (progress == max) {
            rect.set(0f, top, width.toFloat(), bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, progressPaint)
            return
        }

        val progressPath = getPathForProgress(top, bottom, widthPx)

        canvas.drawPath(progressPath, progressPaint)
    }

    private fun drawAngledSecondaryProgress(
        canvas: Canvas,
        top: Float,
        bottom: Float
    ) {
        val widthPx = width * (secondaryProgress / max.toFloat())
        if (widthPx <= 0f) return

        if (secondaryProgress == max) {
            rect.set(0f, top, width.toFloat(), bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, secondaryPaint)
            return
        }

        val secondaryProgressPath = getPathForProgress(top, bottom, widthPx)

        canvas.drawPath(secondaryProgressPath, secondaryPaint)
    }

    private fun getPathForProgress(top: Float, bottom: Float, widthPx: Float): Path {
        val slant = min(progressSlant, widthPx)

        val path = Path()

        path.moveTo(cornerRadius, top)
        path.lineTo(widthPx, top)

        path.lineTo(widthPx - slant, bottom)
        path.lineTo(cornerRadius, bottom)

        path.quadTo(0f, bottom, 0f, bottom - cornerRadius)
        path.lineTo(0f, top + cornerRadius)
        path.quadTo(0f, top, cornerRadius, top)

        path.close()

        return path
    }


    private fun drawTicks(canvas: Canvas, top: Float, bottom: Float) {
        if (tickInterval <= 0) return

        val tickCount = max / tickInterval
        val slant = progressSlant
        val thickness = dp(3f)

        for (i in 1 until tickCount) {
            val x = width * (i * tickInterval / max.toFloat())

            val path = Path()

            // Top edge
            path.moveTo(x, top)
            path.lineTo(x + thickness, top)

            // Bottom edge (slanted)
            path.lineTo(x + thickness - slant, bottom)
            path.lineTo(x - slant, bottom)

            path.close()

            // Fill
            canvas.drawPath(path, tickFillPaint)

            // Stroke
            canvas.drawPath(path, tickStrokePaint)
        }
    }

    private fun dp(value: Float): Float =
        value * context.resources.displayMetrics.density
}
