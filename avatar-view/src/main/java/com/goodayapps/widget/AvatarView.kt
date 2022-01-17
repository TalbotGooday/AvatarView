package com.goodayapps.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import com.goodayapps.widget.utils.*
import com.goodayapps.widget.utils.colorAttribute
import com.goodayapps.widget.utils.convertDpToPixel

open class AvatarView : AppCompatImageView {
    companion object {
        const val DEFAULT_TEXT_SIZE_PERCENTAGE = 1f
    }

    @ColorInt
    var borderColor: Int = Color.BLACK

    @ColorInt
    var borderColorSecondary: Int? = null


    var textTypeface: Typeface? = null

    @ColorInt
    var textColor: Int = Color.WHITE
    var textSize: Float = -1f

    @ColorInt
    var backgroundPlaceholderColor: Int = Color.BLACK
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
    var textSizePercentage: Float = DEFAULT_TEXT_SIZE_PERCENTAGE
        set(value) {
            field = value
            postInvalidate()
        }

    var textOverSizePercentage: Float = DEFAULT_TEXT_SIZE_PERCENTAGE
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
    var volumetricType: AvatarDrawable.Volumetric = AvatarDrawable.Volumetric.ALL
        set(value) {
            field = value
            postInvalidate()
        }

    @IntRange(from = 0, to = 360)
    var borderGradientAngle = 0
        set(value) {
            field = value
            postInvalidate()
        }

    @IntRange(from = 0, to = 360)
    var archesDegreeArea = 0
        set(value) {
            field = value
            postInvalidate()
        }

    @IntRange(from = 0, to = 360)
    var archesAngle = 0
        set(value) {
            field = value
            postInvalidate()
        }
    var archesCount = 0
        set(value) {
            field = value
            postInvalidate()
        }
    var archesType = AvatarDrawable.Border.ARCH_TYPE_DEFAULT
        set(value) {
            field = value
            postInvalidate()
        }

    var blurHash: String? = null
        set(value) {
            field = value
            if (value != null) {
                setImageBitmap(BlurHashDecoder.decode(value, 20, 20))
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
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

        val size =
            measuredWidth.coerceAtMost(measuredHeight).coerceAtLeast(context.convertDpToPixel(10))

        val newDrawable = avatarDrawable {
            drawable(drawable)
            size(size)
            backgroundColor(backgroundPlaceholderColor)
            volumetric(this@AvatarView.volumetricType)
            iconDrawableScale(this@AvatarView.iconDrawableScale)
            avatarMargin(this@AvatarView.avatarMargin)

            border {
                width(borderWidth)
                color(borderColor)
                colorSecondary(borderColorSecondary)
                gradientAngle(borderGradientAngle)
                archesCount(this@AvatarView.archesCount)
                archesDegreeArea(this@AvatarView.archesDegreeArea)
                archesAngle(this@AvatarView.archesAngle)
                archesType(this@AvatarView.archesType)
            }

            placeholder {
                text(placeholderText)
                color(textColor)
                size((if (textSize <= 0f) size / 3f else textSize) * textSizePercentage)
                typeface(textTypeface)
            }
        }

        canvas?.let { newDrawable.draw(it) }
    }

    private fun configureStyleValues(typedArray: TypedArray) {
        val colorAccent = context.colorAttribute(R.attr.colorAccent)
        backgroundPlaceholderColor =
            typedArray.getColor(R.styleable.AvatarView_avBackgroundColor, colorAccent)
        borderColor = typedArray.getColor(R.styleable.AvatarView_avBorderColor, colorAccent)
        borderColorSecondary =
            typedArray.getColorOrNull(R.styleable.AvatarView_avBorderColorSecondary)
        borderWidth =
            typedArray.getDimensionPixelSize(R.styleable.AvatarView_avBorderWidth, borderWidth)
        borderGradientAngle =
            typedArray.getInt(R.styleable.AvatarView_avBorderGradientAngle, borderGradientAngle)
                .coerceIn(0, 360)
        textSizePercentage = typedArray.getFloat(
            R.styleable.AvatarView_avTextSizePercentage,
            DEFAULT_TEXT_SIZE_PERCENTAGE
        )
        volumetricType = AvatarDrawable.Volumetric.from(
            typedArray.getInt(
                R.styleable.AvatarView_avVolumetricType,
                -1
            )
        )
        placeholderText = typedArray.getText(R.styleable.AvatarView_placeholderText)
        iconDrawableScale =
            typedArray.getFloat(R.styleable.AvatarView_iconDrawableScale, iconDrawableScale)
        avatarMargin =
            typedArray.getDimensionPixelSize(R.styleable.AvatarView_avAvatarMargin, avatarMargin)
        archesDegreeArea =
            typedArray.getInt(R.styleable.AvatarView_avArchesDegreeArea, archesDegreeArea)
                .coerceIn(0, 360)
        archesAngle =
            typedArray.getInt(R.styleable.AvatarView_avArchesAngle, archesAngle).coerceIn(0, 360)
        archesCount =
            typedArray.getInt(R.styleable.AvatarView_avArchesCount, archesCount).coerceAtLeast(0)
        archesType = typedArray.getInt(R.styleable.AvatarView_avArchesType, archesType)

        textTypeface =
            typedArray.getTypefaceOrNull(context, R.styleable.AvatarView_android_fontFamily)

        //Get Drawable
        typedArray.getDrawable(R.styleable.AvatarView_android_src)?.let { setImageDrawable(it) }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        postInvalidate()
    }

}