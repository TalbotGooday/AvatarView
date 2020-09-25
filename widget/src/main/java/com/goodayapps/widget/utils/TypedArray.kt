package com.goodayapps.widget.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat

@ColorInt
fun TypedArray.getColorOrNull(@StyleableRes index: Int): Int? {
	val color = getColor(index, Integer.MAX_VALUE)

	return if (color == Integer.MAX_VALUE) {
		null
	} else {
		color
	}
}

fun TypedArray.getTypefaceOrNull(context: Context, @StyleableRes index: Int): Typeface? {
	val resId = getResourceId(index, 0)

	return if (resId != 0) {
		ResourcesCompat.getFont(context, resId)
	}else{
		null
	}
}