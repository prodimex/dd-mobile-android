package ru.prodimex.digitaldispatcher

import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class DriverTripPage:DriverAppController() {
    companion object {
        var pingCounter = 0
        var currentRangingState = ""
        //var myShortCut = ""
        var toLoaderConnected = false
        var toLoaderConnectionStarted = false
    }

    var timer = Timer()
    var takeNewTripStarted = false

    init {
        init(R.layout.driver_trip_page)

        highlightIcon(R.id.trip_ico, R.color.text_yellow)
        highlightText(R.id.trip_text, R.color.text_yellow)

        setText(R.id.trip_page_short_name, UserData.shortName)
        setText(R.id.trip_page_cars_numbers, "${UserData.carsNumbers}")
        startPreloading()

        if(UserData.tripData != null) {
            processPingResponse(UserData.tripData!!)
        } else {
            setText(R.id.trip_page_header,"Загрузка...</b>")
        }

        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() { override fun run() { pingTrip() }}, 0, 3000)
        pingTrip()
    }

    fun pingTrip() {
        pingCounter++
        Main.log("pingCounter $pingCounter")
        HTTPRequest("users/trip", _requestMethod = "GET", _callback = fun (_response:HashMap<String, Any>) {
            Main.log(" - ping - ")
            Main.log(_response)
            if(_response["result"] == "error") {
                if(_response["responseCode"] == "401") {
                    Main.setParam("driverLoggedOuted", "")
                    switchTopage(Main.DRIVER_LOGIN_PAGE)
                    PopupManager.showAlert("Авторизация утеряна, возможно имел место вход с другого устройства.")
                    showErrorByCode(_response)
                    return
                }
                showErrorByCode(_response)
                if(UserData.tripData != null) {
                    processPingResponse(UserData.tripData!!)
                }
                return
            }
            hideError()
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
            //timer.cancel()

            setOnClick(R.id.go_to_line_button) {
                takeNewTrip()
            }
            return
        }
    }

    fun takeNewTrip() {
        if(takeNewTripStarted) return

        if(UserData.cars.size == 0) {
            PopupManager.showAlert("Невозможно взять рейс, к вашему профилю не прикреплён автомобиль, обратитесь к диспетчеру.", "Автомобиль не назначен")
            return
        }

        //currentTripState = 0
        onLoading = false
        toLoaderConnected = false
        toLoaderConnectionStarted = false
        currentRangingState = ""
        loaderFinded = false

        takeNewTripStarted = true
        startPreloading()

        setText(R.id.trip_page_header,"Получаем новый рейс,\nподождите...")

        Main.log("-------------------------------")
        Main.log(UserData.cars[0])
        //var carId = (UserData.cars[0]["id"] as Double).toInt().toString()
        //val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        //val currentDate = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date())
        //timer.cancel()
        HTTPRequest("users/ready-to-trip-new",
            _args = hashMapOf("car_id" to (UserData.cars[0]["id"] as Double).toInt().toString(),
                "isNeedDelete" to "false",
                "ready_at" to SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date()) ),
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
                //startSheduler()
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
        if(currentPageId == _pageId)
            return

        killViews()
        timer.cancel()
        super.switchTopage(_pageId)
    }

    fun showUpdateTripInfo() {
        if(UserData.currentTripStatus > 2)
            Beacons.killAllBeacons()

        if(currentTripState == 0 || currentTripState != UserData.currentTripStatus) {
            var status = UserData.currentTripStatus
            if(pendingRequests.contains(UserData.tripId)) {
                status = 2
                loaderFinded = true
            }
            when(status) {
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
            if(takeNewTripStarted) return
            stopPreloading()
        }
    }

    override fun createPendingRequest(_tripId: String, _status: String, _date: String) {
        super.createPendingRequest(_tripId, _status, _date)
        setText(R.id.trip_page_trip_status,"СТАТУС: <b>ЗАГРУЖЕН</b>")
    }

    fun showUpdateTripInfoBlock() {
        takeNewTripStarted = false
        if(infoView == null) {
            infoView = scene.layoutInflater.inflate(R.layout.trip_info_block, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_info_container).addView(infoView)
        }

        setText(R.id.trip_page_header,"РЕЙС: <b>${ UserData.dq_id}</b>")

        var status = if(pendingRequests.contains(UserData.tripId)) 2 else UserData.currentTripStatus

        setText(R.id.trip_page_trip_status,"СТАТУС: <b>${UserData.tripStatuses[status]!!["name"]!!.uppercase()}</b>")
        infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext, Dict.statusColors[status]!!))

        var loadDate = "<b>${UserData.currentTrip!!["loading_time_from"]}</b>-<b>${UserData.currentTrip!!["loading_time_to"]}</b>"
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        var date = formatter.parse("${UserData.currentTrip!!["loading_date"]}") as Date

        loadDate += " ${Dict.daysOfWeek[date.day]}., ${date.date} ${Dict.monts[date.month]}"

        var unloadDate = "<b>${UserData.currentTrip!!["unloading_time_from"]}</b>-<b>${UserData.currentTrip!!["unloading_time_to"]}</b>"
        date = formatter.parse("${UserData.currentTrip!!["unloading_date"]}") as Date
        unloadDate += " ${Dict.daysOfWeek[date.day]}., ${date.date} ${Dict.monts[date.month]}"

        setText(R.id.loading_field, "Поле: <b>${UserData.toNode(UserData.currentTrip!!["loading_cargo_station"])["name"]}</b>")
        setText(R.id.unloading_field, "<b>${UserData.toNode(UserData.currentTrip!!["unloading_cargo_station"])["name"]}</b>")
        setText(R.id.loading_date, loadDate)
        setText(R.id.unloading_date, unloadDate)
        setText(R.id.trip_page_base_farm_name,"<b>${UserData.base_farm_name}</b>")
    }
    fun showUpdateActions() {
        if(currentTripState == 0 || currentTripState != UserData.currentTripStatus) {
            var status = UserData.currentTripStatus
            if(pendingRequests.contains(UserData.tripId))
                status = 2

            when(status) {
                1 -> {
                    showAssignedStateActions()
                }
                5, 7 -> {
                    actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_ready_or_not, null) as LinearLayout
                    scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)

                    setOnClick(R.id.take_new_trip_button) {
                        startPreloading()
                        //timer.cancel()
                        HTTPRequest("trips/confirm-finish-new",
                             _args = hashMapOf("tripId" to UserData.tripId, "ready_to_trip" to "true"),
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
                        //timer.cancel()
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

    override fun showAssignedStateActions() {
        if(toLoaderConnected) {
            if(onLoading)
                setText(R.id.trip_page_trip_status,"СТАТУС: <b>НА ПОГРУЗКЕ</b>")
            else
                setText(R.id.trip_page_trip_status,"СТАТУС: <b>В ОЧЕРЕДИ</b>")

            infoView!!.findViewById<TextView>(R.id.trip_page_trip_status).setTextColor(ContextCompat.getColor(scene.applicationContext, Dict.statusColors[2]!!))
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

        if(currentRangingState == Dict.IM_DISMISSED_BUT_ON_FIELD) {
            actionsView = scene.layoutInflater.inflate(R.layout.trip_actions_block_2, null) as LinearLayout
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)
            setOnClick(R.id.disconnect_from_loader_button) {
                currentRangingState = ""
                loaderFinded = true
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
        //timer.cancel()
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

    override fun showToLoaderConnectionActions() {
        if(actionsView != null) {
            scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).removeView(actionsView)
            actionsView = null
        }
        startPreloading()

        actionsView = scene.layoutInflater.inflate(R.layout.trip_to_loader_connection, null) as LinearLayout
        scene.findViewById<LinearLayout>(R.id.trip_page_actions_container).addView(actionsView)

        setOnClick(R.id.connect_to_loader_cancel_button) {
            toLoaderConnectionStarted = false
            Beacons.killAllBeacons()
            stopPreloading()
            showAssignedStateActions()
        }

        //timer.cancel()
    }

    fun startConectionToLoader() {
        toLoaderConnectionStarted = true
        toLoaderConnected = false
        currentRangingState = Dict.CONNECT_TO_LOADER_SIGNAL
        showToLoaderConnectionActions()

        var dq_id = Integer.toHexString(UserData.dq_id.toInt())
        dq_id = "${Integer.toHexString(dq_id.length)}$dq_id"

        var uuid = currentRangingState +
            currentCarNumber.length.let {Integer.toHexString(it).uppercase()} +
                Beacons.makeCodeFromNumber(currentCarNumber) + dq_id

        uuid = Beacons.completeRawUUID(uuid)

        Main.log(" ======================= ++++ $uuid ${UserData.dq_id} $dq_id")

        var tripId = Beacons.getTripIdFromUUID(uuid)
        Beacons.createBeacon(uuid)
    }

    override fun startPreloading() {
        if(preloadingShowed) return
        super.startPreloading()

        Main.log("===== startPreloading")

        if(actionsView != null) actionsView!!.visibility = View.GONE

        scene.findViewById<ImageView>(R.id.trip_page_preloading).visibility = View.VISIBLE
        val r = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 750
        r.repeatCount = Animation.INFINITE
        r.interpolator = LinearInterpolator()

        scene.findViewById<ImageView>(R.id.trip_page_preloading).startAnimation(r)
    }
    override fun stopPreloading() {
        if(!preloadingShowed) return
        super.stopPreloading()

        Main.log("----- stopPreloading")
        scene.findViewById<ImageView>(R.id.trip_page_preloading).clearAnimation()
        scene.findViewById<ImageView>(R.id.trip_page_preloading).visibility = View.GONE
    }
}