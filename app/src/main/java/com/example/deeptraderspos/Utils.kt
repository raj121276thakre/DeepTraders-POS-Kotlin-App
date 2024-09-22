package com.example.deeptraderspos

import android.app.Activity
import android.os.Build
import androidx.core.content.ContextCompat
 // Import your resource file for colors

object Utils {
    fun setStatusBarColor(activity: Activity) {
        // Check if the version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Use a predefined color, for example, colorPrimary
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.statusBarColor)
        }
    }
}
