package ru.prodimex.digitaldispatcher

import android.annotation.SuppressLint
import java.lang.Long.parseLong
import android.widget.TextView
import org.altbeacon.beacon.Beacon

class DriverPageController:AppController() {
    companion object {
        val drivers:MutableMap<String, DriverBeacon> = mutableMapOf()
    }

    init {
        showLayout(R.layout.main)
        listContainer = scene.findViewById(R.id.drivers_container)

        Beacons.init()
        Beacons.startScan()

        scene.setOnClick(R.id.add_driver_button) {
            if (drivers.size == DriverBeacon.driversData.size-2)
                return@setOnClick

            val driver = DriverBeacon(this)
            drivers[driver.number] = driver
            updateView()
        }
        updateView()
    }

    override fun scanObserver(beacons:Collection<Beacon>) {
        beacons.forEach{
            var uuid = it.id1.toString()
            uuid = uuid.replace("-", "", true)

            var number = LoaderPageDriverView.getNumberFromNumberCode(uuid.slice(1..parseLong(uuid[0].toString(), 16).toInt() * 2))
            Main.log("scanObserver: $uuid $number")
            if(drivers.contains(number)) {
                Main.log("scanObserver: $uuid $number")
                drivers[number]!!.checkIncomingSignal(uuid.slice(parseLong(uuid[0].toString(), 16).toInt() * 2+1..uuid.length-1))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun updateView() {
        var totalTransmitters = 0
        drivers.forEach { totalTransmitters += it.value.myTransmitters.size }
        (scene.findViewById<TextView>(R.id.top_panel_info)).text = "Водителей: ${drivers.size}/${DriverBeacon.driversData.size-2}\nТрансмиттеров: $totalTransmitters"
    }
}