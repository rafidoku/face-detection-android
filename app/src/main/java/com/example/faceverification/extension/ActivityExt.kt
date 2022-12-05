package com.example.faceverification.extension
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

@SuppressLint("RestrictedApi")
fun AppCompatActivity.setNavigationBar(title: String, titleColor: Int = Color.BLACK, isHide: Boolean, backgroundColor: Int, icon: Int?, iconColor: Int?) {
    supportActionBar?.title = title
    supportActionBar?.setShowHideAnimationEnabled(false)
    val toolbar = findViewById<Toolbar>(com.google.android.material.R.id.action_bar)
    toolbar.setTitleTextColor(titleColor)
    toolbar.setBackgroundColor(backgroundColor)

    if (icon != null) {
        toolbar.setNavigationIcon(icon)
        toolbar.navigationIcon?.setTint(iconColor!!)
    }

    if (isHide) {
        supportActionBar?.hide()
    } else {
        supportActionBar?.show()
    }
}