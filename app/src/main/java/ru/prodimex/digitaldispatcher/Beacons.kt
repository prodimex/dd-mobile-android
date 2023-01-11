package ru.prodimex.digitaldispatcher

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import org.altbeacon.beacon.*
import java.lang.Long.parseLong
import java.math.BigInteger
import java.sql.Time
import java.util.*


class Beacons {
    companion object {
        private val TAG = "BEACONS"
        lateinit var beaconManager:BeaconManager
        var immortalBeacon:BeaconTransmitter? = null
        var controller:AppController? = null

        //val beaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        val beaconParser = BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")

        val region = Region("all-beacons", null, null, null)
        val beaconTransmitters: MutableMap<String, BeaconTransmitter> = mutableMapOf()
        var beaconFarmCode:String = "1234512345"

        var initialized = false
        var scanStarted = false
        fun init() {
            if(initialized)
                return

            controller = null
            initialized = true

            beaconManager = BeaconManager.getInstanceForApplication(Main.main)
            beaconManager.beaconParsers.clear()
            beaconManager.beaconParsers.add(beaconParser)

            //setupForegroundService()
            beaconManager.setEnableScheduledScanJobs(false)
            beaconManager.setBackgroundBetweenScanPeriod(0)
            beaconManager.setBackgroundScanPeriod(1100)

            beaconManager.getRegionViewModel(region).rangedBeacons.observeForever { beacons ->
                Main.log("rangedBeacons.observeForever ${beacons.size}", TAG)
                if(controller != null)
                    controller!!.scanObserver(beacons)
            }

            beaconManager.getRegionViewModel(region).regionState.observeForever { state ->
                if (state == MonitorNotifier.INSIDE) {
                    Main.log("Detected beacons(s)", TAG)
                } else {
                    Main.log("Stopped detecteing beacons", TAG)
                    stopScan()
                    //PopupManager.showAlert("Сканирование приостановлено, по неизвестной причине и автоматически перезапущено.")
                    Main.main.toastMe("Сканирование приостановлено, по неизвестной причине и автоматически перезапущено.")

                    Timer().schedule(object:TimerTask() { override fun run() { startScan() }}, 200)
                }
            }

            makeFarmCode()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setupForegroundService() {
            val builder = Notification.Builder(Main.main, "ПРОДИМЕКС")
            //builder.setSmallIcon(org.altbeacon.beacon.R.drawable.ic_launcher_background)
            builder.setContentTitle("Поиск сигнала")
            val intent = Intent(Main.main, BaconsBgActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                Main.main, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent);
            val channel =  NotificationChannel("prodimex-notification-id",
                "Уведомление PRODIMEX", NotificationManager.IMPORTANCE_DEFAULT)
            channel.setDescription("My Notification Channel Description")

            val notificationManager = Main.main.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
            BeaconManager.getInstanceForApplication(Main.main).enableForegroundServiceScanning(builder.build(), 456);
        }

        fun checkPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Main.main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    Main.main.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE), Main.PERMISSION_REQUEST_FINE_LOCATION)
                }
            }
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (

                    Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || Main.main.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    Main.main.requestPermissions(arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE), Main.PERMISSION_REQUEST_FINE_LOCATION)
                }
            }*/
        }

        fun startScan(_immortal_uuid:String? = null) {
            Main.log("${scanStarted} ${initialized}", TAG)
            if(scanStarted || !initialized)
                return

            Main.log("Scan started!", TAG)
            scanStarted = true
            beaconManager.startMonitoring(region)
            beaconManager.startRangingBeacons(region)

            if(_immortal_uuid != null)
                immortalBeacon = createBeaconTransmitter(_immortal_uuid)
        }

        fun stopScan() {
            if(!scanStarted)
                return

            scanStarted = false
            Main.log("Scan stopped!", TAG)
            beaconManager.stopMonitoring(region)
            beaconManager.stopRangingBeacons(region)
            Main.log("beaconManager.foregroundBetweenScanPeriod ${beaconManager.foregroundBetweenScanPeriod}")
        }
        fun createBeaconTransmitter(_uuid:String): BeaconTransmitter {
            makeFarmCode()
            Main.log("createBeacon uuid $_uuid", TAG)
            Main.log("createBeacon id2+id3 ${beaconFarmCode.slice(0..4) + beaconFarmCode.slice(5..9)}", TAG)


            val beacon = Beacon.Builder()
                .setId1(_uuid)
                //.setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2(beaconFarmCode.slice(0..4))
                .setId3(beaconFarmCode.slice(5..9))
                .setManufacturer(0x0118)
                .setBeaconTypeCode(3)
                .setTxPower(-59)
                .setDataFields(mutableListOf(0))

                .build()

            val beaconTransmitter = BeaconTransmitter(Main.main.applicationContext, beaconParser)
            beaconTransmitter.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
            beaconTransmitter.advertiseTxPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH

            beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    Main.log("Error from start advertising $errorCode", TAG)
                    Main.main.toastMe("Ошибка начала вещания (код ${errorCode})")
                }
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Main.log("Success start advertising", TAG)
                }
            })

            Main.log("ADVERTISE_MODE_LOW_LATENCY ${beaconTransmitter.advertiseMode} = ${AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY}", TAG)
            Main.log("ADVERTISE_TX_POWER_HIGH ${beaconTransmitter.advertiseTxPowerLevel} = ${AdvertiseSettings.ADVERTISE_TX_POWER_HIGH}", TAG)

            return beaconTransmitter
        }
        fun createBeacon(_uuid:String, _immortal_beacon:Boolean = false) {
            if(beaconTransmitters.contains(_uuid)) {
                Main.log("BEACON $_uuid ALREADY CREATED", TAG)
                return
            }

            val beaconTransmitter = createBeaconTransmitter(_uuid)

            if(!_immortal_beacon)
                beaconTransmitters[_uuid] = beaconTransmitter
        }

        fun killBeacon(_uuid: String) {
            Main.log("killBeacon $_uuid", TAG)
            if(beaconTransmitters.contains(_uuid)) {
                beaconTransmitters[_uuid]!!.stopAdvertising()
                beaconTransmitters.remove(_uuid)
            } else {
                Main.log("Error - Beacon not finded", TAG)
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
            var uuidTail = _uuid.replace("-", "", true)
            var sclength = uuidTail.slice(2..2).toInt()

            uuidTail = uuidTail.slice (3 + sclength..uuidTail.length-1)
            var numLength = uuidTail.slice(0..0).toInt()
            uuidTail = uuidTail.slice(1..uuidTail.length-1)
            var number = ""
            for (i in 0 until numLength) {
                number += Dict.carNumberCharsByHex[uuidTail.slice(i * 2..i * 2 + 1)]
            }
            return number
        }

        fun getTripIdFromUUID(_uuid:String):String {
            var uuidTail = _uuid.slice (3.._uuid.length-1).replace("-", "", true)
            uuidTail = uuidTail.slice(1..BigInteger(uuidTail.get(0).toString(), 16).toInt())

            return BigInteger(uuidTail, 16).toString()
        }
    }
}

