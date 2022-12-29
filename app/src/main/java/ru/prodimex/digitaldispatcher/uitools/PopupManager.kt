package ru.prodimex.digitaldispatcher.uitools

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.R

class PopupManager {
    companion object {
        val popupLayer:RelativeLayout
            get() = Main.main.findViewById(R.id.popup_layer)

        var popupView:View? = null
        fun killPopup() {
            popupLayer.removeAllViews()
            popupView = null
        }

        fun showAlert(_message:String, _header:String = "Внимание") {
            killPopup()
            popupView = Main.main.layoutInflater.inflate(R.layout.alert_layout, null)
            if(_header == "")
                popupView!!.findViewById<TextView>(R.id.alert_header_text).visibility = View.GONE

            setText(R.id.alert_header_text, "<b>$_header</b>")
            setText(R.id.alert_body_text, _message)

            popupLayer.addView(popupView)
            setOnClick(R.id.alert_background) { }
            setOnClick(R.id.ok_alert_button) {
                killPopup()
            }
        }

        fun showYesNoDialog(_message:String, _header:String = "Внимание", _onAccept:(()->Unit)? = null) {
            killPopup()
            popupView = Main.main.layoutInflater.inflate(R.layout.yes_no_popup, null)
            if(_header == "")
                popupView!!.findViewById<TextView>(R.id.alert_header_text).visibility = View.GONE

            setText(R.id.alert_header_text, "<b>$_header</b>")
            setText(R.id.alert_body_text, _message)

            popupLayer.addView(popupView)
            setOnClick(R.id.alert_background) { }
            setOnClick(R.id.ok_alert_button) {
                if(_onAccept != null)
                    _onAccept!!()

                killPopup()
            }
            setOnClick(R.id.cancel_alert_button) {
                killPopup()
            }
        }

        private fun setOnClick(_id:Int, _callback:(()->Unit)) {
            popupView!!.findViewById<View>(_id).setOnClickListener{ _callback.invoke() }
        }

        /*private fun vis(_id:Int, _show:Boolean = true) {
            (scene.findViewById(_id) as View).visibility = if(_show) View.VISIBLE else View.GONE
        }*/

        private fun setText(_id:Int, _text:String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                (popupView!!.findViewById<TextView>(_id)).text = Html.fromHtml(_text, Html.FROM_HTML_MODE_COMPACT)
            } else {
                (popupView!!.findViewById<TextView>(_id)).text = Html.fromHtml(_text)
            }
        }
    }
}