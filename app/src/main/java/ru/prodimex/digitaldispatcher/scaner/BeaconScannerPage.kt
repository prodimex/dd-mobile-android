package ru.prodimex.digitaldispatcher.scaner
import android.annotation.SuppressLint
import org.altbeacon.beacon.*
import android.widget.TextView
import ru.prodimex.digitaldispatcher.*
import ru.prodimex.digitaldispatcher.R

class BeaconScannerPage: AppController() {
    companion object {
        val fieldId = "МДА"
        val drivers:MutableMap<String, BeaconScannerListItem> = mutableMapOf()
    }
    override val TAG = "BEACON SCANNER PAGE"
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
        beacons.forEach {
            if(Beacons.beaconFarmCode.indexOf("${it.id2}${it.id3}") != 0)
                return
            var uuid = it.id1.toString()
            Main.log("Beacon observed: $uuid ${it.id2}${it.id3}", TAG)
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

    @SuppressLint("SetTextI18n")
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

        scene.findViewById<TextView>(R.id.scanner_stats).text = "FBSP: ${Beacons.beaconManager.foregroundBetweenScanPeriod} " +
                "FSP: ${Beacons.beaconManager.foregroundScanPeriod} " +
                "FSFF: ${Beacons.beaconManager.foregroundServiceStartFailed()} " +
                "BGM: ${Beacons.beaconManager.backgroundMode} " +
                "MRGS: ${Beacons.beaconManager.monitoredRegions} "
    }
}
