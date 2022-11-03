package ru.prodimex.digitaldispatcher

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.loader.content.Loader
import java.util.*
import java.lang.Long.parseLong
import kotlin.collections.HashMap


class LoaderPageDriverView(_scene: LoaderPageController, _uuid:String) {
    companion object {
        const val ACCEPT_CONNECTION = "0"
        /*val infoTypes = mapOf( "connect_to_me" to "0" )*/

        var infoTypesByNum = hashMapOf("0" to "Хочет подключиться к похрузчику")
        var inited = false

        fun getNumberFromNumberCode(_numberCode:String):String {
            var number = ""
            for (i in 0 until _numberCode.length / 2) {
                number += Dictionary.carNumberCharsByHex[_numberCode.slice(i * 2..i * 2 + 1)]
            }
            return number
        }
        fun getNumberFromUUID(_uuid:String):String {
            /*if (!inited) {
                for((key, value ) in Main.carNumberChars) charByHexForCarNumbers[value] = key
                inited = true
            }*/

            var uuidTail = _uuid.slice (   LoaderPageController.fieldId.length.._uuid.length-1)
            uuidTail = uuidTail.replace("-", "", true)

            return getNumberFromNumberCode(uuidTail.slice (1..parseLong(uuidTail[0].toString(), 16).toInt() * 2))
        }
        fun getActionFromUUID(_uuid:String):Int {
            var uuidTail = _uuid.slice (   LoaderPageController.fieldId.length.._uuid.length-1)
            uuidTail = uuidTail.replace("-", "", true)
            var numlength = parseLong(uuidTail[0].toString(), 16).toInt()
            uuidTail = uuidTail.slice (1..uuidTail.length - 1)
            uuidTail = uuidTail.slice (numlength*2..uuidTail.length - 1)

            return parseLong(uuidTail[0].toString(), 16).toInt()
        }
    }
    private val scene = _scene
    private val view: LinearLayout = scene.scene.layoutInflater.inflate(R.layout.loader_page_driver_block, null) as LinearLayout
    private val space = Space(scene.scene.applicationContext)

    val number = getNumberFromUUID(_uuid)
    private var pingCounter = 0
    private var offlinePingCounter = 0
    var online = false
    var action = getActionFromUUID(_uuid)
    init {
        Main.log("loh $number ${infoTypesByNum[action.toString()]}")
        view.tag = number

        scene.listContainer.addView(view)
        space.minimumHeight = 10
        scene.listContainer.addView(space)

        setOnClick(R.id.accept_driver_connection) {
            var uuid = number.length.let {Integer.toHexString(it).uppercase()}
            uuid += DriverBeacon.makeCodeFromNumber(number)
            uuid += LoaderPageController.fieldId
            uuid += ACCEPT_CONNECTION
            uuid = DriverBeacon.completeRawUUID(uuid)

            Main.log("uuid ${uuid}")
            Beacons.createBeacon(uuid)
            vis(R.id.accept_driver_connection, false)
        }
        ping(_uuid)
    }

    @SuppressLint("SetTextI18n")
    fun updateView() {
        view.findViewById<TextView>(R.id.driver_number).text = "АМ номер: $number $action ${infoTypesByNum[action.toString()]}"
        view.findViewById<TextView>(R.id.ping_count).text = "$pingCounter"
        view.findViewById<TextView>(R.id.offline_ping_count).text = "$offlinePingCounter"
        if(online) {
            view.setBackgroundColor(0xFFCCCCCC.toInt())
        } else {
            view.setBackgroundColor(0xFFE6C2C2.toInt())
        }
    }

    fun setOnClick(_btnId:Int, _callback:(()->Unit)) {
        (view.findViewById<Button>(_btnId)).setOnClickListener { _callback.invoke() }
    }

    fun ping(_uuid:String? = null) {
        pingCounter++
        if(_uuid == null) {
            if(offlinePingCounter > 5) online = false
            offlinePingCounter ++
        } else {
            online = true
            offlinePingCounter = 0
        }
        //Main.log("ping $_uuid")
        updateView()
    }

    fun removedFromList() {
        scene.listContainer.removeView(view)
        scene.listContainer.removeView(space)
    }

    private fun vis(_id:Int, _show:Boolean = true) {
        (view.findViewById(_id) as View).visibility = if(_show) View.VISIBLE else View.GONE
    }
}