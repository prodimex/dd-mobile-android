package ru.prodimex.digitaldispatcher.loader

import android.media.RingtoneManager
import android.net.Uri
import android.widget.TextView
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconTransmitter
import ru.prodimex.digitaldispatcher.*
import java.math.BigInteger

open class LoaderAppController: AppController() {
    companion object {
        var driverShortCutMem = 0

        var driversOnField:MutableMap<String, LoaderPagesListitem> = hashMapOf()
        var driversOnArchive:MutableMap<String, LoaderPagesListitem> = hashMapOf()

        var driversOnFieldByShortCut:MutableMap<String, LoaderPagesListitem> = hashMapOf()
        var reconnectShortcuts:MutableMap<String, BeaconTransmitter> = hashMapOf()

        var driversInfoCache = HashMap<String, Any>()

        fun dropDrivers() {
            driversOnField.clear()
            driversOnFieldByShortCut.clear()
            reconnectShortcuts.clear()
        }

        fun checkDriverAvailability(_uuid:String) {
            Main.log("checkDriverAvailability $_uuid")
            var number = Beacons.makeNumberFromUUID(_uuid)
            var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2.._uuid.length - 1))
            Main.log("checkDriverAvailability $number $shortCut ${driversOnField.contains(number)}")
            if(!driversOnField.contains(number)) {
                driversOnField[number] = LoaderPagesListitem(number, shortCut)
                if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0)
                    driversOnField[number]!!.PAGE_ID = Dict.LOADER_LOADED_PAGE

                if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0)
                    driversOnField[number]!!.PAGE_ID = Dict.LOADER_CANCELLED_PAGE

                Main.log("Обнаружен новый водитель и добавлен: $_uuid")
                playAlertSoundAnd("Обнаружен новый водитель.")
            }
            driversOnField[number]!!.receiveUIIDs(_uuid)
        }

        fun playAlertSoundAnd(_msg:String) {
            Main.main.toastMe(_msg)
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(Main.main.applicationContext, notification)
            r.play()
        }

        fun makeReconnectBeacon(_shortcut:String) {
            var uuid = Beacons.completeRawUUID("${Dict.YOU_NEED_TO_RECONNECT}$_shortcut")
            Main.log("makeReconnectBeacon $uuid ++ ${_shortcut}")
            Beacons.createBeacon(uuid)
            reconnectShortcuts[_shortcut] = Beacons.beaconTransmitters[uuid]!!

        }

        var driversPings:MutableMap<String, Boolean> = hashMapOf()
        fun processTheSignal(_uuid:String) {
            Main.log("processTheSignal $_uuid - ${_uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL)}")

            //При обнаружении водителя с просьбой о подключении заводим его карточку и читаем сигнал
            if(_uuid.indexOf(Dict.CONNECT_TO_LOADER_SIGNAL) == 0) {
                checkDriverAvailability(_uuid)
            }

            //Если водитель получил сигнал переприсоединения он присылает соответствующий сигнал
            if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER) == 0
                || _uuid.indexOf(Dict.RECONNECT_TO_LOADER_AS_DISMISSED) == 0
                || _uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0) {
                var shortCut = UserData.makeShortCut(Beacons.getTripIdFromUUID(_uuid))
                Main.log("Если водитель получил сигнал ${reconnectShortcuts.contains(shortCut)} ${shortCut}")
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
                || _uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY) == 0
                || _uuid.indexOf(Dict.IM_ON_LOADING) == 0) {

                var shortCut = UserData.getShortCutFromUUIDTail(_uuid.slice(2.._uuid.length - 1))//getShortCutFromUUID(_uuid)
                Main.log("Only shorcutted signal $shortCut ${driversOnFieldByShortCut.contains(shortCut)}")
                if (driversOnFieldByShortCut.contains(shortCut)) {
                    driversOnFieldByShortCut[shortCut]!!.receiveUIIDs(_uuid)
                    driversPings[shortCut] = true
                } else if (driversOnArchive.contains(shortCut)) {
                    driversOnArchive[shortCut]!!.receiveUIIDs(_uuid)
                } else {
                    if(!reconnectShortcuts.contains(shortCut))
                         makeReconnectBeacon(shortCut)
                }
            }
        }

        /*private fun getShortCutFromUUID(_uuid: String):String {
            Main.log("getShortCutFromUUID $_uuid")
            var shortCut = _uuid.slice(2..2) +
                    _uuid.replace("-", "", true)
                        .slice(3..BigInteger(_uuid.slice(2..2), 16).toInt() + 2)
            Main.log("getShortCutFromUUID shortCut $shortCut")
            return shortCut
        }*/
    }

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
                Main.log(it.key)
                driversOnFieldByShortCut[it.key]!!.ping()
            }
        }
        updateView()
    }

    var dc = HashMap<String, Int>() // счетчики водителей
    private fun processDriverOnPage(driver: LoaderPagesListitem) {
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