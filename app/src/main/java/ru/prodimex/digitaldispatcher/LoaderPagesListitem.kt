package ru.prodimex.digitaldispatcher

import android.content.Loader
import android.os.Build
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap

class LoaderPagesListitem(_number:String) {
    var PAGE_ID = Main.LOADER_QUEUE_PAGE
    val number = _number
    var driverState = Dict.NEW_ITEM
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
        space.minimumHeight = 12
        updateView()
        LoaderAppController.driversOnFieldByShortCut[shortCut] = this

        view.findViewById<Button>(R.id.disconnect_button).setOnClickListener {
            if(PAGE_ID == Main.LOADER_LOADED_PAGE) {
                PopupManager.showYesNoDialog("Вернуть АМ <b>$number</b> в очередь?", "") {
                    driverState = Dict.GO_RETURN_TO_QUEUE
                    startBeacon()
                    startPreloading()
                }
            }
            if(PAGE_ID == Main.LOADER_QUEUE_PAGE) {
                PopupManager.showYesNoDialog("Отказать АМ <b>$number</b> в погрузке?", "") {
                    driverState = Dict.DISMISS_FROM_QUEUE
                    startBeacon()
                    startPreloading()
                }
            }
        }

        view.findViewById<Button>(R.id.connect_button).setOnClickListener {
            if(PAGE_ID == Main.LOADER_LOADED_PAGE) {
                PopupManager.showYesNoDialog("Перенести АМ <b>$number</b> в статус загружен?", "") {
                    setImInQueueAndWait(Main.LOADER_CANCELLED_PAGE, "Погрузка успешно завершена")

                    driverState = Dict.YOU_LOADED_GO_TO_FACTORY
                    startBeacon()
                    startPreloading()
                }
            }
            if(PAGE_ID == Main.LOADER_QUEUE_PAGE) {
                PopupManager.showYesNoDialog("Отправить АМ <b>$number</b> на погрузку?", "") {
                    driverState = Dict.GO_TO_LOADING
                    startBeacon()
                    startPreloading()
                }
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
        if(uuid.indexOf(Dict.SEND_DRIVER_INFO_TO_LOADER) == 0
            && driverState == Dict.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO) {
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
                        fio += Dict.farmIndexCharByHex[hex]
                }
                fio = fio.lowercase()
                Main.log(fio)

                val fioArray:List<String> = fio.split("|")
                surname = "${fioArray[0].slice(0..0).uppercase()}${fioArray[0].slice(1 until fioArray[0].length)}"
                name = "${fioArray[1].slice(0..0).uppercase()}${fioArray[1].slice(1 until fioArray[1].length)}"
                patronymic = "${fioArray[2].slice(0..0).uppercase()}${fioArray[2].slice(1 until fioArray[2].length)}"
                LoaderAppController.driversInfoCache[number] = hashMapOf("surname" to surname, "name" to name, "patronymic" to patronymic) //LinkedTreeMap<String, Any>()
                Main.setParam("driversInfoCache", Gson().toJson(LoaderAppController.driversInfoCache))

                dataLine.forEach { fio += Dict.farmIndexCharByHex }
                stopPreloading()

                driverState = Dict.STOP_SENDING_DATA_AND_WAIT
                startBeacon()
            }
            Main.log("$cur $tot ${recievedData.size} $uuid")
        }

        if(uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0
            && (driverState == Dict.STOP_SENDING_DATA_AND_WAIT
                    || driverState == Dict.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL)) {
            setImInQueueAndWait(PAGE_ID, "Ожидает погрузки в очереди")
        }

        if((uuid.indexOf(Dict.CONNECT_TO_LOADER_SIGNAL) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0)
            && (driverState != Dict.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO
                    && driverState != Dict.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL)) {
            Beacons.killBeacon(currentBeacon)

            PAGE_ID = if(uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0) Main.LOADER_LOADED_PAGE
                else if(uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0) Main.LOADER_CANCELLED_PAGE
                else Main.LOADER_QUEUE_PAGE

            Main.log("СОЗДАЕМ ОТВЕТНЫЙ БИКОН ДЛЯ ПЕРЕДАЧИ ШОРТКАТА ВОДИТЕЛЮ")

            if(LoaderAppController.driversInfoCache.contains(number)) {
                driverState = Dict.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL
            } else {
                driverState = Dict.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO
            }

            startBeacon()
            startPreloading()
        }

        if(uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0 && driverState == Dict.GO_RETURN_TO_QUEUE) {
            setImInQueueAndWait(Main.LOADER_QUEUE_PAGE, "Ожидает погрузки в очереди")
        }

        if(uuid.indexOf(Dict.IM_ON_LOADING) == 0 && driverState == Dict.GO_TO_LOADING) {
            setImInQueueAndWait(Main.LOADER_LOADED_PAGE, "На загрузке")
        }

        if(uuid.indexOf(Dict.IM_DISMISSED_BUT_ON_FIELD) == 0 && driverState == Dict.DISMISS_FROM_QUEUE) {
            setImInQueueAndWait(Main.LOADER_CANCELLED_PAGE, "Погрузка запрещена")

            LoaderAppController.driversOnField.remove(number)
            LoaderAppController.driversOnArchive[number] = this
        }

        Main.log("=============================================")
        Main.log(uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY))
        Main.log(driverState)

        if(uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY) == 0 && driverState == Dict.YOU_LOADED_GO_TO_FACTORY) {
            setImInQueueAndWait(Main.LOADER_CANCELLED_PAGE, "Погрузка успешно завершена")
            PAGE_ID = Main.LOADER_CANCELLED_PAGE
            Beacons.killBeacon(currentBeacon)
        }
    }

    fun setImInQueueAndWait(_newPageId:String, _newDriverStatus:String) {
        PAGE_ID = _newPageId
        Beacons.killBeacon(currentBeacon)
        driverState = Dict.SILENCE
        driverStatus = _newDriverStatus
        stopPreloading()
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
        setText(R.id.loader_car_number, "<b>$shortCut: $number</b>")
        setText(R.id.loader_block_driver_name, "<b>$surname $name $patronymic</b>")

        setText(R.id.loader_queue_driver_fio, "$driverStatus")

        if(PAGE_ID == Main.LOADER_LOADED_PAGE) {
            view.findViewById<Button>(R.id.disconnect_button).text = "В ОЧЕРЕДЬ"
            view.findViewById<Button>(R.id.connect_button).text = "ПОГРУЖЕН"

            view.findViewById<Button>(R.id.disconnect_button).visibility = View.VISIBLE
            view.findViewById<Button>(R.id.connect_button).visibility = View.VISIBLE
        } else if(PAGE_ID == Main.LOADER_QUEUE_PAGE) {
            view.findViewById<Button>(R.id.disconnect_button).text = "ОТКАЗАТЬ"
            view.findViewById<Button>(R.id.connect_button).text = "ЗАГРУЗИТЬ"

            view.findViewById<Button>(R.id.disconnect_button).visibility = View.VISIBLE
            view.findViewById<Button>(R.id.connect_button).visibility = View.VISIBLE
        } else {
            view.findViewById<Button>(R.id.disconnect_button).visibility = View.GONE
            view.findViewById<Button>(R.id.connect_button).visibility = View.GONE
        }
    }

    fun setText(_id:Int, _text:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text)
        }
    }
}