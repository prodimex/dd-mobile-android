package ru.prodimex.digitaldispatcher.loader

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.google.gson.internal.LinkedTreeMap
import ru.prodimex.digitaldispatcher.Dict
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.R
import java.math.BigInteger

open class ListItem(_number:String, _shortCut:String) {
    open var PAGE_ID = Dict.LOADER_QUEUE_PAGE
    val number = _number
    val tripId = BigInteger(_shortCut.slice(1.._shortCut.length-1), 16).toString()
    var shortCut = _shortCut
    var surname = ""
    var name = ""
    var patronymic = ""

    open val TAG = "LIST ITEM"

    open var view: LinearLayout = Main.main.layoutInflater.inflate(R.layout.queue_driver_list_item, null) as LinearLayout
    val space = Space(Main.main.applicationContext)
    init {
        if(LoaderAppController.driversInfoCache.contains(number)) {
            var cacheData = LoaderAppController.driversInfoCache[number] as LinkedTreeMap<String, Any>
            surname = cacheData["surname"].toString()
            name = cacheData["name"].toString()
            patronymic = cacheData["patronymic"].toString()
        }

        space.minimumHeight = 12


    }

    @SuppressLint("CutPasteId")
    open fun updateView() {

    }

    fun setText(_id:Int, _text:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text)
        }
    }

    open fun receiveUIIDs(_uuid:String) {

    }
}