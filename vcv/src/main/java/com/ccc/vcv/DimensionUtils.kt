package com.ccc.vcv

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.annotation.DimenRes

object DimensionUtils {
    fun getDimensionWithDensity(context: Context, @DimenRes dimenRes: Int): Float {
        return context.resources.getDimension(dimenRes) / context.resources.displayMetrics.density
    }

    fun getDimensionWithScaledDensity(context: Context, @DimenRes dimenRes: Int): Float {
        return context.resources.getDimension(dimenRes) / context.resources.displayMetrics.scaledDensity
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun calculateScreenHeightForLollipop(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y
    }
}