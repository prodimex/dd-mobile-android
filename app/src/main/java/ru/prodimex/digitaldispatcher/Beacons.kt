package ru.prodimex.digitaldispatcher

import android.Manifest
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.pm.PackageManager
import android.os.Build
import org.altbeacon.beacon.*


class Beacons {
    companion object {
        lateinit var beaconManager:BeaconManager
        var immortalBeacon:BeaconTransmitter? = null
        var controller:AppController? = null

        val beaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")

        val region = Region("all-beacons", null, null, null)
        val beaconTransmitters: MutableMap<String, BeaconTransmitter> = mutableMapOf()
        var beaconFarmCode:String = "1234512345"

        var initialized = false
        var scanStarted = false
        fun init() {
            if(initialized)
                return

            initialized = true

            beaconManager = BeaconManager.getInstanceForApplication(Main.main)
            beaconManager.beaconParsers.clear()
            beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))

            beaconManager.getRegionViewModel(region).rangedBeacons.observe(Main.main) { beacons ->
                if(controller != null)
                    controller!!.scanObserver(beacons)
            }
            beaconManager.getRegionViewModel(region).regionState.observe(Main.main) { state ->
                if (state == MonitorNotifier.INSIDE) {
                    Main.log("Detected beacons(s)")
                } else {
                    Main.log("Stopped detecteing beacons")
                    /*_controller.scanStarted = false
                     _controller.updateView()*/
                }
            }
            //var dd = RangeNotifier { beacons, region ->  }
            //beaconManager.addRangeNotifier()

            makeFarmCode()
        }

        fun checkPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Main.main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    Main.main.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE), Main.PERMISSION_REQUEST_FINE_LOCATION)
                }
            }
        }

        fun startScan(_immortal_uuid:String? = null) {
            if(scanStarted)
                return

            Main.log("Scan started!")
            scanStarted = true
            beaconManager.startMonitoring(region)
            beaconManager.startRangingBeacons(region)


            if(_immortal_uuid != null)
                immortalBeacon = createBeacon(_immortal_uuid!!, true)
        }

        fun stopScan() {
            if(!scanStarted)
                return

            scanStarted = false
            Main.log("Scan stopped!")
            beaconManager.stopMonitoring(region)
            beaconManager.stopRangingBeacons(region)
            Main.log("beaconManager.foregroundBetweenScanPeriod ${beaconManager.foregroundBetweenScanPeriod}")

            if(immortalBeacon != null) {
                immortalBeacon!!.stopAdvertising()
                immortalBeacon = null
            }
        }

        fun createBeacon(_uuid:String, _immortal_beacon:Boolean = false): BeaconTransmitter {
            makeFarmCode()
            Main.log("createBeacon uuid $_uuid")
            Main.log("createBeacon id2+id3 ${beaconFarmCode.slice(0..4) + beaconFarmCode.slice(5..9)}")
            val beacon = Beacon.Builder().setId1(_uuid).setId2(beaconFarmCode.slice(0..4)).setId3(beaconFarmCode.slice(5..9))
                .setManufacturer(0x4C).setTxPower(0).build()

            /*var bluetoothManager: BluetoothManager = Main.main.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            var bluetoothAdapter = bluetoothManager.adapter
            var bluetoothAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

            var  settingsBuilder:AdvertiseSettings.Builder = AdvertiseSettings.Builder();
            settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
            settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
            settingsBuilder.setConnectable(false)

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setTimeout(0)
                .setConnectable(false)
                .build()

            if (ActivityCompat.checkSelfPermission( Main.main.applicationContext, Manifest.permission.BLUETOOTH_ADVERTISE ) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return
            }
            val pUuid = ParcelUuid(UUID.fromString("cdb7950d-73f1-4d4d-8e47-c090502dbd63"))
            val pServiceDataUuid = ParcelUuid(UUID.fromString("0000950d-0000-1000-8000-00805f9b34fb"))

            var adData:AdvertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .addServiceData(pServiceDataUuid, "D".toByteArray())
                .build()
            bluetoothAdvertiser.startAdvertising(settings, adData, object : AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    Main.log("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA $errorCode")
                }
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Main.log("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA Success start advertising")
                }
            });*/


            //BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            //BluetoothAdapter ;
            //BluetoothLeAdvertiser bluetoothAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser()

            val beaconTransmitter = BeaconTransmitter(Main.main.applicationContext, beaconParser)

            Main.log("beaconTransmitter.advertiseMode ${beaconTransmitter.advertiseMode}")
            beaconTransmitter.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
            beaconTransmitter.advertiseTxPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
            Main.log("${beaconTransmitter.advertiseTxPowerLevel} = ${AdvertiseSettings.ADVERTISE_TX_POWER_HIGH}")
            Main.log("${beaconTransmitter.advertiseMode} = ${AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY}")
            //beaconTransmitter.advertiseTxPowerLevel
            beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    Main.log("Error from start advertising $errorCode")
                }
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Main.log("Success start advertising")
                }
            })

            if(!_immortal_beacon)
                beaconTransmitters[_uuid] = beaconTransmitter

            return beaconTransmitter
        }

        fun killBeacon(_uuid: String) {
            Main.log("killBeacon $_uuid")
            if(beaconTransmitters.contains(_uuid)) {
                beaconTransmitters[_uuid]!!.stopAdvertising()
                beaconTransmitters.remove(_uuid)
            }
        }

        fun killAllBeacons() {
            beaconTransmitters.forEach {
                it.value.stopAdvertising()
            }
            beaconTransmitters.clear()
        }

        private fun makeFarmCode() {
            beaconFarmCode = ""
            UserData.tripFieldindex.forEach {
                beaconFarmCode += Dict.farmIndexNumByChar["$it"]
            }
            for (i in 0 until 10 - beaconFarmCode.length) beaconFarmCode += 0
        }

        fun putDashToIndex(_uuid:String, _index:Int):String {
            return _uuid.replaceRange(_index, _index, "-")
        }

        fun completeRawUUID(_uuid:String):String {
            var uuid = _uuid
            for (i in 0 until 32 - uuid.length) uuid += 0

            uuid = putDashToIndex(uuid, 8)
            uuid = putDashToIndex(uuid, 13)
            uuid = putDashToIndex(uuid, 18)
            uuid = putDashToIndex(uuid, 23)
            return uuid
        }

        fun makeCodeFromNumber(_number:String):String {
            var result = ""
            for (i in 0 until _number.length)
                result += Dict.carNumberHexsByChar[_number[i].toString()]

            return result
        }

        fun makeNumberFromUUID(_uuid:String):String {
            var uuidTail = _uuid.slice (   3.._uuid.length-1)
            var numLength = _uuid.slice(2..2).toInt()
            uuidTail = uuidTail.replace("-", "", true)

            var number = ""
            for (i in 0 until numLength) {
                number += Dict.carNumberCharsByHex[uuidTail.slice(i * 2..i * 2 + 1)]
            }
            return number
        }
    }
}