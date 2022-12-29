package ru.prodimex.digitaldispatcher.driver

import android.media.RingtoneManager
import android.net.Uri
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import org.altbeacon.beacon.Beacon
import ru.prodimex.digitaldispatcher.*
import ru.prodimex.digitaldispatcher.uitools.PopupManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

open class DriverAppController: AppController() {
    companion object {
        val currentCarNumber:String get() = getSelectedCar()
        val numberCode:String get() = Beacons.makeCodeFromNumber(getSelectedCar())// = Beacons.makeCodeFromNumber(currentCarNumber)
        var loaderFinded = false
        var onLoading = false
        var preloadingShowed = false
        var pendingRequests:HashMap<String, HashMap<String, String>> = hashMapOf()
        fun getSelectedCar():String {
            return (UserData.cars[0])["number"].toString()
        }

        var pendingTimer:Timer? = null

        fun pendingRequestsRun() {
            if(pendingRequests.size > 0 && pendingTimer == null) {
                pendingTimer = Timer()
                pendingTimer!!.scheduleAtFixedRate(object : TimerTask() { override fun run() {
                    if(pendingRequests.size > 0) {
                        var req:HashMap<String, String> = pendingRequests.values.first()
                        HTTPRequest("trips/logs-new",
                            _args = hashMapOf(
                                "id" to req["tripId"].toString(),
                                "status" to req["status"].toString(),
                                "loggingTime" to req["date"].toString()),
                            _callback = fun(_resp: HashMap<String, Any>) {
                                Main.log("-1-1-1-1--1-1-1-1-1-1-1-1-1-1-1-1-1-1-1-1")
                                Main.log("$_resp")
                                if(_resp["result"]  == "error") {
                                    //createPendingRequest(UserData.tripId, "loaded", date)
                                } else {
                                    pendingRequests.remove(req["tripId"])
                                    Main.setParam("driverPendingRequests", Gson().toJson(
                                        pendingRequests
                                    ))
                                    if(pendingRequests.size == 0) {
                                        pendingTimer!!.cancel()
                                        pendingTimer = null
                                    }
                                }
                                DriverTripPage.toLoaderConnected = false
                                DriverTripPage.toLoaderConnectionStarted = false
                            }).execute()
                    } else {
                        pendingTimer!!.cancel()
                        pendingTimer = null
                    }
                }}, 0, 3000)
            }
        }
    }

    val commandsWithShortcuts = hashMapOf(
        Dict.IM_WAITING_FOR_LOADER_SIGNAL to true,
        Dict.IM_DISMISSED_BUT_ON_FIELD to true,
        Dict.SEND_DRIVER_INFO_TO_LOADER to true,
        Dict.IM_ON_LOADING to true,
        Dict.RECONNECT_TO_LOADER_AS_DISMISSED to true,
    )

    var infoView:LinearLayout? = null
    var actionsView:LinearLayout? = null
    var currentTripState = 0

    override fun init(_layoutId: Int) {
        super.init(_layoutId)
        Beacons.controller = this

        setOnClick(R.id.profile_button) {
            switchTopage(Dict.PROFILE_PAGE)
        }

        setOnClick(R.id.trip_button) {
            switchTopage(Dict.TRIP_PAGE)
        }

        setOnClick(R.id.settings_button) {
            switchTopage(Dict.DRIVER_SETTINGS_PAGE)
        }
    }

    override fun scanObserver(beacons: Collection<Beacon>) {
        super.scanObserver(beacons)
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2.toString() + it.id3.toString()}") != 0)
                return

            var uuid = it.id1.toString().replace("-", "", true)
            //if(uuid.indexOf(numberCode) == 2) {
            if(uuid.indexOf(UserData.makeShortCut(UserData.dq_id)) == 2) {
                if(DriverTripPage.currentRangingState == Dict.CONNECT_TO_LOADER_SIGNAL
                    || DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER
                    || DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER_AS_DISMISSED
                    || DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE
                ) {
                    Main.log("myCarNumber $currentCarNumber $uuid $numberCode")
                    when (uuid.slice(0..1)) {
                        Dict.I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL -> {
                            var ix = 2 + currentCarNumber.length * 2
                            //DriverTripPage.myShortCut = uuid.slice(ix..ix + 3)
                            waitForSignal()
                        }
                        Dict.GIVE_ME_DRIVER_INFO -> {
                            var ix = 2 + currentCarNumber.length * 2
                            //DriverTripPage.myShortCut = uuid.slice(ix..ix + 3)
                            DriverTripPage.currentRangingState = Dict.SEND_DRIVER_INFO_TO_LOADER

                            var uuidHead = DriverTripPage.currentRangingState + UserData.makeShortCut(UserData.dq_id)



                            //uuidHead += DriverTripPage.myShortCut

                            var fio = "${UserData.surname}|${UserData.name}|${UserData.patronymic}".uppercase()
                            var fioCode = ""
                            fio.forEach { fioCode += Dict.farmIndexHexByChar[it.toString()] }
                            Main.log("Generater driver data")
                            var maxInfoLength = 32 - uuidHead.length - 2
                            var totalChunks = Math.ceil((fioCode.length.toFloat()/maxInfoLength.toFloat()).toDouble()).toInt()
                            Main.log("$fio $fioCode ${fioCode.length} ${maxInfoLength} ${fioCode.length.toFloat() / maxInfoLength.toFloat()} $totalChunks")
                            Beacons.killAllBeacons()
                            for(i in 0 until totalChunks) {
                                var newUUID = "$uuidHead$i${totalChunks - 1}"
                                if (i * maxInfoLength + maxInfoLength > fioCode.length) {
                                    Main.log(fioCode.slice(i * maxInfoLength..fioCode.length - 1))
                                    newUUID +=fioCode.slice(i * maxInfoLength..fioCode.length - 1)
                                } else {
                                    Main.log(fioCode.slice(i * maxInfoLength..i * maxInfoLength + maxInfoLength - 1))
                                    newUUID += fioCode.slice(i * maxInfoLength..i * maxInfoLength + maxInfoLength - 1)
                                }
                                newUUID = Beacons.completeRawUUID(newUUID)

                                Main.log(newUUID)
                                Beacons.createBeacon(newUUID)
                            }
                        }
                    }
                }
                if(DriverTripPage.currentRangingState == Dict.SEND_DRIVER_INFO_TO_LOADER) {
                    when (uuid.slice(0..1)) {
                        Dict.STOP_SENDING_DATA_AND_WAIT -> {
                            waitForSignal()
                        }
                    }
                }
            }
            Main.log("scanObserver ${uuid.indexOf(UserData.makeShortCut(UserData.dq_id))}")

            if((uuid.indexOf(UserData.makeShortCut(UserData.dq_id)) == 2 || uuid.indexOf(
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                ) == numberCode.length + 2)
                && commandsWithShortcuts.contains(DriverTripPage.currentRangingState)) {
                Main.log("Получен сигнал с шорткатом $uuid")
                when (uuid.slice(0..1)) {
                    Dict.DISMISS_FROM_QUEUE -> {
                        youDismissed()
                    }
                    Dict.YOU_NEED_TO_RECONNECT -> {
                        startReconnectionToLoader()
                    }
                    Dict.GO_TO_LOADING -> {
                        goToLoading()
                    }
                    Dict.GO_RETURN_TO_QUEUE -> {
                        returnToQueue()
                    }
                    Dict.YOU_LOADED_GO_TO_FACTORY -> {
                        setStateToLoadedAndDisconnect()
                    }
                }
            }

            if(uuid.indexOf(Dict.LOADER_ON_FIELD_ON_AIR) == 0 && !loaderFinded && UserData.currentTripStatus == 1 && DriverTripPage.currentRangingState == "") {
                loaderFinded = true
                PopupManager.showYesNoDialog(
                    "Обнаружен погрузчик, желаете подключиться?",
                    "Внимание"
                ) {
                    Main.main.toastMe("ПОДКЛЮЧАЕМСЯ К ПОГРУЗЧИКУ")
                    DriverTripPage.toLoaderConnectionStarted = true
                    DriverTripPage.toLoaderConnected = false
                    DriverTripPage.currentRangingState = Dict.CONNECT_TO_LOADER_SIGNAL
                    loaderFinded = false
                    showToLoaderConnectionActions()

                    var uuid = DriverTripPage.currentRangingState + UserData.makeShortCut(UserData.dq_id) +
                        currentCarNumber.length.let { Integer.toHexString(it).uppercase() } + numberCode

                    Beacons.createBeacon(Beacons.completeRawUUID(uuid))
                }
            }
        }
    }

    fun youDismissed() {
        if(DriverTripPage.currentRangingState == Dict.IM_DISMISSED_BUT_ON_FIELD)
            return

        DriverTripPage.toLoaderConnected = false
        DriverTripPage.toLoaderConnectionStarted = false
        playAlertSoundAnd("Погрузчик отклонил вашу погрузку.")

        DriverTripPage.currentRangingState = Dict.IM_DISMISSED_BUT_ON_FIELD
        Beacons.killAllBeacons()
        Beacons.createBeacon(
            Beacons.completeRawUUID(
                "${DriverTripPage.currentRangingState}${
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                }"
            )
        )

        setText(R.id.trip_page_trip_status,"СТАТУС: <b>ОТКАЗАНО В ПОГРУЗКЕ</b>")
        infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext,
            R.color.button_background_red
        ))
        showAssignedStateActions()
    }

    fun returnToQueue() {
        if(DriverTripPage.currentRangingState == Dict.IM_WAITING_FOR_LOADER_SIGNAL)
            return

        DriverTripPage.currentRangingState = Dict.IM_WAITING_FOR_LOADER_SIGNAL

        Beacons.killAllBeacons()
        Beacons.createBeacon(
            Beacons.completeRawUUID(
                "${DriverTripPage.currentRangingState}${
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                }"
            )
        )
        playAlertSoundAnd("Погрузчик вернул вас в очередь.")

        onLoading = false
        showAssignedStateActions()
    }

    fun setStateToLoadedAndDisconnect() {
        if(DriverTripPage.currentRangingState == Dict.IM_LOADED_AND_GO_TO_FACTORY)
            return

        DriverTripPage.currentRangingState = Dict.IM_LOADED_AND_GO_TO_FACTORY
        Beacons.killAllBeacons()
        Beacons.createBeacon(
            Beacons.completeRawUUID(
                "${DriverTripPage.currentRangingState}${
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                }"
            )
        )
        playAlertSoundAnd("Вы погружены, отправляйтесь на завод.")

        var d = Date()
        var timeZone = SimpleDateFormat("z", Locale("en")).format(d).replace(":", "")
        var date = SimpleDateFormat("EEE MMM d yyy HH:mm:ss", Locale("en")).format(d) + " $timeZone"
        HTTPRequest("trips/logs-new",
            _args = hashMapOf(
                "id" to UserData.tripId,
                "status" to "loaded",
                "loggingTime" to date),
            _callback = fun(_resp: HashMap<String, Any>) {
                if(_resp["result"]  == "error") {
                    createPendingRequest(UserData.tripId, "loaded", date)
                }
                DriverTripPage.toLoaderConnected = false
                DriverTripPage.toLoaderConnectionStarted = false
            }).execute()
        onLoading = false
        showAssignedStateActions()
    }

    open fun createPendingRequest(_tripId:String, _status:String, _date:String) {
        Main.main.toastMe("createPendingRequest $_tripId, $_status, $_date")
        pendingRequests[_tripId] = hashMapOf("tripId" to _tripId, "status" to _status, "date" to _date)
        Main.setParam("driverPendingRequests", Gson().toJson(pendingRequests))
        pendingRequestsRun()
    }

    fun goToLoading() {
        if(DriverTripPage.currentRangingState == Dict.IM_ON_LOADING)
            return

        DriverTripPage.currentRangingState = Dict.IM_ON_LOADING
        Beacons.killAllBeacons()
        Beacons.createBeacon(
            Beacons.completeRawUUID(
                "${DriverTripPage.currentRangingState}${
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                }"
            )
        )
        playAlertSoundAnd("Погрузчик вызывает вас для погрузки.")
        /*HTTPRequest("trips/logs-new",
            _args = hashMapOf("id" to UserData.tripId, "status" to "loaded", "loggingTime" to "Mon Oct 31 2022 08:38:22 GMT+0300"),
            _callback = fun(_resp: HashMap<String, Any>) {
                Main.log(_resp)
            }).execute()*/
        onLoading = true
        showAssignedStateActions()
    }

    fun startReconnectionToLoader() {
        if(DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER
            || DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE
            || DriverTripPage.currentRangingState == Dict.RECONNECT_TO_LOADER_AS_DISMISSED
        )
            return

        Main.main.toastMe("ПОГРУЗЧИК ЗАПРОСИЛ ОБНОВИТЬ ДАННЫЕ")
        DriverTripPage.toLoaderConnectionStarted = true
        DriverTripPage.toLoaderConnected = false

        if(DriverTripPage.currentRangingState == Dict.IM_DISMISSED_BUT_ON_FIELD) {
            DriverTripPage.currentRangingState = Dict.RECONNECT_TO_LOADER_AS_DISMISSED
        } else {
            DriverTripPage.currentRangingState = if(onLoading) Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE else Dict.RECONNECT_TO_LOADER
        }

        showToLoaderConnectionActions()
        Main.log(UserData.makeShortCut(UserData.dq_id))
        Main.log(UserData.dq_id)
        var uuid = DriverTripPage.currentRangingState + UserData.makeShortCut(UserData.dq_id) +
                currentCarNumber.length.let {Integer.toHexString(it).uppercase()} +
                Beacons.makeCodeFromNumber(currentCarNumber)

        uuid = Beacons.completeRawUUID(uuid)

        Beacons.createBeacon(uuid)
    }

    fun playAlertSoundAnd(_msg:String) {
        PopupManager.showAlert(_msg)
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(Main.main.applicationContext, notification)
        Main.main.toastMe(_msg)
        r.play()
    }

    fun waitForSignal() {
        Beacons.killAllBeacons()
        DriverTripPage.currentRangingState = Dict.IM_WAITING_FOR_LOADER_SIGNAL
        Beacons.createBeacon(
            Beacons.completeRawUUID(
                "${DriverTripPage.currentRangingState}${
                    UserData.makeShortCut(
                        UserData.dq_id
                    )
                }"
            )
        )
        DriverTripPage.toLoaderConnected = true
        DriverTripPage.toLoaderConnectionStarted = false
        currentTripState = 0
        //startSheduler()

        stopPreloading()
        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }
    }

    open fun showToLoaderConnectionActions() {

    }

    open fun showAssignedStateActions() {

    }

    open fun startPreloading() {
        preloadingShowed = true
    }
    open fun stopPreloading() {
        preloadingShowed = false
    }
}