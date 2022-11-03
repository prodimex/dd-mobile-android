package ru.prodimex.digitaldispatcher

import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap

class OnFieldDriver(_number:String) {
    var PAGE_ID = Main.LOADER_QUEUE_PAGE
    val number = _number
    var driverState = Dictionary.NEW_ITEM
    var view: LinearLayout = Main.main.layoutInflater.inflate(R.layout.queue_driver_list_item, null) as LinearLayout
    var shortCut = makeShortCut()
    var currentBeacon = ""

    var surname = ""
    var name = ""
    var patronymic = ""
    var driverStatus = "Обнаружен"
    val space = Space(Main.main.applicationContext)

    init {
        if(LoaderAppController.driversInfoCache.contains(number)) {
            var cacheData = LoaderAppController.driversInfoCache[number] as LinkedTreeMap<String, Any>
            surname = cacheData["surname"].toString()
            name = cacheData["name"].toString()
            patronymic = cacheData["patronymic"].toString()
        }
        space.minimumHeight = 7
        updateView()
        LoaderAppController.driversOnFieldByShortCut[shortCut] = this

        view.findViewById<Button>(R.id.disconnect_button).setOnClickListener {
            PopupManager.showYesNoDialog("Отказать АМ <b>$number</b> в погрузке?", "") {
                driverState = Dictionary.DISMISS_FROM_QUEUE
                startBeacon()
                startPreloading()
            }
        }

        view.findViewById<Button>(R.id.connect_button).setOnClickListener {
            PopupManager.showYesNoDialog("Отправить АМ <b>$number</b> на погрузку?", "") {
                driverState = Dictionary.GO_TO_LOADING
                startBeacon()
                startPreloading()
            }
        }
    }
    var recievedData = HashMap<String, String>()
    fun startBeacon() {
        currentBeacon = driverState
        currentBeacon += Beacons.makeCodeFromNumber(number) + shortCut
        currentBeacon = Beacons.completeRawUUID(currentBeacon)
        Beacons.createBeacon(currentBeacon)
    }
    fun receiveUIIDs(_uuid:String) {
        var uuid = _uuid
        if(uuid.indexOf(Dictionary.SEND_DRIVER_INFO_TO_LOADER) == 0
            && driverState == Dictionary.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO) {
            Beacons.killBeacon(currentBeacon)
            Main.log("Получаем данные от водителя")
            driverStatus = "Передаёт данные"
            var cur = _uuid.slice(6..6)
            var tot = _uuid.slice(7..7)
            uuid = uuid.slice(8.._uuid.length - 1).replace("-", "", true)
            recievedData[cur] = uuid
            if(recievedData.size - 1 == tot.toInt()) {
                Main.log("Данные получены")
                driverStatus = "Все данные получены"
                var dataLine = ""
                for (i in 0..tot.toInt()) {
                    dataLine += recievedData["$i"]
                }
                var fio = ""
                for (i in 0 until dataLine.length/2) {
                    var hex = dataLine.slice(i * 2..i * 2 + 1)
                    if(hex != "00")
                        fio += Dictionary.farmIndexCharByHex[hex]
                }
                fio = fio.lowercase()
                Main.log(fio)

                val fioArray:List<String> = fio.split("|")
                surname = "${fioArray[0].slice(0..0).uppercase()}${fioArray[0].slice(1 until fioArray[0].length)}"
                name = "${fioArray[1].slice(0..0).uppercase()}${fioArray[1].slice(1 until fioArray[1].length)}"
                patronymic = "${fioArray[2].slice(0..0).uppercase()}${fioArray[2].slice(1 until fioArray[2].length)}"
                LoaderAppController.driversInfoCache[number] = hashMapOf("surname" to surname, "name" to name, "patronymic" to patronymic) //LinkedTreeMap<String, Any>()
                Main.setParam("driversInfoCache", Gson().toJson(LoaderAppController.driversInfoCache))

                dataLine.forEach { fio += Dictionary.farmIndexCharByHex }
                stopPreloading()

                driverState = Dictionary.STOP_SENDING_DATA_AND_WAIT
                startBeacon()
            }
            Main.log("$cur $tot ${recievedData.size} $uuid")
        }

        if(uuid.indexOf(Dictionary.IM_WAITING_FOR_LOADER_SIGNAL) == 0
            && (driverState == Dictionary.STOP_SENDING_DATA_AND_WAIT
                    || driverState == Dictionary.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL)) {
            Beacons.killBeacon(currentBeacon)
            Main.log("Водитель ожидает погрузки в очереди")
            driverState = Dictionary.SILENCE
            driverStatus = "Ожидает погрузки в очереди"
            stopPreloading()
        }

        if((uuid.indexOf(Dictionary.CONNECT_TO_LOADER_SIGNAL) == 0
                    || uuid.indexOf(Dictionary.RECONNECT_TO_LOADER) == 0)
            && (driverState != Dictionary.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO
                    && driverState != Dictionary.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL)) {

            PAGE_ID = Main.LOADER_QUEUE_PAGE
            Main.log("СОЗДАЕМ ОТВЕТНЫЙ БИКОН ДЛЯ ПЕРЕДАЧИ ШОРТКАТА ВОДИТЕЛЮ")

            if(LoaderAppController.driversInfoCache.contains(number)) {
                driverState = Dictionary.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL
            } else {
                driverState = Dictionary.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO
            }

            startBeacon()
            startPreloading()
        }

        if(uuid.indexOf(Dictionary.IM_ON_LOADING) == 0 && driverState == Dictionary.GO_TO_LOADING) {
            PAGE_ID = Main.LOADER_LOADED_PAGE

            Beacons.killBeacon(currentBeacon)
            Main.log("Водитель на загрузке")
            driverState = Dictionary.SILENCE
            driverStatus = "На загрузке"
            stopPreloading()
        }
        Main.log("=============================================")
        Main.log(uuid.indexOf(Dictionary.IM_DISMISSED_BUT_ON_FIELD))
        Main.log(driverState)
        if(uuid.indexOf(Dictionary.IM_DISMISSED_BUT_ON_FIELD) == 0 && driverState == Dictionary.DISMISS_FROM_QUEUE) {
            PAGE_ID = Main.LOADER_CANCELLED_PAGE

            Beacons.killBeacon(currentBeacon)
            Main.log("Водитель дисквалифицирован")
            driverState = Dictionary.SILENCE
            driverStatus = "Погрузка запрещена"
            stopPreloading()
        }
    }

    fun startPreloading() {
        view.findViewById<LinearLayout>(R.id.loader_driver_actions).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.loader_driver_preloader).visibility = View.VISIBLE

        val r = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 750
        r.repeatCount = Animation.INFINITE
        r.interpolator = LinearInterpolator()

        view.findViewById<ImageView>(R.id.loader_driver_preloader_image).startAnimation(r)
    }

    fun stopPreloading() {
        view.findViewById<LinearLayout>(R.id.loader_driver_actions).visibility = View.VISIBLE
        view.findViewById<LinearLayout>(R.id.loader_driver_preloader).visibility = View.GONE

        view.findViewById<ImageView>(R.id.loader_driver_preloader_image).clearAnimation()
    }

    fun makeShortCut():String {
        LoaderAppController.driverShortCutMem++
        var str = LoaderAppController.driverShortCutMem.toString()
        for (i in 0 until 4 - str.length) str = "0$str"
        Main.log(str)
        return str
    }

    fun updateView() {
        view.findViewById<TextView>(R.id.loader_queue_driver_id).text = shortCut + ": " + number
        view.findViewById<TextView>(R.id.loader_queue_driver_fio).text = "$surname $name $patronymic"
        view.findViewById<TextView>(R.id.loader_queue_driver_status).text = "$driverStatus"
    }
}