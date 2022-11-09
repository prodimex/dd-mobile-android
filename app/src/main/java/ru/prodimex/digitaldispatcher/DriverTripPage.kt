package ru.prodimex.digitaldispatcher

import android.media.RingtoneManager
import android.net.Uri
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.internal.LinkedTreeMap
import org.altbeacon.beacon.Beacon
import java.text.SimpleDateFormat
import java.util.*

class DriverTripPage:DriverAppController() {
    companion object {
        var pingCounter = 0
        var beaconsInited = false
        var currentRangingState = ""
        var myShortCut = ""
        var toLoaderConnected = false
        var toLoaderConnectionStarted = false
    }
    private var infoView:LinearLayout? = null
    private var actionsView:LinearLayout? = null
    var timer = Timer()
    var takeNewTripStarted = false
    var currentCarNumber = (UserData.cars[0] as LinkedTreeMap)["number"].toString()
    var currentTripState = 0

    init {
        init(R.layout.driver_trip_page)

        highlightIcon(R.id.trip_ico, R.color.text_yellow)
        highlightText(R.id.trip_text, R.color.text_yellow)

        setText(R.id.trip_page_short_name, UserData.shortName)
        setText(R.id.trip_page_cars_numbers, "${UserData.carsNumbers}")

        if(UserData.tripData != null) {
            processPingResponse(UserData.tripData!!)
        } else {
            setText(R.id.trip_page_header,"Загрузка...</b>")
            startPreloading()
        }

        startSheduler()
    }

    fun startSheduler() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() { override fun run() { pingTrip() }}, 0, 1000)
        pingTrip()
    }

    fun pingTrip() {
        pingCounter++
        HTTPRequest("users/trip", _requestMethod = "GET", _callback = fun (_response:HashMap<String, Any>) {
            if(_response["result"] == "error") {
                showErrorByCode(_response)
                if(UserData.tripData != null) {
                    processPingResponse(UserData.tripData!!)
                }
                return
            }
            hideError()
            Main.log(" - ping - ")
            processPingResponse(_response)
        }).execute()
    }

    fun processPingResponse(_response:HashMap<String, Any>) {
        UserData.collectTripData(_response)

        if(pageKilled)
            return

        if(currentTripState == 0 || currentTripState != UserData.currentTripStatus) {
            killViews()
        }
        showUpdateTripInfo()
        showUpdateActions()
        currentTripState = UserData.currentTripStatus
        if(UserData.currentTrip == null && !takeNewTripStarted) {
            killViews()
            setText(R.id.trip_page_header,"РЕЙС НЕ НАЗНАЧЕН</b>")
            actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_trip_not_set, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)
            stopPreloading()
            timer.cancel()

            setOnClick(R.id.go_to_line_button) {
                takeNewTrip()
            }
            return
        }
    }

    fun takeNewTrip() {
        if(UserData.cars.size == 0)
            return
        takeNewTripStarted = true
        setText(R.id.trip_page_header,"Получаем новый рейс,\nподождите...")

        startPreloading()
        Main.log("-------------------------------")
        Main.log(UserData.cars[0])
        var carId = (UserData.cars[0]["id"] as Double).toInt().toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val currentDate = sdf.format(Date())
        timer.cancel()
        HTTPRequest("users/ready-to-trip-new",
            _args = hashMapOf("car_id" to carId, "isNeedDelete" to "false", "ready_at" to currentDate ),
            _callback = fun(_response:HashMap<String, Any>) {
                if(_response["result"] == "error") {
                    showErrorByCode(_response)
                    stopPreloading()
                    return
                }
                hideError()
                Main.log(" ================= ")
                Main.log(_response)
                Main.log(" + +++ +++++++++++ ")
                startSheduler()
            }).execute()
    }
    fun killViews() {
        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }
        if(infoView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_info_container).removeView(infoView)
            infoView = null
        }
    }

    override fun switchTopage(_pageId: String) {
        killViews()
        timer.cancel()
        super.switchTopage(_pageId)
    }

    fun showUpdateTripInfo() {
        if(currentTripState == 0 || currentTripState != UserData.currentTripStatus) {
            when(UserData.currentTripStatus) {
                1, 2, 3, 4, 6 -> {
                    showUpdateTripInfoBlock()
                }
                5 -> {
                    setText(R.id.trip_page_header,"РЕЙС <b>${ UserData.dq_id}</b><br>ЗАВЕРШЁН.<br>ВЫ ГОТОВЫ ВЗЯТЬ<br>НОВЫЙ РЕЙС?")
                }
                7 -> {
                    setText(R.id.trip_page_header,"РЕЙС <b>${ UserData.dq_id}</b><br>ОТМЕНЁН.<br>ВЫ ГОТОВЫ ВЗЯТЬ<br>НОВЫЙ РЕЙС?")
                }
            }
            stopPreloading()
        }
    }

    fun showUpdateTripInfoBlock() {
        takeNewTripStarted = false
        if(infoView == null) {
            infoView = scene.layoutInflater.inflate(R.layout.trip_info_block, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_info_container).addView(infoView)
        }

        setText(R.id.trip_page_header,"РЕЙС: <b>${ UserData.dq_id}</b>")
        setText(R.id.trip_page_trip_status,"СТАТУС: <b>${UserData.tripStatuses[UserData.currentTripStatus]!!["name"]!!.uppercase()}</b>")
        infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext, Dictionary.statusColors[UserData.currentTripStatus]!!))

        var loadDate = "<b>${UserData.currentTrip!!["loading_time_from"]}</b>-<b>${UserData.currentTrip!!["loading_time_to"]}</b>"
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        var date = formatter.parse("${UserData.currentTrip!!["loading_date"]}") as Date


        loadDate += " ${Dictionary.daysOfWeek[date.day]}., ${date.date} ${Dictionary.monts[date.month]}"

        var unloadDate = "<b>${UserData.currentTrip!!["unloading_time_from"]}</b>-<b>${UserData.currentTrip!!["unloading_time_to"]}</b>"
        date = formatter.parse("${UserData.currentTrip!!["unloading_date"]}") as Date
        unloadDate += " ${Dictionary.daysOfWeek[date.day]}., ${date.date} ${Dictionary.monts[date.month]}"

        setText(R.id.loading_field, "Поле: <b>${UserData.toNode(UserData.currentTrip!!["loading_cargo_station"])["name"]}</b>")
        setText(R.id.unloading_field, "<b>${UserData.toNode(UserData.currentTrip!!["unloading_cargo_station"])["name"]}</b>")
        setText(R.id.loading_date, loadDate)
        setText(R.id.unloading_date, unloadDate)
        setText(R.id.trip_page_base_farm_name,"<b>${UserData.base_farm_name}</b>")
    }
    fun showUpdateActions() {
        if(currentTripState == 0 || currentTripState != UserData.currentTripStatus) {
            when(UserData.currentTripStatus) {
                1 -> {
                    showAssignedStateActions()
                }
                5, 7 -> {
                    actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_ready_or_not, null) as LinearLayout
                    scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)

                    setOnClick(R.id.take_new_trip_button) {
                        startPreloading()
                        timer.cancel()
                        HTTPRequest("trips/confirm-finish-new",
                            _args = hashMapOf("tripId" to UserData.tripId),
                            _callback = fun(_response:HashMap<String, Any>) {
                                if(_response["result"] == "error") {
                                    showErrorByCode(_response)
                                    stopPreloading()
                                    return
                                }
                                hideError()
                                takeNewTrip()
                            }).execute()
                    }
                    setOnClick(R.id.not_ready_button) {
                        startPreloading()
                        timer.cancel()
                        HTTPRequest("trips/confirm-finish-new",
                            _args = hashMapOf("tripId" to UserData.tripId),
                            _callback = fun(_response:HashMap<String, Any>) {
                                if(_response["result"] == "error") {
                                    showErrorByCode(_response)
                                    stopPreloading()
                                    return
                                }
                                hideError()
                                pingTrip()
                            }).execute()
                    }
                }
            }
        }
    }

    fun showAssignedStateActions() {
        if(toLoaderConnected) {
            setText(R.id.trip_page_trip_status,"СТАТУС: <b>В ОЧЕРЕДИ</b>")
            infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext, Dictionary.statusColors[2]!!))
            return
        }

        if(toLoaderConnectionStarted) {
            showToLoaderConnectionActions()
            return
        }

        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }

        if(currentRangingState == Dictionary.IM_DISMISSED_BUT_ON_FIELD) {
            actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_block_2, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)
            setOnClick(R.id.disconnect_from_loader_button) {
                currentRangingState = ""
                Beacons.killAllBeacons()
                showAssignedStateActions()
            }
        } else {
            actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_block_1, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)
            setOnClick(R.id.connect_to_loader_button) { startConectionToLoader() }
        }

        setOnClick(R.id.cancel_trip_button) { cancelTrip() }
    }

    fun cancelTrip() {
        startPreloading()
        timer.cancel()
        HTTPRequest("trips/${UserData.tripId}/cancel", _requestMethod = "GET", _callback = fun(_response:HashMap<String, Any>) {
            if(_response["result"] == "error") {
                showErrorByCode(_response)
                stopPreloading()
                return
            }
            hideError()
            pingTrip()
        }).execute()
    }

    fun showToLoaderConnectionActions() {
        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }
        startPreloading()

        actionsView = scene.layoutInflater.inflate(R.layout.trip_to_loader_connection, null) as LinearLayout
        scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)

        setOnClick(R.id.connect_to_loader_cancel_button) {
            toLoaderConnectionStarted = false
            Beacons.stopScan()
            Beacons.killAllBeacons()
            stopPreloading()
            showAssignedStateActions()
        }

        timer.cancel()
    }

    fun startConectionToLoader() {
        toLoaderConnectionStarted = true
        toLoaderConnected = false
        currentRangingState = Dictionary.CONNECT_TO_LOADER_SIGNAL
        showToLoaderConnectionActions()

        var uuid = currentRangingState
        uuid += currentCarNumber.length.let {Integer.toHexString(it).uppercase()}
        uuid += Beacons.makeCodeFromNumber(currentCarNumber)
        uuid = Beacons.completeRawUUID(uuid)

        Main.log(" ======================= ++++ $uuid")
        Beacons.createBeacon(uuid)
        Beacons.startScan()
    }

    val commandsWithShortcuts = hashMapOf(Dictionary.IM_WAITING_FOR_LOADER_SIGNAL to true,
        Dictionary.IM_DISMISSED_BUT_ON_FIELD to true,
        Dictionary.SEND_DRIVER_INFO_TO_LOADER to true,
        Dictionary.IM_ON_LOADING to true,
    )

    override fun scanObserver(beacons: Collection<Beacon>) {
        super.scanObserver(beacons)
        var numberCode = Beacons.makeCodeFromNumber(currentCarNumber)
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2.toString() + it.id3.toString()}") != 0)
                return

            var uuid = it.id1.toString().replace("-", "", true)
            if(uuid.indexOf(numberCode) == 2) {
                if(currentRangingState == Dictionary.CONNECT_TO_LOADER_SIGNAL || currentRangingState == Dictionary.RECONNECT_TO_LOADER) {
                    Main.log("myCarNumber $currentCarNumber $uuid $numberCode")
                    when (uuid.slice(0..1)) {
                        Dictionary.GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL -> {
                            var ix = 2 + currentCarNumber.length * 2
                            myShortCut = uuid.slice(ix..ix + 3)
                            waitForSignal()
                        }
                        Dictionary.GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO -> {
                            var ix = 2 + currentCarNumber.length * 2
                            myShortCut = uuid.slice(ix..ix + 3)
                            currentRangingState = Dictionary.SEND_DRIVER_INFO_TO_LOADER

                            var uuidHead = currentRangingState
                            uuidHead += myShortCut

                            var fio = "${UserData.surname}|${UserData.name}|${UserData.patronymic}".uppercase()
                            var fioCode = ""
                            fio.forEach { fioCode += Dictionary.farmIndexHexByChar[it.toString()] }
                            Main.log("Generater driver data")
                            var maxInfoLength = 32 - uuidHead.length - 2
                            var totalChunks = Math.ceil((fioCode.length.toFloat()/maxInfoLength.toFloat()).toDouble()).toInt()
                            Main.log("$fio $fioCode ${fioCode.length} ${maxInfoLength} ${fioCode.length.toFloat()/maxInfoLength.toFloat()} $totalChunks")
                            Beacons.killAllBeacons()
                            for(i in 0 until totalChunks) {
                                var newUUID = "$uuidHead$i${totalChunks - 1}"
                                if (i * maxInfoLength + maxInfoLength > fioCode.length) {
                                    Main.log(fioCode.slice(i * maxInfoLength..fioCode.length-1))
                                    newUUID +=fioCode.slice(i * maxInfoLength..fioCode.length - 1)
                                } else {
                                    Main.log(fioCode.slice(i * maxInfoLength..i * maxInfoLength + maxInfoLength-1))
                                    newUUID += fioCode.slice(i * maxInfoLength..i * maxInfoLength + maxInfoLength - 1)
                                }
                                newUUID = Beacons.completeRawUUID(newUUID)

                                Main.log(newUUID)
                                Beacons.createBeacon(newUUID)
                            }
                        }
                    }
                }
                if(currentRangingState == Dictionary.SEND_DRIVER_INFO_TO_LOADER) {
                    when (uuid.slice(0..1)) {
                        Dictionary.STOP_SENDING_DATA_AND_WAIT -> {
                            waitForSignal()
                        }
                    }
                }
            }
            Main.log(uuid.indexOf(myShortCut))

            //if(uuid.indexOf(myShortCut) == 2 || uuid.indexOf(myShortCut) == 18  || uuid.indexOf(myShortCut) == 20) {
            Main.log("${uuid.indexOf(myShortCut)} ${numberCode.length+2}")
            if(uuid.indexOf(myShortCut) == 2 || uuid.indexOf(myShortCut) == numberCode.length+2) {
                if(commandsWithShortcuts.contains(currentRangingState)) {
                    Main.log("Получен сигнал с шорткатом")
                    Main.log(uuid)
                    when (uuid.slice(0..1)) {
                        Dictionary.DISMISS_FROM_QUEUE -> {
                            youDismissed()
                        }
                        Dictionary.YOU_NEED_TO_RECONNECT -> {
                            startReconnectionToLoader()
                        }
                        Dictionary.GO_TO_LOADING -> {
                            goToLoading()
                        }
                    }
                }
            }
        }
    }

    fun youDismissed() {
        if(currentRangingState == Dictionary.IM_DISMISSED_BUT_ON_FIELD)
            return

        toLoaderConnected = false
        toLoaderConnectionStarted = false
        playAlertSoundAnd("Погрузчик отклонил вашу погрузку.")

        currentRangingState = Dictionary.IM_DISMISSED_BUT_ON_FIELD
        Beacons.killAllBeacons()
        Beacons.createBeacon(Beacons.completeRawUUID("$currentRangingState$myShortCut"))

        setText(R.id.trip_page_trip_status,"СТАТУС: <b>ОТКАЗАНО В ПОГРУЗКЕ</b>")
        infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext, R.color.button_background_red))
        showAssignedStateActions()
    }

    fun goToLoading() {
        if(currentRangingState == Dictionary.IM_ON_LOADING)
            return

        currentRangingState = Dictionary.IM_ON_LOADING
        Beacons.killAllBeacons()
        Beacons.createBeacon(Beacons.completeRawUUID("$currentRangingState$myShortCut"))
        playAlertSoundAnd("Погрузчик вызывает вас для погрузки.")
        HTTPRequest("trips/logs-new",
            _args = hashMapOf("id" to UserData.tripId, "status" to "loaded", "loggingTime" to "Mon Oct 31 2022 08:38:22 GMT+0300"),
            _callback = fun(_response:HashMap<String, Any>) {
                if(_response["result"] == "error") {
                    showErrorByCode(_response)
                    return
                }
                hideError()
                Main.log(_response)
            }).execute()
    }

    fun startReconnectionToLoader() {
        if(currentRangingState == Dictionary.RECONNECT_TO_LOADER)
            return

        Main.main.toastMe("ПОГРУЗЧИК ЗАПРОСИЛ ОБНОВИТЬ ДАННЫЕ")
        toLoaderConnectionStarted = true
        toLoaderConnected = false
        currentRangingState = Dictionary.RECONNECT_TO_LOADER
        showToLoaderConnectionActions()

        var uuid = currentRangingState
        uuid += myShortCut + currentCarNumber.length.let {Integer.toHexString(it).uppercase()}
        uuid += Beacons.makeCodeFromNumber(currentCarNumber)
        uuid = Beacons.completeRawUUID(uuid)

        Beacons.createBeacon(uuid)
    }



    fun playAlertSoundAnd(_msg:String) {
        PopupManager.showAlert(_msg)
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(Main.main.applicationContext, notification)
        r.play()
    }

    fun waitForSignal() {
        Beacons.killAllBeacons()
        currentRangingState = Dictionary.IM_WAITING_FOR_LOADER_SIGNAL
        Beacons.createBeacon(Beacons.completeRawUUID("$currentRangingState$myShortCut"))
        toLoaderConnected = true
        toLoaderConnectionStarted = false
        currentTripState = 0
        startSheduler()

        stopPreloading()
        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }
    }
    fun startPreloading() {
        Main.log("===== startPreloading")
        if(actionsView != null)
            actionsView!!.visibility = View.GONE
        scene.findViewById<ImageView>(R.id.trip_page_preloading).visibility = View.VISIBLE
        val r = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 750
        r.repeatCount = Animation.INFINITE
        r.interpolator = LinearInterpolator()

        scene.findViewById<ImageView>(R.id.trip_page_preloading).startAnimation(r)
    }
    fun stopPreloading() {
        Main.log("----- stopPreloading")
        scene.findViewById<ImageView>(R.id.trip_page_preloading).clearAnimation()
        scene.findViewById<ImageView>(R.id.trip_page_preloading).visibility = View.GONE
    }
}