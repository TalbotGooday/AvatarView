package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import kotlin.math.cos
import kotlin.math.sin

class AvatarDrawable(
		placeholderText: CharSequence?,
		private val size: Int,
		private val textSize: Float,
		private val borderWidth: Int,
		private val backgroundColor: Int = Color.BLACK,
		private val textColor: Int = Color.WHITE,
		private val borderColor: Int = Color.BLACK,
		private val borderColorSecondary: Int? = null,
		private val borderGradientAngle: Int = 0,
		private val avatarDrawable: Drawable?,
		private val iconDrawableScale: Float = .5f,
		private val volumetricType: VolumetricType = VolumetricType.ALL,
		private val textTypeface: Typeface? = null,
		private val avatarMargin: Int = 0,
) : Drawable() {
	private var textLayout: StaticLayout? = null

	private var placeholderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
		this.textSize = this@AvatarDrawable.textSize
		this.color = textColor

		if (textTypeface != null) {
			this.typeface = textTypeface
		}
		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
	}

	private var textOverPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
		this.textSize = this@AvatarDrawable.textSize
		this.color = textColor

		if (textTypeface != null) {
			this.typeface = textTypeface
		}
		this.style = Paint.Style.FILL

		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
	}

	private var avatarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		this.color = backgroundColor
		this.isAntiAlias = true
		this.style = Paint.Style.FILL
	}

	private val gradientPaint = Paint().apply {
		this.isAntiAlias = true
		this.shader = LinearGradient(
				0f,
				size * .94f,
				0f,
				0f,
				0,
				Color.argb(102, 255, 255, 255),
				Shader.TileMode.CLAMP
		)
	}

	private val borderPaint = Paint().apply {
		this.isAntiAlias = true
		this.style = Paint.Style.STROKE
		this.strokeWidth = borderWidth.toFloat()
		this.shader = getGradientShader(borderColor, borderColorSecondary ?: borderColor)
	}

	private val textOverBackgroundPaint = Paint().apply {
		this.isAntiAlias = true
		this.style = Paint.Style.STROKE
		this.strokeWidth = borderWidth * 2f
		this.shader = getGradientShader(Color.TRANSPARENT, Color.GREEN)
	}

	private val clipPaint = Paint().apply {
		this.color = Color.WHITE
		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
		this.style = Paint.Style.FILL_AND_STROKE
	}

	private var bufferBitmap: Bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
	private var bufferCanvas: Canvas = Canvas(bufferBitmap)

	private var textWidth = 0f
	private var textHeight = 0f
	private var textLeft = 0f

	private var circleRadius = 0f
	private var circleCenter = 0f
	private var backgroundCircleRadius = 0f

	constructor(options: Options) : this(
			options.placeholderText,
			options.size,
			options.textSize,
			options.borderWidth.coerceAtMost(options.size / 2),
			options.backgroundPlaceholderColor,
			options.textColor,
			options.borderColor,
			options.borderColorSecondary,
			options.borderGradientAngle,
			options.avatarDrawable,
			options.iconDrawableScale,
			options.volumetricType,
			options.textTypeface,
			options.avatarMargin.coerceAtMost(options.size / 2)
	)

	init {
		circleRadius = (size / 2.0f)
		circleCenter = circleRadius
		backgroundCircleRadius = circleRadius - avatarMargin - (borderWidth * .90f)

		textLayout = placeholderText?.let {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				StaticLayout.Builder.obtain(it, 0, it.length, placeholderPaint, size)
						.setAlignment(Layout.Alignment.ALIGN_CENTER)
						.setLineSpacing(1f, 1f)
						.setIncludePad(false)
						.build()
			} else {
				StaticLayout(
						it,
						placeholderPaint,
						size,
						Layout.Alignment.ALIGN_CENTER,
						1f,
						1f,
						false
				)
			}
		}?.also {
			if (it.lineCount > 0) {
				textLeft = it.getLineLeft(0)
				textWidth = it.getLineWidth(0)
				textHeight = it.getLineBottom(it.lineCount - 1).toFloat()
			}
		}
	}

	override fun draw(canvas: Canvas) {
		canvas.save()
		canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

		var isIconDrawable = false

		val avatarBitmap: Bitmap? = when (avatarDrawable) {
			is BitmapDrawable -> {
				val bitmapSize = size - avatarMargin

				ThumbnailUtils.extractThumbnail(avatarDrawable.bitmap, bitmapSize, bitmapSize)
			}
			is Drawable -> {
				isIconDrawable = true
				val width = (size * iconDrawableScale).toInt()
				val height = (size * iconDrawableScale).toInt()

				if (width > 0 && height > 0) {
					Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
						avatarDrawable.setBounds(0, 0, width, height)

						avatarDrawable.draw(Canvas(it))
					}
				} else {
					null
				}
			}
			else -> {
				null
			}
		}

		//Drawing a background circle and a clip circle for the avatar
		bufferCanvas.drawCircle(circleCenter, circleCenter, backgroundCircleRadius, avatarBackgroundPaint)

		if (avatarBitmap == null) {
			drawPlaceholder()
		} else {
			drawBitmap(isIconDrawable, avatarBitmap)
		}

		//Draw volumetric gradient
		drawVolume(avatarBitmap != null)

		//Draw Border
		if (borderWidth > 0) {
			bufferCanvas.drawCircle(
					circleCenter,
					circleCenter,
					circleRadius - (borderWidth / 2f),
					borderPaint
			)
		}

		//TODO drawTextOver()

		canvas.drawBitmap(bufferBitmap, Matrix(), null)
		canvas.restore()
	}

	private fun drawTextOver() {
		val radius = circleRadius - borderWidth
		bufferCanvas.drawCircle(
				circleCenter,
				circleCenter,
				radius,
				textOverBackgroundPaint
		)

		val path = Path()
		bufferCanvas.save()
		bufferCanvas.rotate((borderGradientAngle + 45).toFloat(), circleCenter, circleCenter)
		path.addCircle(circleCenter, circleCenter, radius, Path.Direction.CCW)
		bufferCanvas.drawTextOnPath("#OPENTOWORK", path, 0f, 0f, textOverPaint)
		bufferCanvas.restore()
	}

	override fun setAlpha(alpha: Int) {
	}

	override fun setColorFilter(colorFilter: ColorFilter?) {

	}

	override fun getOpacity(): Int {
		return PixelFormat.TRANSLUCENT
	}

	private fun drawBitmap(isIconDrawable: Boolean, avatarBitmap: Bitmap) {
		if (isIconDrawable) {
			//Draw background

			val left = ((size - avatarBitmap.width) / 2f)
			val top = ((size - avatarBitmap.height) / 2f)

			bufferCanvas.drawBitmap(avatarBitmap, left, top, clipPaint)
		} else {
			bufferCanvas.drawBitmap(avatarBitmap, Matrix(), clipPaint)
		}
	}

	private fun drawPlaceholder() {
		//Draw text
		if (textLayout != null) {
			bufferCanvas.save()
			bufferCanvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2)
			textLayout?.draw(bufferCanvas)
			bufferCanvas.restore()
		}
	}

	private fun drawVolume(isDrawable: Boolean) {
		if (volumetricType == VolumetricType.NONE) return
		if (volumetricType == VolumetricType.DRAWABLE && isDrawable.not()) return
		if (volumetricType == VolumetricType.PLACEHOLDER && isDrawable) return

		bufferCanvas.drawCircle(circleCenter, circleCenter, backgroundCircleRadius, gradientPaint)
	}

	private fun getGradientShader(@ColorInt colorStart: Int, @ColorInt colorEnd: Int): LinearGradient {
		val angleInRadians = Math.toRadians(borderGradientAngle.toDouble())
		val length = size

		val r = size / 2f
		val centerX: Float = length / 2f
		val centerY: Float = length / 2f

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

	class Options {
		@ColorInt
		var textColor: Int = Color.WHITE

		@ColorInt
		var backgroundPlaceholderColor: Int = Color.BLACK

		@ColorInt
		var borderColor: Int = Color.BLACK

		@ColorInt
		var borderColorSecondary: Int? = null

		var borderGradientAngle: Int = 0

		@Dimension(unit = Dimension.PX)
		var textSize: Float = 0f

		@Dimension(unit = Dimension.PX)
		var size: Int = 0

		@Dimension(unit = Dimension.PX)
		var borderWidth: Int = 0
		var placeholderText: CharSequence? = "?"
		var avatarDrawable: Drawable? = null
		var iconDrawableScale: Float = .5f
		var volumetricType: VolumetricType = VolumetricType.ALL
		var textTypeface: Typeface? = null

		@Dimension(unit = Dimension.PX)
		var avatarMargin: Int = 0
	}

	enum class VolumetricType(val value: Int) {
		NONE(-1),
		ALL(0),
		DRAWABLE(1),
		PLACEHOLDER(2);

		companion object {
			fun from(value: Int) = values().firstOrNull { it.value == value } ?: NONE
		}
	}
}