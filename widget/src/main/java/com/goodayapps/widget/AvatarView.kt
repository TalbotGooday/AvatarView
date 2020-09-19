package com.goodayapps.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.goodayapps.widget.utils.convertDpToPixel
import kotlin.math.min

open class AvatarView : AppCompatImageView {
	companion object {
		const val DEFAULT_TEXT_SIZE_PERCENTAGE = 1f
	}

	@ColorInt
	var borderColor: Int = Color.BLACK
	var textTypeface: Typeface? = null

	@ColorInt
	var textColor: Int = Color.WHITE

	@ColorInt
	var backgroundPlaceholderColor: Int = Color.BLACK
	var textSize: Float = -1f
	var placeholderText: CharSequence? = "?"
	var iconDrawableScale: Float = .5f
		set(value) {
			field = value
			postInvalidate()
		}

	@Dimension(unit = Dimension.PX)
	var borderWidth: Int = 0
		set(value) {
			field = value
			postInvalidate()
		}
	var backgroundGradient: Boolean = false
		set(value) {
			field = value
			postInvalidate()
		}
	var avatarGradient: Boolean = false
		set(value) {
			field = value
			postInvalidate()
		}
	var textSizePercentage: Float = DEFAULT_TEXT_SIZE_PERCENTAGE
		set(value) {
			field = value
			postInvalidate()
		}

	@Dimension(unit = Dimension.PX)
	var avatarMargin: Int = 0
		set(value) {
			field = value
			postInvalidate()
		}
	var volumetricType: AvatarDrawable.VolumetricType = AvatarDrawable.VolumetricType.ALL
		set(value) {
			field = value
			postInvalidate()
		}

	private var circleRadius: Int = 0
	private var circleCenterXValue: Int = 0
	private var circleCenterYValue: Int = 0
	private var viewSize: Int = 0

	private var circleRect: Rect? = null

	init {
		borderWidth = context.convertDpToPixel(1)
		borderColor = Color.BLACK
	}

	constructor(context: Context) : super(context) {
		init(context, null)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init(context, attrs)
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		init(context, attrs)
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr) {
		init(context, attrs)
	}

	private fun init(context: Context, attrs: AttributeSet?) {
		if (attrs != null) {
			val typedArray = context.theme.obtainStyledAttributes(
					attrs,
					R.styleable.AvatarView,
					0,
					0
			)
			try {
				configureStyleValues(typedArray)
			} finally {
				typedArray.recycle()
			}
		}
	}

	override fun onDraw(canvas: Canvas?) {
		//Reject drawing original image
	}

	override fun dispatchDraw(canvas: Canvas?) {
		super.dispatchDraw(canvas)

		val size = measuredWidth.coerceAtMost(measuredHeight).coerceAtLeast(context.convertDpToPixel(10))

		val newDrawable = AvatarDrawable(AvatarDrawable.Options().apply {
			this.avatarDrawable = drawable
			this.placeholderText = this@AvatarView.placeholderText
			this.size = size
			this.textColor = this@AvatarView.textColor
			val textSizeFin = if (this@AvatarView.textSize <= 0f) {
				size / 3f
			} else {
				this@AvatarView.textSize
			}
			this.textSize = textSizeFin * textSizePercentage
			this.borderColor = this@AvatarView.borderColor
			this.backgroundPlaceholderColor = this@AvatarView.backgroundPlaceholderColor
			this.borderWidth = this@AvatarView.borderWidth
			this.volumetricType = this@AvatarView.volumetricType
			this.textTypeface = this@AvatarView.textTypeface
			this.iconDrawableScale = this@AvatarView.iconDrawableScale
			this.avatarMargin = this@AvatarView.avatarMargin
		})

		canvas?.let { newDrawable.draw(it) }
	}


	private fun configureStyleValues(typedArray: TypedArray) {
		backgroundPlaceholderColor = typedArray.getColor(R.styleable.AvatarView_avBackgroundColor, Color.TRANSPARENT)
		borderColor = typedArray.getColor(R.styleable.AvatarView_avBorderColor, borderColor)
		borderWidth = typedArray.getDimensionPixelSize(R.styleable.AvatarView_avBorderWidth, borderWidth)
		textSizePercentage = typedArray.getFloat(R.styleable.AvatarView_avTextSizePercentage, DEFAULT_TEXT_SIZE_PERCENTAGE)
		volumetricType = AvatarDrawable.VolumetricType.from(typedArray.getInt(R.styleable.AvatarView_avVolumetricType, -1))
		placeholderText = typedArray.getText(R.styleable.AvatarView_placeholderText)
		iconDrawableScale = typedArray.getFloat(R.styleable.AvatarView_iconDrawableScale, iconDrawableScale)
		avatarMargin = typedArray.getDimensionPixelSize(R.styleable.AvatarView_avAvatarMargin, avatarMargin)

		val typefaceId = typedArray.getResourceId(R.styleable.AvatarView_android_fontFamily, 0)
		if (typefaceId != 0) {
			textTypeface = ResourcesCompat.getFont(this.context, typefaceId)
		}

		//Get Drawable
		typedArray.getDrawable(R.styleable.AvatarView_android_src)?.let { setImageDrawable(it) }
	}

	private fun saveBasicValues(canvas: Canvas) {
		val viewHeight = canvas.height
		val viewWidth = canvas.width

		viewSize = min(viewWidth, viewHeight)

		circleCenterXValue = (viewWidth - viewSize) / 2
		circleCenterYValue = (viewHeight - viewSize) / 2
		circleRadius = (viewSize - borderWidth * 2) / 2

		circleRect = Rect(0, 0, viewSize, viewSize)

		maximizeAvailableBorderSize()
	}

	private fun maximizeAvailableBorderSize() {
		if (viewSize / 3 < borderWidth) {
			borderWidth = viewSize / 3
		}
	}

	override fun drawableStateChanged() {
		super.drawableStateChanged()
		invalidate()
	}

	class SavedState : BaseSavedState {
		var defaultBorderColor: Int = 0
		var defaultCenterColor: Int = 0
		var defaultBorderWidth: Int = 0
		var defaultOutlineWidth: Int = 0

		constructor(source: Parcel?) : super(source) {
			source ?: return
			defaultBorderColor = source.readInt()
			defaultCenterColor = source.readInt()
			defaultBorderWidth = source.readInt()
			defaultOutlineWidth = source.readInt()
		}

		constructor(superState: Parcelable?) : super(superState) {}

		@RequiresApi(Build.VERSION_CODES.N)
		constructor(source: Parcel?, loader: ClassLoader?) : super(source, loader)

		override fun writeToParcel(parcel: Parcel, flags: Int) {
			super.writeToParcel(parcel, flags)
			parcel.writeInt(defaultBorderColor)
			parcel.writeInt(defaultCenterColor)
			parcel.writeInt(defaultBorderWidth)
			parcel.writeInt(defaultOutlineWidth)
		}

		override fun describeContents(): Int {
			return 0
		}

		companion object CREATOR : Parcelable.Creator<SavedState> {
			override fun createFromParcel(parcel: Parcel): SavedState {
				return SavedState(parcel)
			}

			override fun newArray(size: Int): Array<SavedState?> {
				return arrayOfNulls(size)
			}
		}

	}
}