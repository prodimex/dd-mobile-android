package ru.prodimex.digitaldispatcher.loader

import android.media.RingtoneManager
import android.net.Uri
import android.widget.TextView
import com.google.gson.Gson
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconTransmitter
import ru.prodimex.digitaldispatcher.*
import ru.prodimex.digitaldispatcher.driver.DriverAppController
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class LoaderAppController: AppController() {
    companion object {
        var driversOnField:MutableMap<String, LoaderQueueListItem> = hashMapOf()
        var driversOnFieldByShortCut:MutableMap<String, LoaderQueueListItem> = hashMapOf()

        var driversOnArchive:MutableMap<String, LoaderArchiveListItem> = hashMapOf()

        var reconnectShortcuts:MutableMap<String, BeaconTransmitter> = hashMapOf()

        var driversInfoCache = HashMap<String, Any>()

        fun dropDrivers() {
            driversOnField.clear()
            driversOnFieldByShortCut.clear()
            reconnectShortcuts.clear()
        }

        fun playAlertSoundAnd(_msg:String) {
            Main.main.toastMe(_msg)
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(Main.main.applicationContext, notification)
            r.play()
        }

        var driversPings:MutableMap<String, Boolean> = hashMapOf()

        fun placeToArchive(_number:String, _shortCut:String) {
            driversOnField.remove(_number)
            driversOnFieldByShortCut.remove(_shortCut)
            driversPings.remove(_shortCut)

            var time = Date().time.toString()
            driversOnArchive["$_shortCut$time"] = LoaderArchiveListItem(_number, _shortCut, time)

            var archiveToSave:ArrayList<HashMap<String, String>> = arrayListOf()
            driversOnArchive.forEach {
                archiveToSave.add(hashMapOf("number" to it.value.number, "shortCut" to it.value.shortCut, "time" to it.value.time))
            }
            Main.setParam("loaderArchive", Gson().toJson(archiveToSave))
        }
    }

    override val TAG = "LOADER APP CONTROLLER"
    override fun init(_layoutId:Int) {
        super.init(_layoutId)
        listContainer = scene.findViewById(R.id.loader_list_container)

        setOnClick(R.id.queue_button) {
            switchTopage(Dict.LOADER_QUEUE_PAGE)
        }
        setOnClick(R.id.loaded_button) {
            switchTopage(Dict.LOADER_LOADED_PAGE)
        }
        setOnClick(R.id.cancelled_button) {
            switchTopage(Dict.LOADER_CANCELLED_PAGE)
        }
        setOnClick(R.id.settings_button) {
            switchTopage(Dict.LOADER_SETTINGS_PAGE)
        }
    }

    override fun switchTopage(_pageId: String) {
        if(currentPageId == _pageId)
            return

        Beacons.controller = null
        driversOnField.forEach {
            if(it.value.view.parent != null) {
                listContainer.removeView(it.value.view)
                listContainer.removeView(it.value.space)
            }
        }
        driversOnArchive.forEach {
            if(it.value.view.parent != null) {
                listContainer.removeView(it.value.view)
                listContainer.removeView(it.value.space)
            }
        }

        super.switchTopage(_pageId)
    }

    fun checkDriverAvailability(_uuid:String) {
        var number = Beacons.makeNumberFromUUID(_uuid)
        var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2.._uuid.length - 1))
        Main.log("checkDriverAvailability $_uuid $number $shortCut ${driversOnField.contains(number)}", TAG)
        if(!driversOnField.contains(number)) {
            driversOnField[number] = LoaderQueueListItem(number, shortCut)
            if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0)
                driversOnField[number]!!.PAGE_ID = Dict.LOADER_LOADED_PAGE

            if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0)
                driversOnField[number]!!.PAGE_ID = Dict.LOADER_CANCELLED_PAGE

            Main.log("Обнаружен новый водитель и добавлен: $_uuid")
            playAlertSoundAnd("Обнаружен новый водитель.")
        }
        driversOnField[number]!!.receiveUIIDs(_uuid)
    }

    fun processTheSignal(_uuid:String) {
        Main.log("processTheSignal $_uuid - ${_uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL)}", TAG)

        //При обнаружении водителя с просьбой о подключении заводим его карточку и читаем сигнал
        if(_uuid.indexOf(Dict.CONNECT_TO_LOADER_SIGNAL) == 0) {
            checkDriverAvailability(_uuid)
        }

        //Если водитель получил сигнал переприсоединения он присылает соответствующий сигнал
        if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER) == 0
            || _uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0
            || _uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0) {
            var shortCut = UserData.makeShortCut(Beacons.getTripIdFromUUID(_uuid))
            Main.log("Если водитель получил сигнал ${reconnectShortcuts.contains(shortCut)} ${shortCut}", TAG)
            if(reconnectShortcuts.contains(shortCut)) {
                reconnectShortcuts.remove(shortCut)
                Beacons.killBeacon(Beacons.completeRawUUID("${Dict.YOU_NEED_TO_RECONNECT}$shortCut"))
            }
            checkDriverAvailability(_uuid)
        }

        //При получении сигналов содержащих только шорткат проверяем на отсутствие карточки
        // и если её нет то запрашиваем данные
        if(_uuid.indexOf(Dict.SEND_DRIVER_INFO_TO_LOADER) == 0
            || _uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0
            || _uuid.indexOf(Dict.IM_DISMISSED_BUT_ON_FIELD) == 0
            || _uuid.indexOf(Dict.IM_ON_LOADING) == 0) {

            var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2.._uuid.length - 1))
            Main.log("Only shorcutted signal $shortCut ${driversOnFieldByShortCut.contains(shortCut)}", TAG)
            if (driversOnFieldByShortCut.contains(shortCut)) {
                driversOnFieldByShortCut[shortCut]!!.receiveUIIDs(_uuid)
                driversPings[shortCut] = true
            } else {
                if(!reconnectShortcuts.contains(shortCut))
                    makeReconnectBeacon(shortCut)
            }
        }
        if(_uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY) == 0) {
            var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2.._uuid.length - 1))
            Beacons.killBeacon(Beacons.completeRawUUID("${Dict.YOU_LOADED_GO_TO_FACTORY}$shortCut"))
        }

    }

    fun makeReconnectBeacon(_shortcut:String) {
        var d = Date().time
        var uuid = Beacons.completeRawUUID("${Dict.YOU_NEED_TO_RECONNECT}$_shortcut$d")


        Main.log("makeReconnectBeacon $uuid ++ ${_shortcut}", TAG)
        Beacons.createBeacon(uuid)
        reconnectShortcuts[_shortcut] = Beacons.beaconTransmitters[uuid]!!
    }

    override fun scanObserver(beacons:Collection<Beacon>) {
        super.scanObserver(beacons)
        driversPings.clear()
        driversOnFieldByShortCut.forEach {
            driversPings[it.value.shortCut] = false
        }
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2.toString() + it.id3.toString()}") == 0)
                processTheSignal(it.id1.toString())
        }
        driversPings.forEach {
            if(!it.value) {
                Main.log(it.key, TAG)
                driversOnFieldByShortCut[it.key]!!.ping()
            }
        }
        updateView()
    }

    var dc = HashMap<String, Int>() // счетчики водителей
    private fun processDriverOnPage(driver: ListItem) {
        driver.updateView()
        if(!dc.contains(driver.PAGE_ID))
            dc[driver.PAGE_ID] = 0

        dc[driver.PAGE_ID] = dc[driver.PAGE_ID]!! + 1

        if(driver.view.parent != listContainer && driver.PAGE_ID == currentPageId) {
            listContainer.addView(driver.view)
            listContainer.addView(driver.space)
        }
        if(driver.view.parent == listContainer && driver.PAGE_ID != currentPageId) {
            listContainer.removeView(driver.view)
            listContainer.removeView(driver.space)
        }
    }
    override fun updateView() {
        super.updateView()
        dc = HashMap()
        driversOnField.forEach {
            processDriverOnPage(it.value)
        }

        driversOnArchive.forEach {
            processDriverOnPage(it.value)
        }

        scene.findViewById<TextView>(R.id.queue_counter_text).text =
            if(dc.contains(Dict.LOADER_QUEUE_PAGE)) dc[Dict.LOADER_QUEUE_PAGE]!!.toString() else "0"

        scene.findViewById<TextView>(R.id.loaded_counter_text).text =
            if(dc.contains(Dict.LOADER_LOADED_PAGE)) dc[Dict.LOADER_LOADED_PAGE]!!.toString() else "0"

        scene.findViewById<TextView>(R.id.canceled_counter_text).text =
            if(dc.contains(Dict.LOADER_CANCELLED_PAGE)) dc[Dict.LOADER_CANCELLED_PAGE]!!.toString() else "0"
    }
}