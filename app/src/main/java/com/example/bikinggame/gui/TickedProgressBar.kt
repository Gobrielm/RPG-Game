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
    private val progressPath = Path()
    private val cornerRadius = dp(5f)


    private var progressColor: Int = Color.parseColor("#FFD300")
    private var secondaryProgressColor: Int = Color.parseColor("#80FFD300")
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 0, 0, 0)
        strokeWidth = dp(2f)
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
            Color.parseColor("#80FFD300")
        )

        tickPaint.color = a.getColor(
            R.styleable.TickedProgressBar_tickColor,
            tickPaint.color
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
        if (secondaryProgress > 0) {
            val secWidth = width * (secondaryProgress / max.toFloat())
            rect.set(0f, top, secWidth, bottom)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, secondaryPaint)
        }

        /* ---------- Main Progress ---------- */
        val progWidth = width * (progress / max.toFloat())
        drawAngledProgress(canvas, top, bottom, progWidth)

        drawTicks(canvas, top, bottom)
    }

    private fun drawAngledProgress(
        canvas: Canvas,
        top: Float,
        bottom: Float,
        widthPx: Float
    ) {
        if (widthPx <= 0f) return

        val slant = min(progressSlant, widthPx)

        progressPath.reset()

        progressPath.moveTo(cornerRadius, top)
        progressPath.lineTo(widthPx, top)

        progressPath.lineTo(widthPx - slant, bottom)
        progressPath.lineTo(cornerRadius, bottom)

        progressPath.quadTo(0f, bottom, 0f, bottom - cornerRadius)
        progressPath.lineTo(0f, top + cornerRadius)
        progressPath.quadTo(0f, top, cornerRadius, top)

        progressPath.close()

        canvas.drawPath(progressPath, progressPaint)
    }


    private fun drawTicks(canvas: Canvas, top: Float, bottom: Float) {
        if (tickInterval <= 0) return

        val tickCount = max / tickInterval
        val slant = progressSlant

        for (i in 1 until tickCount) {
            val x = width * (i * tickInterval / max.toFloat())

            canvas.drawLine(
                x - slant,
                bottom,
                x,
                top,
                tickPaint
            )
        }
    }

    private fun dp(value: Float): Float =
        value * context.resources.displayMetrics.density
}
