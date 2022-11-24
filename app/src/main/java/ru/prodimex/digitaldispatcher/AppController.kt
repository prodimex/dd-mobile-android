package ru.prodimex.digitaldispatcher

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import org.altbeacon.beacon.Beacon

open class AppController {
    val scene = Main.main
    lateinit var listContainer: LinearLayout
    var scanStarted = false
    var rootLayer:RelativeLayout = scene.findViewById(R.id.root_layer)
    var currentPageId = ""
    open fun scanObserver(beacons:Collection<Beacon>) {

    }

    var pageKilled = false
    init {

    }

    open fun init(_layoutId:Int) {
        showLayout(_layoutId)
        Beacons.controller = this
    }

    fun showLayout(_layoutId:Int) {
        rootLayer.removeAllViews()
        rootLayer.addView((scene.layoutInflater.inflate(_layoutId, null) as View))
    }

    open fun switchTopage(_pageId:String) {
        if(currentPageId == _pageId)
            return

        Main.log("PAGE SWITCHED TO: $currentPageId")
        pageKilled = true
        scene.showPage(_pageId)
    }

    open fun updateView() {
        if(pageKilled)
            return
    }

    fun setOnClick(_id:Int, _callback:(()->Unit)) {
        scene.findViewById<View>(_id).setOnClickListener{ _callback.invoke() }
    }

    fun vis(_id:Int, _show:Boolean = true) {
        (scene.findViewById(_id) as View).visibility = if(_show) View.VISIBLE else View.GONE
    }

    fun setText(_id:Int, _text:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (scene.findViewById<TextView>(_id)).text = Html.fromHtml(_text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            (scene.findViewById<TextView>(_id)).text = Html.fromHtml(_text)
        }
    }

    fun afterInit(_pageId: String) {
        currentPageId = _pageId
        scene.findViewById<TextView>(R.id.app_version_text).text = "Версия: ${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TYPE}"
        updateView()
    }

    fun highlightButton(_id:Int, _color:Int = R.color.text_orange_light) {
        scene.findViewById<LinearLayout>(_id).setBackgroundColor(ContextCompat.getColor(scene.applicationContext, _color))
    }

    fun highlightIcon(_id:Int, _color:Int = R.color.text_gray) {
        scene.findViewById<ImageView>(_id).setColorFilter(ContextCompat.getColor(scene.applicationContext, _color), android.graphics.PorterDuff.Mode.SRC_IN)
    }

    fun highlightText(_id:Int, _color:Int = R.color.text_gray) {
        scene.findViewById<TextView>(_id).setTextColor(ContextCompat.getColor(scene.applicationContext, _color))
    }

    fun showErrorByCode(_response:HashMap<String, Any>) {
        var code = _response["responseCode"].toString()
        val errText:TextView? = scene.findViewById(R.id.error_field_app_version)
        if (errText == null) {
            Main.main.toastMe(Dict.getErrorByCode(code))
        } else {
            scene.findViewById<TextView>(R.id.error_field_app_version).text = Dict.getErrorByCode(code)
        }
    }

    fun hideError() {
        val errText:TextView? = scene.findViewById(R.id.error_field_app_version)
        if (errText != null) {
            scene.findViewById<TextView>(R.id.error_field_app_version).text = ""
        }
    }
}