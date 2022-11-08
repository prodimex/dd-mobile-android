package ru.prodimex.digitaldispatcher

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import java.util.*


class DriverBeacon(_scene:DriverPageController) {
    companion object {
        var driverCounter = 0

        val driversData: Array<Map<String, String>> = arrayOf(
            mapOf("number" to "Р589АХ 46", "fio" to "Золотарёв Леонид Сергеевич"),
            mapOf("number" to "К780АО 136", "fio" to "Бугорский Владимир Анатольевич"),
            mapOf("number" to "Н482УА 46", "fio" to "Золотарёв Сергей Фёдорович"),
            mapOf("number" to "Е462КА 46", "fio" to "Филатов Александр Михайлович"),
            mapOf("number" to "Н678ОС 46", "fio" to "Соболев Александр Михайлович"),
            mapOf("number" to "М340ТО 46", "fio" to "Шкурко Александр Александрович"),
            mapOf("number" to "Н749ТА 46", "fio" to "Терехов Дмитрий Викторович"),
            mapOf("number" to "Н160КМ 46", "fio" to "Пряхин Максим Викторович"),
            mapOf("number" to "О177ОУ 46", "fio" to "Шумаков Николай Николаевич"),

            mapOf("number" to "1589АХ 46", "fio" to "Золотарёв Леонид Сергеевич"),
            mapOf("number" to "1780АО 136", "fio" to "Бугорский Владимир Анатольевич"),
            mapOf("number" to "1482УА 46", "fio" to "Золотарёв Сергей Фёдорович"),
            mapOf("number" to "1462КА 46", "fio" to "Филатов Александр Михайлович"),
            mapOf("number" to "1678ОС 46", "fio" to "Соболев Александр Михайлович"),
            mapOf("number" to "1340ТО 46", "fio" to "Шкурко Александр Александрович"),
            mapOf("number" to "1749ТА 46", "fio" to "Терехов Дмитрий Викторович"),
            mapOf("number" to "1160КМ 46", "fio" to "Пряхин Максим Викторович"),
            mapOf("number" to "1177ОУ 46", "fio" to "Шумаков Николай Николаевич"),

            mapOf("number" to "2589АХ 46", "fio" to "Золотарёв Леонид Сергеевич"),
            mapOf("number" to "2780АО 136", "fio" to "Бугорский Владимир Анатольевич"),
            mapOf("number" to "2482УА 46", "fio" to "Золотарёв Сергей Фёдорович"),
            mapOf("number" to "2462КА 46", "fio" to "Филатов Александр Михайлович"),
            mapOf("number" to "2678ОС 46", "fio" to "Соболев Александр Михайлович"),
            mapOf("number" to "2340ТО 46", "fio" to "Шкурко Александр Александрович"),
            mapOf("number" to "2749ТА 46", "fio" to "Терехов Дмитрий Викторович"),
            mapOf("number" to "2160КМ 46", "fio" to "Пряхин Максим Викторович"),
            mapOf("number" to "2177ОУ 46", "fio" to "Шумаков Николай Николаевич"),

            mapOf("number" to "3589АХ 46", "fio" to "Золотарёв Леонид Сергеевич"),
            mapOf("number" to "3780АО 136", "fio" to "Бугорский Владимир Анатольевич"),
            mapOf("number" to "3482УА 46", "fio" to "Золотарёв Сергей Фёдорович"),
            mapOf("number" to "3462КА 46", "fio" to "Филатов Александр Михайлович"),
            mapOf("number" to "3678ОС 46", "fio" to "Соболев Александр Михайлович"),
            mapOf("number" to "3340ТО 46", "fio" to "Шкурко Александр Александрович"),
            mapOf("number" to "3749ТА 46", "fio" to "Терехов Дмитрий Викторович"),
            mapOf("number" to "3160КМ 46", "fio" to "Пряхин Максим Викторович"),
            mapOf("number" to "3177ОУ 46", "fio" to "Шумаков Николай Николаевич"),
            mapOf("number" to "", "fio" to "")
        )
        const val CONNECT_ME = "0"
        const val SEND_INFO_ON_CONNECTION = "1"

        fun putDashToIndex(_uuid:String, _index:Int):String {
            return _uuid.replaceRange(_index, _index, "-")
        }

        fun completeRawUUID(_uuid:String):String {
            var uuid = _uuid
            for (i in 0 until 32 - uuid.length) uuid += 0

            uuid = putDashToIndex(uuid, 8)
            uuid = putDashToIndex(uuid, 13)
            uuid = putDashToIndex(uuid, 18)
            uuid = putDashToIndex(uuid, 23)
            return uuid
        }
        fun makeCodeFromNumber(_number:String):String {
            var result = ""
            for (i in 0 until _number.length)
                result += Dictionary.carNumberHexsByChar[_number[i].toString()]

            return result
        }
    }
    private val scene = _scene
    private val id = DriverPageController.drivers.size
    val tag:String = "driverCounter_".plus(driverCounter++)
    @SuppressLint("InflateParams")
    private val view:LinearLayout = scene.scene.layoutInflater.inflate(R.layout.driver_block, null) as LinearLayout
    private val space = Space(scene.scene.applicationContext)
    private var beaconEnabled = false
    val myTransmitters = arrayListOf<String>()
    val number = driversData[id]["number"]!!
    val numberCode = makeCodeFromNumber(number)
    var myCurrendSignalCode = ""
    init {
        Main.log("Created DriverBeacon: $tag")
        view.tag = tag
        scene.listContainer.addView(view)
        space.tag = tag.plus("space")
        space.minimumHeight = 10
        scene.listContainer.addView(space)

        setOnClick(R.id.disconnect_button) { stopTransmit() }
        setOnClick(R.id.connect_button) {
            beaconEnabled = true
            myCurrendSignalCode = CONNECT_ME
            var rawUUid = BeaconScannerPage.fieldId.toString()
            rawUUid += driversData[id]["number"]?.length?.let {Integer.toHexString(it).uppercase()}
            rawUUid += numberCode
            rawUUid += myCurrendSignalCode

            Main.log("rawUUid ${completeRawUUID(rawUUid)}")
            Main.log("rawUUid $rawUUid")

            createBeacon(completeRawUUID(rawUUid))
            updateView()
        }

        setOnClick(R.id.remove_driver_beacon) {
            stopTransmit()
            scene.listContainer.removeView(view)
            scene.listContainer.removeView(space)
            DriverPageController.drivers.remove(tag)
        }
        (view.findViewById(R.id.driver_id) as TextView).text = "#$id АМ номер: ${driversData[id]["number"]}"
        (view.findViewById(R.id.driver_fio) as TextView).text = "".plus(driversData[id]["fio"])
        updateView()
    }

    fun setOnClick(_btnId:Int, _callback:(()->Unit)) {
        (view.findViewById<Button>(_btnId)).setOnClickListener { _callback.invoke() }
    }

    var lastSignal = ""
    fun checkIncomingSignal(_signal:String) {
        if (lastSignal == _signal)
            return

        if(_signal.indexOf(BeaconScannerPage.fieldId, 0, true) == 0) {

            var signalTail = _signal.slice(BeaconScannerPage.fieldId.length.._signal.length-1)

            val signalCode = signalTail[0].toString()
            Main.log("checkIncommingSignal $_signal $signalTail ${myCurrendSignalCode == CONNECT_ME} ${signalCode == BeaconScannerListItem.ACCEPT_CONNECTION}")
            if (myCurrendSignalCode == CONNECT_ME && signalCode == BeaconScannerListItem.ACCEPT_CONNECTION) {
                killAllTransmitters()
                myCurrendSignalCode = SEND_INFO_ON_CONNECTION
                vis(R.id.disconnect_button, false)
                vis(R.id.connect_button, false)
                lastSignal = _signal
            }
        }
    }

    private fun createBeacon(uuid:String) {
        Beacons.beaconTransmitters[uuid] = Beacons.createBeacon(uuid)
        myTransmitters.add(uuid)
        Main.log("myTransmitters.size ${myTransmitters.size}")
        Main.log("uuid ${uuid}")
    }

    private fun killAllTransmitters() {
        myTransmitters.forEach {
            Beacons.beaconTransmitters[it]?.stopAdvertising()
            Beacons.beaconTransmitters.remove(it)
        }
        myTransmitters.clear()
    }
    private fun stopTransmit() {
        beaconEnabled = false

        killAllTransmitters()
        Main.log("$tag myTransmitters.size ${myTransmitters.size}}")

        updateView()
    }

    private fun updateView() {
        if(!beaconEnabled) {
             vis(R.id.connect_button)
             vis(R.id.disconnect_button, false)
        } else {
            vis(R.id.connect_button, false)
            vis(R.id.disconnect_button)
        }
       scene.updateView()
    }

    private fun vis(_id:Int, _show:Boolean = true) {
        (view.findViewById(_id) as View).visibility = if(_show) View.VISIBLE else View.GONE
    }
}