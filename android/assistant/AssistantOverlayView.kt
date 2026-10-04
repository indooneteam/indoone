package com.indoone.assistant

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class AssistantOverlayView(
    context: Context,
    private val onClose: () -> Unit,
) : FrameLayout(context) {

    private val statusView = TextView(context)
    private val transcriptView = TextView(context)

    init {
        setPadding(16.dp(), 10.dp(), 10.dp(), 10.dp())

        val pill = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.argb(238, 18, 15, 31))
                cornerRadius = 28.dp().toFloat()
                setStroke(1.dp(), Color.argb(80, 184, 150, 255))
            }
            elevation = 12.dp().toFloat()
            setPadding(16.dp(), 12.dp(), 8.dp(), 12.dp())
        }

        val copy = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(context).apply {
            text = "Indoone"
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        statusView.apply {
            text = "Listening…"
            textSize = 12f
            setTextColor(Color.rgb(186, 167, 255))
        }

        transcriptView.apply {
            text = ""
            textSize = 13f
            setTextColor(Color.rgb(232, 226, 245))
            maxLines = 2
            visibility = View.GONE
        }

        copy.addView(title)
        copy.addView(statusView)
        copy.addView(
            transcriptView,
            LinearLayout.LayoutParams(
                220.dp(),
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = 4.dp()
            },
        )

        val close = TextView(context).apply {
            text = "×"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            isClickable = true
            isFocusable = true
            contentDescription = "Close Indoone Assistant"
            setOnClickListener { onClose() }
        }

        pill.addView(
            copy,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            ),
        )
        pill.addView(
            close,
            LinearLayout.LayoutParams(42.dp(), 42.dp()),
        )

        addView(
            pill,
            LayoutParams(
                340.dp(),
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM,
            ).apply {
                bottomMargin = 28.dp()
            },
        )
    }

    fun setStatus(text: String) {
        statusView.text = text
    }

    fun setTranscript(text: String) {
        val clean = text.trim()
        transcriptView.text = clean
        transcriptView.visibility = if (clean.isBlank()) View.GONE else View.VISIBLE
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}
