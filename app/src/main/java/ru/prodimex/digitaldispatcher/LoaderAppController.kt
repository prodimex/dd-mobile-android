package ru.prodimex.digitaldispatcher

import android.media.RingtoneManager
import android.net.Uri
import android.widget.TextView
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconTransmitter

open class LoaderAppController:AppController() {
    companion object {
        var driverShortCutMem = 0

        var driversOnField:MutableMap<String, LoaderPagesListitem> = hashMapOf()
        var driversOnFieldByShortCut:MutableMap<String, LoaderPagesListitem> = hashMapOf()
        var reconnectShortcuts:MutableMap<String, BeaconTransmitter> = hashMapOf()

        var driversInfoCache = HashMap<String, Any>()

        fun dropDrivers() {
            driversOnField.clear()
            driversOnFieldByShortCut.clear()
            reconnectShortcuts.clear()
        }

        fun checkDriverAvailability(_uuid:String) {
            var number = Beacons.makeNumberFromUUID(_uuid)
            if(!driversOnField.contains(number)) {
                driversOnField[number] = LoaderPagesListitem(number)
                if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0)
                    driversOnField[number]!!.PAGE_ID = Main.LOADER_LOADED_PAGE
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
            Main.log("makeReconnectBeacon $uuid")
            reconnectShortcuts[_shortcut] = Beacons.createBeacon(uuid)
        }

        fun processTheSignal(_uuid:String) {
            Main.log("processTheSignal $_uuid - ${_uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL)}")

            //При обнаружении водителя с просьбой о подключении заводим его карточку и читаем сигнал
            if(_uuid.indexOf(Dict.CONNECT_TO_LOADER_SIGNAL) == 0) {
                checkDriverAvailability(_uuid)
            }

            //Если водитель получил сигнал переприсоединения он присылает соответствующий сигнал
            if(_uuid.indexOf(Dict.RECONNECT_TO_LOADER) == 0
                || _uuid.indexOf(Dict.RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE) == 0) {
                var shortCut = _uuid.slice(2..5)
                if(reconnectShortcuts.contains(shortCut)) {
                    reconnectShortcuts.remove(shortCut)
                    Beacons.killBeacon(Beacons.completeRawUUID("${Dict.YOU_NEED_TO_RECONNECT}$shortCut"))
                }
                checkDriverAvailability(_uuid.slice(0..1) + _uuid.slice(6.._uuid.length-1))
            }

            //При получении сигналов содержащих только шорткат проверяем на отсутствие карточки
            // и если её нет то запрашиваем данные
            if(_uuid.indexOf(Dict.SEND_DRIVER_INFO_TO_LOADER) == 0
                || _uuid.indexOf(Dict.IM_WAITING_FOR_LOADER_SIGNAL) == 0
                || _uuid.indexOf(Dict.IM_DISMISSED_BUT_ON_FIELD) == 0
                || _uuid.indexOf(Dict.IM_ON_LOADING) == 0) {
                var shortCut = _uuid.slice(2..5)
                Main.log("Only shorcutted signal $shortCut ${driversOnFieldByShortCut.contains(shortCut)}")
                if (driversOnFieldByShortCut.contains(shortCut)) {
                    driversOnFieldByShortCut[shortCut]!!.receiveUIIDs(_uuid)
                } else {
                    if(!reconnectShortcuts.contains(shortCut))
                        makeReconnectBeacon(shortCut)
                }
            }
        }
    }

    override fun init(_layoutId:Int) {
        super.init(_layoutId)
        listContainer = scene.findViewById(R.id.loader_list_container)

        setOnClick(R.id.queue_button) {
            switchTopage(Main.LOADER_QUEUE_PAGE)
        }
        setOnClick(R.id.loaded_button) {
            switchTopage(Main.LOADER_LOADED_PAGE)
        }
        setOnClick(R.id.cancelled_button) {
            switchTopage(Main.LOADER_CANCELLED_PAGE)
        }
        setOnClick(R.id.settings_button) {
            switchTopage(Main.LOADER_SETTINGS_PAGE)
        }
    }

    override fun switchTopage(_pageId: String) {
        Beacons.controller = null
        driversOnField.forEach {
            if(it.value.view.parent != null) {
                listContainer.removeView(it.value.view)
                listContainer.removeView(it.value.space)
            }
        }

        super.switchTopage(_pageId)
    }

    override fun scanObserver(beacons: Collection<Beacon>) {
        super.scanObserver(beacons)
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2.toString() + it.id3.toString()}") == 0)
                processTheSignal(it.id1.toString())
        }
        updateView()
    }

    var dc = HashMap<String, Int>() // счетчики водителей
    override fun updateView() {
        super.updateView()
        dc = HashMap()
        driversOnField.forEach {
            it.value.updateView()
            if(!dc.contains(it.value.PAGE_ID))
                dc[it.value.PAGE_ID] = 0

            dc[it.value.PAGE_ID] = dc[it.value.PAGE_ID]!! + 1

            if(it.value.view.parent != listContainer && it.value.PAGE_ID == currentPageId) {
                listContainer.addView(it.value.view)
                listContainer.addView(it.value.space)
            }
            if(it.value.view.parent == listContainer && it.value.PAGE_ID != currentPageId) {
                listContainer.removeView(it.value.view)
                listContainer.removeView(it.value.space)
            }
        }

        scene.findViewById<TextView>(R.id.queue_counter_text).text =
            if(dc.contains(Main.LOADER_QUEUE_PAGE)) dc[Main.LOADER_QUEUE_PAGE]!!.toString() else "0"

        scene.findViewById<TextView>(R.id.loaded_counter_text).text =
            if(dc.contains(Main.LOADER_LOADED_PAGE)) dc[Main.LOADER_LOADED_PAGE]!!.toString() else "0"

        scene.findViewById<TextView>(R.id.canceled_counter_text).text =
            if(dc.contains(Main.LOADER_CANCELLED_PAGE)) dc[Main.LOADER_CANCELLED_PAGE]!!.toString() else "0"
    }
}