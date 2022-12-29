package ru.prodimex.digitaldispatcher.uitools

import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView


class PreloadingButton(_button:View, _preloader:View) {
    val button = _button as Button
    val preloader = _preloader as ImageView
    val buttonText = button.text
    init {
        preloader.visibility = View.GONE
    }
    fun startPreloading() {
        button.text = ""
        preloader.visibility = View.VISIBLE

        val r = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 750
        r.repeatCount = Animation.INFINITE
        r.interpolator = LinearInterpolator()

        preloader.startAnimation(r)
        button.isEnabled = false
    }
    fun stopPreloading() {
        if(!button.isEnabled)
            preloader.animation.cancel()
            preloader.clearAnimation()
        button.isEnabled = true
        button.text = buttonText
        preloader.visibility = View.GONE
    }
}