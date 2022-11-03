package ru.prodimex.digitaldispatcher

import android.widget.TextView

import org.altbeacon.beacon.*

class LoaderPageController:AppController() {
    companion object {
        val fieldId = "AAAAAAAA"
        val drivers:MutableMap<String, LoaderPageDriverView> = mutableMapOf()
    }

    fun addNewDriver(_number:String, _uuid:String) {
        if (!drivers.contains(_number)) {
            var driver = LoaderPageDriverView(this, _uuid)
            Main.log(driver.number)
            drivers[driver.number] = driver
        }
    }

    override fun scanObserver(beacons:Collection<Beacon>) {
        //Main.log("Ranged: ${beacons.count()} beacons")
        var i = 0
        var pinged = mutableMapOf<String, Boolean>()
        for (beacon: Beacon in beacons) {
            var uuid = beacon.id1.toString()
            if (uuid.length == 36 && uuid.indexOf(fieldId, 0, true) == 0) {
                val number = LoaderPageDriverView.getNumberFromUUID(uuid)
                //Main.log("$beacon about ${beacon.distance} meters away")
                if(!drivers.contains(number)) {
                    addNewDriver(number, uuid)
                } else {
                    drivers[number]!!.ping(uuid)
                }
                pinged[number] = true
            }
            i ++
        }
        //Main.log("Not pinged ${drivers.size}")
        drivers.forEach { if(!pinged.contains(it.value.number)) { it.value.ping() }}
        updateView()
    }
    init {
        //if (beacon?.uuid && beacon.uuid != undefined && beacon.uuid.indexOf(fieldId) == 0)
        showLayout(R.layout.loader_page)
        listContainer = scene.findViewById(R.id.lp_drivers_container)

        Beacons.init()

        scene.setOnClick(R.id.start_scan) {
            scanStarted = true
            updateView()
            Beacons.startScan()
        }
        scene.setOnClick(R.id.stop_scan) {
            scanStarted = false
            updateView()
            Beacons.stopScan()
        }
        scene.setOnClick(R.id.clear_list) {
            drivers.forEach { it.value.removedFromList() }
            drivers.clear()
            updateView()
        }
        updateView()
    }

    override fun updateView() {
        if(scanStarted) {
            vis(R.id.start_scan, false)
            vis(R.id.stop_scan)
        } else {
            vis(R.id.start_scan)
            vis(R.id.stop_scan, false)
        }
        var online = 0
        drivers.forEach{
            if(it.value.online)
                online++
        }
        scene.findViewById<TextView>(R.id.online_offline_drivers_counter).text = "$online/${drivers.size}"
    }
}