package ru.prodimex.digitaldispatcher

import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class TripPageController:AppController() {
    companion object {

    }
    init {
        showLayout(R.layout.driver_trip_page)

        setOnClick(R.id.profile_button) {
            scene.showPage(Main.PROFILE_PAGE)
        }

        setOnClick(R.id.trip_button) {

        }

        setOnClick(R.id.settings_button) {
            scene.showPage(Main.DRIVER_SETTINGS_PAGE)
        }
        scene.findViewById<ImageView>(R.id.trip_ico).setColorFilter(ContextCompat.getColor(scene.applicationContext, R.color.text_yellow), android.graphics.PorterDuff.Mode.SRC_IN)
        scene.findViewById<TextView>(R.id.trip_text).setTextColor(ContextCompat.getColor(scene.applicationContext, R.color.text_yellow))
    }
}