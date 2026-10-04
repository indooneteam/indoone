package com.indoone.assistant

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.RadialGradient
import android.graphics.LinearGradient
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.hypot

class AssistantOverlayView(
    context: Context,
    private val onClose: () -> Unit,
    private val onOpenApp: () -> Unit,
    private val onDrag: (dx: Int, dy: Int) -> Unit,
) : FrameLayout(context) {

    companion object {
        private const val ORB_SIZE_DP = 92
        private const val CLOSE_SIZE_DP = 30
        private const val HIT_SLOP_DP = 10
    }

    private val orb = AssistantOrbView(context)
    private val close = TextView(context)
    private var downRawX = 0f
    private var downRawY = 0f
    private var totalDrag = 0f

    init {
        clipChildren = false
        clipToPadding = false
        isClickable = true
        contentDescription = "Indoone Voice Assistant"

        orb.apply {
            contentDescription = "Open Indoone"
            isClickable = true
            isFocusable = true
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downRawX = event.rawX
                        downRawY = event.rawY
                        totalDrag = 0f
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downRawX).toInt()
                        val dy = (event.rawY - downRawY).toInt()
                        totalDrag += hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        if (dx != 0 || dy != 0) {
                            onDrag(dx, dy)
                            downRawX = event.rawX
                            downRawY = event.rawY
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (totalDrag < HIT_SLOP_DP.dp()) {
                            performClick()
                            onOpenApp()
                        }
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> true
                    else -> false
                }
            }
        }

        addView(
            orb,
            LayoutParams(
                ORB_SIZE_DP.dp(),
                ORB_SIZE_DP.dp(),
                Gravity.CENTER,
            ),
        )

        close.apply {
            text = "×"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(0xFFF7F4FF.toInt())
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xCC161221.toInt())
                setStroke(1.dp(), 0x80D4C5FF.toInt())
            }
            elevation = 8.dp().toFloat()
            isClickable = true
            isFocusable = true
            contentDescription = "Close Indoone Assistant"
            setOnClickListener { onClose() }
        }

        addView(
            close,
            LayoutParams(
                CLOSE_SIZE_DP.dp(),
                CLOSE_SIZE_DP.dp(),
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = 1.dp()
                rightMargin = 1.dp()
            },
        )

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    fun setStatus(text: String) {
        orb.setState(text)
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}

private class AssistantOrbView(
    context: Context,
) : View(context) {

    private enum class State {
        LISTENING,
        THINKING,
        SPEAKING,
        IDLE,
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val smilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.4f.dp(context)
    }
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3.0f.dp(context)
    }

    private var state = State.LISTENING
    private var phase = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setWillNotDraw(false)
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    fun setState(text: String) {
        state = when {
            text.contains("think", ignoreCase = true) -> State.THINKING
            text.contains("speak", ignoreCase = true) -> State.SPEAKING
            text.contains("listen", ignoreCase = true) -> State.LISTENING
            else -> State.IDLE
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = (minOf(width, height) * 0.34f).coerceAtLeast(1f)
        val pulse = when (state) {
            State.LISTENING -> 0.94f + 0.06f * kotlin.math.sin(phase * Math.PI * 2).toFloat()
            State.THINKING -> 0.91f + 0.04f * kotlin.math.sin(phase * Math.PI * 2).toFloat()
            State.SPEAKING -> 0.91f + 0.10f * kotlin.math.sin(phase * Math.PI * 4).toFloat()
            State.IDLE -> 0.96f
        }
        val radius = baseRadius * pulse

        haloPaint.shader = RadialGradient(
            cx,
            cy,
            baseRadius * 1.65f,
            intArrayOf(0xA86C4DFF.toInt(), 0x504D29FF.toInt(), 0x001A1730),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, baseRadius * 1.62f, haloPaint)

        centerPaint.shader = LinearGradient(
            0f,
            cy - radius,
            0f,
            cy + radius,
            intArrayOf(0xFFC38CFF.toInt(), 0xFF6647FF.toInt(), 0xFF3320A5.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, radius, centerPaint)

        centerPaint.shader = RadialGradient(
            cx,
            cy - radius * 0.18f,
            radius * 0.95f,
            intArrayOf(0xFFFFFFFF.toInt(), 0xFFDDD6FF.toInt(), 0x00000000),
            floatArrayOf(0f, 0.25f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy - radius * 0.12f, radius * 0.76f, centerPaint)

        val eyeRadius = radius * 0.105f
        eyePaint.shader = null
        eyePaint.color = 0xFFE7ECFF.toInt()
        canvas.drawCircle(cx - radius * 0.26f, cy - radius * 0.05f, eyeRadius, eyePaint)
        canvas.drawCircle(cx + radius * 0.26f, cy - radius * 0.05f, eyeRadius, eyePaint)

        smilePaint.color = 0xFFB9D1FF.toInt()
        val smileRect = RectF(
            cx - radius * 0.20f,
            cy + radius * 0.02f,
            cx + radius * 0.20f,
            cy + radius * 0.23f,
        )
        canvas.drawArc(smileRect, 15f, 150f, false, smilePaint)

        wavePaint.color = 0xFF8E9CFF.toInt()
        val waveX = radius * 1.55f
        val waveCenter = cy + radius * 0.02f
        val waveHeights = when (state) {
            State.SPEAKING -> floatArrayOf(0.24f, 0.42f, 0.66f, 0.40f, 0.24f)
            State.THINKING -> floatArrayOf(0.18f, 0.28f, 0.42f, 0.28f, 0.18f)
            else -> floatArrayOf(0.14f, 0.22f, 0.32f, 0.22f, 0.14f)
        }
        val spacing = radius * 0.20f
        val startX = cx - waveX
        waveHeights.forEachIndexed { index, heightFactor ->
            val x = startX + index * spacing
            val h = radius * heightFactor * (0.8f + 0.2f * kotlin.math.sin((phase + index * 0.15f) * Math.PI * 2).toFloat())
            canvas.drawLine(x, waveCenter - h, x, waveCenter + h, wavePaint)
            canvas.drawLine(cx * 2f - x, waveCenter - h, cx * 2f - x, waveCenter + h, wavePaint)
        }
    }

    private fun Float.dp(context: Context): Float =
        this * context.resources.displayMetrics.density
}
