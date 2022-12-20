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
import java.io.File


class Main : AppCompatActivity() {
    companion object {
        const val TRIP_PAGE = "TRIP_PAGE"
        const val PROFILE_PAGE = "PROFILE_PAGE"
        const val DRIVER_LOGIN_PAGE = "DRIVER_LOGIN_PAGE"
        const val DRIVER_SETTINGS_PAGE = "DRIVER_SETTINGS_PAGE"

        const val BEACON_SCANNER_PAGE = "BEACON_SCANNER_PAGE"


        const val LOADER_ENTER_PAGE = "LOADER_ENTER_PAGE"
        const val LOADER_QUEUE_PAGE = "LOADER_QUEUE_PAGE"
        const val LOADER_LOADED_PAGE = "LOADER_LOADED_PAGE"
        const val LOADER_CANCELLED_PAGE = "LOADER_CANCELLED_PAGE"
        const val LOADER_SETTINGS_PAGE = "LOADER_SETTINGS_PAGE"

        const val UPDATE_PAGE = "UPDATE_PAGE"

        const val ROLE_SELECTOR = "ROLE_SELECTOR"


        lateinit var sharedPref: SharedPreferences
        lateinit var main:Main

        var logCounter = 0
        val sessionLogs:ArrayList<String> = arrayListOf()
        fun log(_str:Any) {
            println("#${logCounter++} MOMOZODO: $_str")
            sessionLogs.add("$_str")
        }
        val PERMISSION_REQUEST_FINE_LOCATION = 1
        val PERMISSION_BLUETOOTH_SCAN = 2
        val PERMISSION_BLUETOOTH_ADVERTISE = 3
        /*val carNumberChars = mapOf(
            "0" to "00", "1" to "01", "2" to "02", "3" to "03", "4" to "04", "5" to "05", "6" to "06",
            "7" to "07", "8" to "08", "9" to "09",
            "А" to "0A", "В" to "0B", "Е" to "0C", "К" to "0D", "М" to "0E", "Н" to "0F", "О" to "10",
            "Р" to "11", "С" to "12", "Т" to "13", "У" to "14", "Х" to "15", " " to "16"
        )
        var charByHexForCarNumbers = HashMap<String, String>()*/

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
    //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    //
    //
    //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    var currentApiVersion:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.root_layout)
        Dict.init()
        main = this
        sharedPref = getSharedPreferences("ProdimexLocalStorage", MODE_PRIVATE)
        Beacons.checkPermissions()
        showPage(UPDATE_PAGE)
        /*if(AppConfig.APP_MODE == AppConfig.DEV_MODE) {
            showPage(UPDATE_PAGE)
        } else if(AppConfig.APP_MODE == AppConfig.DRIVER_MODE) {
            showPage(LOGIN_PAGE)
        } else if(AppConfig.APP_MODE == AppConfig.LOADER_MODE) {
            showPage(LOADER_ENTER_PAGE)
        }*/

        //supportActionBar?.hide()
        currentApiVersion = android.os.Build.VERSION.SDK_INT
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

        setOnClick(R.id.select_scanner_role) { showPage(BEACON_SCANNER_PAGE) }
        setOnClick(R.id.select_driver_mode) { showPage(DRIVER_LOGIN_PAGE) }
        setOnClick(R.id.select_loader_mode) { showPage(LOADER_ENTER_PAGE) }
    }

    fun showPage(pageId:String) {
        when (pageId) {
            TRIP_PAGE -> DriverTripPage().afterInit(TRIP_PAGE)
            PROFILE_PAGE -> DriverProfilePage().afterInit(PROFILE_PAGE)
            DRIVER_LOGIN_PAGE -> {
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
                DriverLoginPage().afterInit(DRIVER_LOGIN_PAGE)
            }
            DRIVER_SETTINGS_PAGE -> DriverSettingsPage().afterInit(DRIVER_SETTINGS_PAGE)

            BEACON_SCANNER_PAGE -> BeaconScannerPage().afterInit(BEACON_SCANNER_PAGE)

            LOADER_ENTER_PAGE -> LoaderEnterPage().afterInit(LOADER_ENTER_PAGE)
            LOADER_QUEUE_PAGE -> LoaderQueuePage().afterInit(LOADER_QUEUE_PAGE)
            LOADER_LOADED_PAGE -> LoaderLoadedPage().afterInit(LOADER_LOADED_PAGE)
            LOADER_CANCELLED_PAGE -> LoaderCancelledPage().afterInit(LOADER_CANCELLED_PAGE)
            LOADER_SETTINGS_PAGE -> LoaderSettingsPage().afterInit(LOADER_SETTINGS_PAGE)

            UPDATE_PAGE -> UpdatePage().afterInit(UPDATE_PAGE)

            ROLE_SELECTOR -> showRoleSelector()
        }
    }
    @SuppressLint("MissingPermission")
    fun showWhenBluetoothIsOff() {
        AlertDialog.Builder(this)
            .setTitle("Внимание Bluetooth выключен!")
            .setMessage("Для корректной работы приложения необходимо включить Bluetooth-адаптер.")
            .setPositiveButton("ВКЛЮЧИТЬ") { dialog, which ->
                BluetoothAdapter.getDefaultAdapter().enable()
                Main.main.toastMe("Bluetooth включен!")
            }.setIcon(android.R.drawable.ic_dialog_alert).show()
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
        Main.log("=================== onStop")
        super.onStop()
        Beacons.stopScan()
        //unregisterReceiver(mReceiver)
    }

    override fun onResume() {
        super.onResume()
        Beacons.startScan()
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
        Main.log("=================== onDestroy")
        Beacons.killAllBeacons()
        super.onDestroy()
    }

    fun runApplication() {
        when (AppConfig.APP_MODE) {
            AppConfig.DEV_MODE -> showPage(Main.ROLE_SELECTOR)
            AppConfig.LOADER_MODE -> showPage(Main.LOADER_ENTER_PAGE)
            AppConfig.DRIVER_MODE -> showPage(Main.DRIVER_LOGIN_PAGE)
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
