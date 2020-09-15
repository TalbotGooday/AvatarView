package com.goodayapps.widget

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.ColorInt

@Suppress("DEPRECATION")
class AvatarDrawable(
		placeholderText: CharSequence?,
		private val size: Float,
		private val textSize: Float,
		private val borderWidth: Int,
		private val backgroundColor: Int = Color.BLACK,
		private val textColor: Int = Color.WHITE,
		private val borderColor: Int = Color.BLACK,
		private val avatarDrawable: Drawable?,
		private val iconDrawableScale: Float = .5f,
		private val backgroundGradient: Boolean = false,
		private val avatarGradient: Boolean = false,
		private val textTypeface: Typeface? = null,
) : Drawable() {
	private val sizeInt
		get() = size.toInt()
	private var textLayout: StaticLayout? = null

	private var namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
		this.textSize = this@AvatarDrawable.textSize
		this.color = textColor

		if (textTypeface != null) {
			this.typeface = textTypeface
		}
	}

	private var avatarBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		this.color = backgroundColor
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
		this.style = Paint.Style.FILL
		this.color = borderColor
	}

	private val clipPaint = Paint().apply {
		this.color = Color.WHITE
		this.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
	}

	private var bufferBitmap: Bitmap
	private var bufferCanvas: Canvas

	private var textWidth = 0f
	private var textHeight = 0f
	private var textLeft = 0f

	private var circleRadius = 0f
	private var circleCenter = 0f

	constructor(options: Options) : this(
			options.placeholderText,
			options.size,
			options.textSize,
			options.borderWidth,
			options.backgroundPlaceholderColor,
			options.textColor,
			options.borderColor,
			options.avatarDrawable,
			options.iconDrawableScale,
			options.backgroundGradient,
			options.avatarGradient,
			options.textTypeface
	)

	init {
		bufferBitmap = Bitmap.createBitmap(sizeInt, sizeInt, Bitmap.Config.ARGB_8888)
		bufferCanvas = Canvas(bufferBitmap)
		circleRadius = (size / 2.0f) - borderWidth
		circleCenter = size / 2f

		textLayout = placeholderText?.let {
			StaticLayout(
					it,
					namePaint,
					sizeInt,
					Layout.Alignment.ALIGN_CENTER,
					1f,
					1f,
					false
			)
		}?.also {
			if (it.lineCount > 0) {
				textLeft = it.getLineLeft(0)
				textWidth = it.getLineWidth(0)
				textHeight = it.getLineBottom(0).toFloat()
			}
		}
	}

	override fun draw(canvas: Canvas) {
		//Draw Border
		if (borderWidth > 0) {
			canvas.drawCircle(
					(circleRadius + borderWidth),
					(circleRadius + borderWidth),
					(circleRadius + borderWidth),
					borderPaint
			)
		}

		canvas.save()
		canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

		var isIconDrawable = false

		val avatarBitmap: Bitmap? = when (avatarDrawable) {
			is BitmapDrawable -> {
				ThumbnailUtils.extractThumbnail(avatarDrawable.bitmap, sizeInt, sizeInt)
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

		//Draw border
		bufferCanvas.drawCircle(circleCenter, circleCenter, circleRadius, borderPaint)

		if (avatarBitmap == null) {
			drawPlaceholder()
		} else {
			drawBitmap(isIconDrawable, avatarBitmap)
		}

		canvas.drawBitmap(bufferBitmap, Matrix(), null)
		canvas.restore()
	}

	private fun drawBitmap(isIconDrawable: Boolean, avatarBitmap: Bitmap) {
		if (isIconDrawable) {
			//Draw background
			bufferCanvas.drawCircle(circleCenter, circleCenter, circleRadius, avatarBackgroundPaint)

			val left = (size - avatarBitmap.width) / 2f
			val top = (size - avatarBitmap.height) / 2f

			bufferCanvas.drawBitmap(avatarBitmap, left, top, clipPaint)
		} else {
			bufferCanvas.drawBitmap(avatarBitmap, Matrix(), clipPaint)
		}

		//Draw gradient
		if (avatarGradient) {
			bufferCanvas.drawCircle(circleCenter, circleCenter, circleRadius, gradientPaint)
		}
	}

	private fun drawPlaceholder() {
		//Draw background
		bufferCanvas.drawCircle(circleCenter, circleCenter, circleRadius, avatarBackgroundPaint)

		//Draw gradient
		if (backgroundGradient) {
			bufferCanvas.drawCircle(circleCenter, circleCenter, circleRadius, gradientPaint)
		}

		//Draw text
		if (textLayout != null) {
			bufferCanvas.translate((size - textWidth) / 2 - textLeft, (size - textHeight) / 2)
			textLayout?.draw(bufferCanvas)
		}
	}

	override fun setAlpha(alpha: Int) {

	}

	override fun setColorFilter(colorFilter: ColorFilter?) {

	}

	override fun getOpacity(): Int {
		return PixelFormat.TRANSLUCENT
	}

	class Options {
		@ColorInt
		var textColor: Int = Color.WHITE

		@ColorInt
		var backgroundPlaceholderColor: Int = Color.BLACK

		@ColorInt
		var borderColor: Int = Color.BLACK
		var textSize: Float = 33f
		var size: Float = 133f
		var borderWidth: Int = 33
		var placeholderText: CharSequence? = "?"
		var avatarDrawable: Drawable? = null
		var iconDrawableScale: Float = .5f
		var backgroundGradient: Boolean = false
		var avatarGradient: Boolean = false
		var textTypeface: Typeface? = null
	}
}