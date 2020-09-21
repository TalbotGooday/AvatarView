package com.goodayapps.widget.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes


internal fun Context.convertDpToPixel(dp: Int): Int {
	val metrics = resources.displayMetrics
	return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

internal fun Context.colorAttribute(@AttrRes colorRes: Int): Int {
	val typedValue = TypedValue()
	val theme: Resources.Theme = theme
	theme.resolveAttribute(colorRes, typedValue, true)
	return typedValue.data
}