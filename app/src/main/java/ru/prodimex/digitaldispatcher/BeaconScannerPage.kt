package ru.prodimex.digitaldispatcher
import org.altbeacon.beacon.*
import android.widget.TextView
class BeaconScannerPage:AppController() {
    companion object {
        val fieldId = "МДА"
        val drivers:MutableMap<String, BeaconScannerListItem> = mutableMapOf()
    }
    init {
        showLayout(R.layout.scanner_page_layout)
        listContainer = scene.findViewById(R.id.lp_drivers_container)

        UserData.tripFieldindex = fieldId
        Beacons.init()
        Beacons.controller = this


        scene.setOnClick(R.id.start_scan) {
            scanStarted = true
            updateView()
            Beacons.startScan(Beacons.completeRawUUID(Dict.SCANNER_ON_FIELD_ON_AIR))
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

    override fun scanObserver(beacons:Collection<Beacon>) {
        var pinged = mutableMapOf<String, Boolean>()
        //Main.log(beacons.size)
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2}${it.id3}") != 0)
                return
            var uuid = it.id1.toString()
            Main.log("$uuid ${it.id2}${it.id3}")
            if(!drivers.contains(uuid)) {
                drivers[uuid] = BeaconScannerListItem(this, uuid)
            } else {
                drivers[uuid]!!.ping(uuid)
                pinged[uuid] = true
            }
        }
        drivers.forEach { if(!pinged.contains(it.value.uuid)) { it.value.ping() }}
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
        scene.findViewById<TextView>(R.id.online_offline_drivers_counter).text = "$online/${drivers.size} Поле:$fieldId / ${Beacons.beaconFarmCode}"

        var t = "FBSP: ${Beacons.beaconManager.foregroundBetweenScanPeriod} "
        t += "FSP: ${Beacons.beaconManager.foregroundScanPeriod} "
        t += "FSFF: ${Beacons.beaconManager.foregroundServiceStartFailed()} "
        t += "BGM: ${Beacons.beaconManager.backgroundMode} "
        t += "MRGS: ${Beacons.beaconManager.monitoredRegions} "

        t += "LAY: ${Beacons.beaconParser.layout} "
        t += "POWCORR: ${Beacons.beaconParser.powerCorrection} "
        t += "ID: ${Beacons.beaconParser.identifier} "

        scene.findViewById<TextView>(R.id.scanner_stats).text = t
    }
}
