package com.goodayapps.widget.utils

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.annotation.ColorInt
import kotlin.math.cos
import kotlin.math.sin


fun getGradientShader(
		@ColorInt colorStart: Int,
		@ColorInt colorEnd: Int,
		gradientAngle: Double = 0.0,
		size: Int = 100,
): LinearGradient {
	val angleInRadians = Math.toRadians(gradientAngle)

	val r = size / 2f
	val centerX: Float = size / 2f
	val centerY: Float = size / 2f

	val startX = 0.0.coerceAtLeast(size.toDouble().coerceAtMost(centerX - r * cos(angleInRadians))).toFloat()
	val startY = size.toDouble().coerceAtMost(0.0.coerceAtLeast(centerY - r * sin(angleInRadians))).toFloat()

	val endX = 0.0.coerceAtLeast(size.toDouble().coerceAtMost(centerX + r * cos(angleInRadians))).toFloat()
	val endY = size.toDouble().coerceAtMost(0.0.coerceAtLeast(centerY + r * sin(angleInRadians))).toFloat()

	return LinearGradient(
			startX,
			startY,
			endX,
			endY,
			intArrayOf(colorStart, colorEnd),
			null,
			Shader.TileMode.CLAMP
	)
}
