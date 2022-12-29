package ru.prodimex.digitaldispatcher

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import ru.prodimex.digitaldispatcher.driver.*
import ru.prodimex.digitaldispatcher.loader.*
import ru.prodimex.digitaldispatcher.scaner.BeaconScannerPage
import java.io.File

/*
    "phone" to "7 985 773 94 31",
    "password" to "373369",
    "token" to "0000",
    "notificationToken" to "",

    Пряхин: 79036396165
    Пароль: 802697

    Бугорский Владимир Анатольевич 79036396179
    Пароль: 899012
*/

class Main : AppCompatActivity() {
    companion object {
        lateinit var sharedPref: SharedPreferences
        lateinit var main:Main

        var logCounter = 0
        val sessionLogs:ArrayList<String> = arrayListOf()
        fun log(_str:Any) {
            println("#${logCounter++} ${sessionLogs.size} MOMOZODO: $_str")
            sessionLogs.add("$_str")
            while (sessionLogs.size > 5000)
                sessionLogs.removeAt(0)

        }
        val PERMISSION_REQUEST_FINE_LOCATION = 1
        val PERMISSION_BLUETOOTH_SCAN = 2
        val PERMISSION_BLUETOOTH_ADVERTISE = 3

        fun getParam(_key:String):String {
            return sharedPref.getString(_key, "").toString()
        }
        fun setParam(_key:String, _value:String) {
            sharedPref.edit().putString(_key, _value).apply()
        }
    }

    private val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

    var currentApiVersion:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.root_layout)
        Dict.init()
        main = this
        sharedPref = getSharedPreferences("ProdimexLocalStorage", MODE_PRIVATE)
        Beacons.checkPermissions()
        showPage(Dict.UPDATE_PAGE)

        currentApiVersion = Build.VERSION.SDK_INT
        if(currentApiVersion!! >= Build.VERSION_CODES.KITKAT) {
            window.decorView.setOnSystemUiVisibilityChangeListener {
                setFullscreen(it)
            }
            setFullscreen()
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            showWhenBluetoothIsOff()
        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun showRoleSelector() {
        findViewById<RelativeLayout>(R.id.root_layer).removeAllViews()
        findViewById<RelativeLayout>(R.id.root_layer).addView((layoutInflater.inflate(R.layout.role_selector, null) as View))

        setOnClick(R.id.select_scanner_role) { showPage(Dict.BEACON_SCANNER_PAGE) }
        setOnClick(R.id.select_driver_mode) { showPage(Dict.DRIVER_LOGIN_PAGE) }
        setOnClick(R.id.select_loader_mode) { showPage(Dict.LOADER_ENTER_PAGE) }
    }

    fun showPage(pageId:String) {
        when (pageId) {
            Dict.TRIP_PAGE -> DriverTripPage().afterInit(pageId)
            Dict.PROFILE_PAGE -> DriverProfilePage().afterInit(pageId)
            Dict.DRIVER_LOGIN_PAGE -> {
                Beacons.killAllBeacons()
                Beacons.stopScan()
                if(Beacons.immortalBeacon != null) {
                    Beacons.immortalBeacon!!.stopAdvertising()
                    Beacons.immortalBeacon = null
                }
                UserData.tripData = null
                UserData.currentTripStatus = 0
                if(Beacons.immortalBeacon != null) {
                    Beacons.immortalBeacon!!.stopAdvertising()
                    Beacons.immortalBeacon = null
                }
                DriverAppController.onLoading = false
                DriverAppController.loaderFinded = false
                DriverTripPage.toLoaderConnected = false
                DriverTripPage.toLoaderConnectionStarted = false
                DriverTripPage.currentRangingState = ""
                DriverLoginPage().afterInit(pageId)
            }
            Dict.DRIVER_SETTINGS_PAGE -> DriverSettingsPage().afterInit(pageId)

            Dict.BEACON_SCANNER_PAGE -> BeaconScannerPage().afterInit(pageId)

            Dict.LOADER_ENTER_PAGE -> LoaderEnterPage().afterInit(pageId)
            Dict.LOADER_QUEUE_PAGE -> LoaderQueuePage().afterInit(pageId)
            Dict.LOADER_LOADED_PAGE -> LoaderLoadedPage().afterInit(pageId)
            Dict.LOADER_CANCELLED_PAGE -> LoaderCancelledPage().afterInit(pageId)
            Dict.LOADER_SETTINGS_PAGE -> LoaderSettingsPage().afterInit(pageId)

            Dict.UPDATE_PAGE -> UpdatePage().afterInit(pageId)

            Dict.ROLE_SELECTOR -> showRoleSelector()
        }
    }
    @SuppressLint("MissingPermission")
    fun showWhenBluetoothIsOff() {
        AlertDialog.Builder(this)
            .setTitle("Внимание Bluetooth выключен!")
            .setMessage("Для корректной работы приложения необходимо включить Bluetooth-адаптер.")
            .setPositiveButton("ВКЛЮЧИТЬ") { dialog, which ->
                BluetoothAdapter.getDefaultAdapter().enable()
                toastMe("Bluetooth включен!")
            }.setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context:Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        log("BluetoothAdapter.STATE_OFF")
                        showWhenBluetoothIsOff()
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        log("BluetoothAdapter.STATE_TURNING_OFF")
                    }
                    BluetoothAdapter.STATE_ON -> {
                        log("BluetoothAdapter.STATE_ON")
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                        log("BluetoothAdapter.STATE_TURNING_ON")
                    }
                }
            }
        }
    }

    override fun onStop() {
        log("=================== onStop Intent.ACTION_SCREEN_OFF:${intent.action}")
        super.onStop()
        //unregisterReceiver(mReceiver)
    }

    override fun onResume() {
        log("=================== onResume Intent.ACTION_SCREEN_OFF:${intent.action}")
        Beacons.startScan()
        super.onResume()
    }

    fun toastMe(_str:String) {
        var toast = Toast.makeText(this, _str, Toast.LENGTH_SHORT)
        val v = toast.view!!.findViewById<View>(android.R.id.message) as TextView
        if (v != null) v.gravity = Gravity.CENTER
        toast.show()
    }

    fun setOnClick(_btnId:Int, _callback:(()->Unit)) {
        (findViewById<Button>(_btnId)).setOnClickListener { _callback.invoke() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("fine location permission granted")
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener(DialogInterface.OnDismissListener { })
                    builder.show()
                }
                return
            }
            PERMISSION_BLUETOOTH_SCAN -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("BLUETOOTH_SCAN permission granted")
                } else {
                    log("BLUETOOTH_SCAN permission denied")
                }
                return
            }
            PERMISSION_BLUETOOTH_ADVERTISE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("BLUETOOTH_ADVERTISE permission granted")
                } else {
                    log("BLUETOOTH_ADVERTISE permission denied")
                }
                return
            }
            /*PERMISSION_REQUEST_BLUETOOTH_CONNECT -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("BLUETOOTH_CONNECT permission granted")
                } else {
                    log("BLUETOOTH_CONNECT permission denied")
                }
                return
            }*/
        }
    }

    private fun setFullscreen(isVisible:Int = 0) {
        if ((isVisible and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && currentApiVersion!! >= Build.VERSION_CODES.KITKAT)
            window.decorView.systemUiVisibility = uiOptions
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (currentApiVersion!! >= Build.VERSION_CODES.KITKAT && hasFocus)
            window.decorView.systemUiVisibility = uiOptions
    }

    override fun onDestroy() {
        log("=================== onDestroy")
        Beacons.killAllBeacons()
        super.onDestroy()
    }

    fun runApplication() {
        when (AppConfig.APP_MODE) {
            AppConfig.DEV_MODE -> showPage(Dict.ROLE_SELECTOR)
            AppConfig.LOADER_MODE -> showPage(Dict.LOADER_ENTER_PAGE)
            AppConfig.DRIVER_MODE -> showPage(Dict.DRIVER_LOGIN_PAGE)
        }
    }

    fun installUpdate() {
        runApplication()
        val intent = Intent(Intent.ACTION_VIEW)
        log(Environment.getExternalStorageDirectory())
        val f = File("${Environment.getExternalStorageDirectory().absolutePath}/Download/pxupdate.apk")

        val data = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", f)
        intent.setDataAndType(data, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
    }

    private var backPressed:Long = 0
    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis())
            System.exit(0)
        else
            Toast.makeText(baseContext, "Нажмите снова, чтобы выйти!", Toast.LENGTH_SHORT).show()
        backPressed = System.currentTimeMillis()
    }
}
