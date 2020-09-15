package com.goodayapps.widget.utils

import android.content.Context
import android.util.DisplayMetrics


internal fun Context.convertDpToPixel(dp: Int): Int {
	val metrics = resources.displayMetrics
	return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}
