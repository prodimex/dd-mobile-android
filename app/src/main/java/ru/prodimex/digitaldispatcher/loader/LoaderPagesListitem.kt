package ru.prodimex.digitaldispatcher.loader

import android.os.Build
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import ru.prodimex.digitaldispatcher.*
import ru.prodimex.digitaldispatcher.uitools.PopupManager
import java.math.BigInteger

class LoaderPagesListitem(_number:String, _shortCut:String) {
    var PAGE_ID = Dict.LOADER_QUEUE_PAGE
    val number = _number
    val tripId = BigInteger(_shortCut.slice(1.._shortCut.length-1), 16).toString()
    var driverState = Dict.NEW_ITEM
    var view: LinearLayout = Main.main.layoutInflater.inflate(R.layout.queue_driver_list_item, null) as LinearLayout
    var shortCut = _shortCut//UserData.makeShortCut(_tripId)
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
            if(PAGE_ID == Dict.LOADER_LOADED_PAGE) {
                PopupManager.showYesNoDialog("Вернуть АМ <b>$number</b> в очередь?", "") {
                    startBeacon(Dict.GO_RETURN_TO_QUEUE)
                }
            }
            if(PAGE_ID == Dict.LOADER_QUEUE_PAGE) {
                PopupManager.showYesNoDialog("Отказать АМ <b>$number</b> в погрузке?", "") {
                    startBeacon(Dict.DISMISS_FROM_QUEUE)
                }
            }
        }

        view.findViewById<Button>(R.id.connect_button).setOnClickListener {
            if(PAGE_ID == Dict.LOADER_LOADED_PAGE) {
                PopupManager.showYesNoDialog("Перенести АМ <b>$number</b> в статус загружен?", "") {
                    setImInQueueAndWait(Dict.LOADER_CANCELLED_PAGE, "Погрузка успешно завершена")
                    startBeacon(Dict.YOU_LOADED_GO_TO_FACTORY)
                }
            }
            if(PAGE_ID == Dict.LOADER_QUEUE_PAGE) {
                PopupManager.showYesNoDialog("Отправить АМ <b>$number</b> на погрузку?", "") {
                    startBeacon(Dict.GO_TO_LOADING)
                }
            }
        }
    }

    var recievedData = HashMap<String, String>()
    fun startBeacon(_newState:String) {
        driverState = _newState
        currentBeacon = Beacons.completeRawUUID(driverState + shortCut)// + Beacons.makeCodeFromNumber(number)
        Beacons.createBeacon(currentBeacon)

        startPreloading()
    }

    var pingCounter = 0
    fun ping() {
        pingCounter ++
    }
    fun receiveUIIDs(_uuid:String) {
        Main.log("receiveUIIDs $_uuid $driverState")
        pingCounter = 0
        var uuid = _uuid
        if(uuid.indexOf(Dict.SEND_DRIVER_INFO_TO_LOADER) == 0
            && driverState == Dict.GIVE_ME_DRIVER_INFO
        ) {
            Beacons.killBeacon(currentBeacon)
            driverStatus = "Передаёт данные"

            Main.log("Получаем данные от водителя")
            Main.log(uuid)
            uuid = uuid.replace("-", "", true)
            var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2..uuid.length - 1))
            Main.log("shortCut ${shortCut}")
            var scLength = BigInteger(shortCut.get(0).toString(), 16).toInt()
            Main.log("scLength ${scLength}")
            var cur = uuid.slice(scLength + 3..scLength + 3)
            var tot = uuid.slice(scLength + 4..scLength + 4)
            Main.log("$cur $tot ${recievedData.size} $uuid")

            uuid = uuid.slice(scLength + 5..uuid.length - 1)
            Main.log("uuid ${uuid}")
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

                var driverCache = LinkedTreeMap<String, Any>()
                driverCache["surname"] = surname
                driverCache["name"] = name
                driverCache["patronymic"] = patronymic
                LoaderAppController.driversInfoCache[number] = driverCache

                Main.setParam(
                    "driversInfoCache",
                    Gson().toJson(LoaderAppController.driversInfoCache)
                )

                dataLine.forEach { fio += Dict.farmIndexCharByHex }
                stopPreloading()

                startBeacon(Dict.STOP_SENDING_DATA_AND_WAIT)
                stopPreloading()
            }
            Main.log("$cur $tot ${recievedData.size} $uuid")

        }

        if(uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0
            && (driverState == Dict.STOP_SENDING_DATA_AND_WAIT
                    || driverState == Dict.I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL)) {
            setImInQueueAndWait(PAGE_ID, "Ожидает погрузки в очереди")
        }

        if((uuid.indexOf(Dict.CONNECT_TO_LOADER_SIGNAL) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0
                    || uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0)
            && (driverState != Dict.GIVE_ME_DRIVER_INFO
                    && driverState != Dict.I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL)) {
            Beacons.killBeacon(currentBeacon)

            PAGE_ID = if(uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0) Dict.LOADER_LOADED_PAGE
                else if(uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0) Dict.LOADER_CANCELLED_PAGE
                else Dict.LOADER_QUEUE_PAGE

            Main.log("СОЗДАЕМ ОТВЕТНЫЙ БИКОН ДЛЯ ПЕРЕДАЧИ ШОРТКАТА ВОДИТЕЛЮ $number")
            if(LoaderAppController.driversInfoCache.contains(number)) {
                startBeacon(Dict.I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL)
            } else {
                startBeacon(Dict.GIVE_ME_DRIVER_INFO)
            }
        }

        if(uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0 && driverState == Dict.GO_RETURN_TO_QUEUE) {
            setImInQueueAndWait(Dict.LOADER_QUEUE_PAGE, "Ожидает погрузки в очереди")
        }

        if(uuid.indexOf(Dict.IM_ON_LOADING) == 0 && driverState == Dict.GO_TO_LOADING) {
            setImInQueueAndWait(Dict.LOADER_LOADED_PAGE, "На загрузке")
        }

        if(uuid.indexOf(Dict.IM_DISMISSED_BUT_ON_FIELD) == 0 && driverState == Dict.DISMISS_FROM_QUEUE) {
            setImInQueueAndWait(Dict.LOADER_CANCELLED_PAGE, "Погрузка запрещена")

            placeMeToArchive()
        }

        Main.log("=============================================")
        Main.log(uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY))
        Main.log(driverState)

        if(uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY) == 0 && driverState == Dict.YOU_LOADED_GO_TO_FACTORY) {
            setImInQueueAndWait(Dict.LOADER_CANCELLED_PAGE, "Погрузка успешно завершена")
            placeMeToArchive()
        }
    }

    fun placeMeToArchive() {
        LoaderAppController.driversOnField.remove(number)
        LoaderAppController.driversOnFieldByShortCut.remove(shortCut)
        LoaderAppController.driversPings.remove(shortCut)

        LoaderAppController.driversOnArchive[shortCut] = this
    }

    fun setImInQueueAndWait(_newPageId:String, _newDriverStatus:String) {
        Main.log("setImInQueueAndWait --------- $currentBeacon")
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

    fun updateView() {
        setText(R.id.loader_car_number, "<b>АМ: $number Номер рейса: $tripId</b>")
        setText(R.id.loader_block_driver_name, "<b>$surname $name $patronymic</b>")

        setText(R.id.loader_queue_driver_fio, "$driverStatus")

        view.findViewById<Button>(R.id.disconnect_button).visibility = View.VISIBLE
        view.findViewById<Button>(R.id.connect_button).visibility = View.VISIBLE
        view.findViewById<RelativeLayout>(R.id.online_indicator).visibility = View.VISIBLE

        if(PAGE_ID == Dict.LOADER_LOADED_PAGE) {
            view.findViewById<Button>(R.id.disconnect_button).text = "В ОЧЕРЕДЬ"
            view.findViewById<Button>(R.id.connect_button).text = "ПОГРУЖЕН"
        } else if(PAGE_ID == Dict.LOADER_QUEUE_PAGE) {
            view.findViewById<Button>(R.id.disconnect_button).text = "ОТКАЗАТЬ"
            view.findViewById<Button>(R.id.connect_button).text = "ЗАГРУЗИТЬ"
        } else {
            view.findViewById<Button>(R.id.disconnect_button).visibility = View.GONE
            view.findViewById<Button>(R.id.connect_button).visibility = View.GONE
            view.findViewById<RelativeLayout>(R.id.online_indicator).visibility = View.GONE
        }

        view.findViewById<Button>(R.id.online_indicator_dot).isEnabled = pingCounter <= 5
        view.findViewById<TextView>(R.id.online_indicator_text).text = if (pingCounter <= 5)  "На связи $pingCounter" else "Связь отсутствует $pingCounter"
    }

    fun setText(_id:Int, _text:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            (view.findViewById<TextView>(_id)).text = Html.fromHtml(_text)
        }
    }
}