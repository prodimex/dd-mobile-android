package ru.prodimex.digitaldispatcher.scaner

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import ru.prodimex.digitaldispatcher.Beacons
import ru.prodimex.digitaldispatcher.Dict
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.R


class BeaconScannerListItem(_scene: BeaconScannerPage, _uuid:String) {
    companion object {
        const val ACCEPT_CONNECTION = "0"
    }
    private val scene = _scene
    private val view: LinearLayout = scene.scene.layoutInflater.inflate(R.layout.scanner_page_list_item, null) as LinearLayout
    private val space = Space(scene.scene.applicationContext)
    private val TAG = "BEACON SCANNER LIST ITEM"
    val number = Beacons.makeNumberFromUUID(_uuid)
    private var pingCounter = 0
    private var offlinePingCounter = 0
    var online = false
    var action = _uuid.slice(0..1)
    var uuid = _uuid
    init {
        Main.log("loh $number ${Dict.signalsLangs[action]}", TAG)
        view.tag = number

        scene.listContainer.addView(view)
        space.minimumHeight = 10
        scene.listContainer.addView(space)

        setOnClick(R.id.accept_driver_connection) {
            var uuid = number.length.let {Integer.toHexString(it).uppercase()}
            uuid += Beacons.makeCodeFromNumber(number)
            uuid += BeaconScannerPage.fieldId
            uuid += ACCEPT_CONNECTION
            uuid = Beacons.completeRawUUID(uuid)

            Main.log("uuid ${uuid}", TAG)
            Beacons.createBeacon(uuid)
            vis(R.id.accept_driver_connection, false)
        }
        vis(R.id.accept_driver_connection, false)
        ping(_uuid)
    }

    @SuppressLint("SetTextI18n")
    fun updateView() {
        view.findViewById<TextView>(R.id.driver_number).text = "$number $action ${Dict.signalsLangs[action.toString()]}"
        view.findViewById<TextView>(R.id.ping_count).text = "$pingCounter $uuid"
        view.findViewById<TextView>(R.id.offline_ping_count).text = "$offlinePingCounter - ${Beacons.makeNumberFromUUID(uuid)}"

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
        if(_uuid != null)
            uuid = _uuid.toString()

        pingCounter++
        if(_uuid == null) {
            if(offlinePingCounter > 5) online = false
            offlinePingCounter ++
        } else {
            online = true
            offlinePingCounter = 0
        }
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