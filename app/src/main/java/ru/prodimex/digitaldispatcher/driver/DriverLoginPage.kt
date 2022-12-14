package ru.prodimex.digitaldispatcher.driver

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vicmikhailau.maskededittext.MaskedEditText
import ru.prodimex.digitaldispatcher.*
import ru.prodimex.digitaldispatcher.uitools.PreloadingButton


class DriverLoginPage: AppController() {
    private var showPassword = false
    var phoneField:MaskedEditText? = null
    var passwordText:EditText? = null
    var errorField:TextView? = null
    var loginButton: PreloadingButton? = null
    var requestButton: PreloadingButton? = null
    var passwordActionsView:LinearLayout? = null
    var requestPasswordActionsView:LinearLayout? = null
    override val TAG = "DRIVER LOGIN PAGE"
    init {
        init(R.layout.driver_enter_page)
    }

    override fun init(_layoutId:Int) {
        if(Main.getParam("userData") != "" && Main.getParam("driverLoggedOuted") != "") {
            Main.log(Main.getParam("userData"), TAG)
            val userData:HashMap<String, Any>
                = Gson().fromJson(Main.getParam("userData"), object : TypeToken<HashMap<String?, Any?>?>() {}.type)



            if(Main.getParam("tripData") != "") {
                val tripData:HashMap<String, Any>
                        = Gson().fromJson(Main.getParam("tripData"), object : TypeToken<HashMap<String?, Any?>?>() {}.type)
                UserData.collectTripData(tripData)
            }

            HTTPRequest.token = Main.getParam("token").toString()
            HTTPRequest.token_type = Main.getParam("token_type").toString()
            HTTPRequest.nodeServerUrl =  Main.getParam("server") + "/mobile/"

            if(Main.getParam("driverPendingRequests") != "") {
                val pendingRequests:HashMap<String, HashMap<String, String>>
                        = Gson().fromJson(Main.getParam("driverPendingRequests"), object : TypeToken<HashMap<String, HashMap<String, String>>?>() {}.type)
                DriverAppController.pendingRequests = pendingRequests
            }

            runApp(userData)

            DriverAppController.pendingRequestsRun()
        } else {
            showLayout(_layoutId)
            phoneField = scene.findViewById(R.id.user_phone)
            phoneField!!.setText(Main.getParam("phone"))
            showPasswordActions()
            updateView()
        }
    }

    fun showPasswordActions() {
        enableInput(true)
        if(requestPasswordActionsView != null && requestPasswordActionsView!!.parent != null) {
            scene.findViewById<LinearLayout>(R.id.driver_login_actions_container).removeView(requestPasswordActionsView)
        }
        if(passwordActionsView == null) {
            passwordActionsView = scene.layoutInflater.inflate(R.layout.driver_login_password_actioons, null ) as LinearLayout
            passwordActionsView!!.findViewById<View>(R.id.get_password_by_sms).setOnClickListener{
                showPasswordRequestActions()
            }
            loginButton = PreloadingButton(passwordActionsView!!.findViewById(R.id.login_page_enter_button),
                passwordActionsView!!.findViewById(R.id.login_page_enter_button_loader))


            passwordText = passwordActionsView!!.findViewById(R.id.user_password)
            passwordText!!.setText(Main.getParam("password"))

            passwordActionsView!!.findViewById<View>(R.id.visible_on).setOnClickListener{ showPassword = true; updateView() }
            passwordActionsView!!.findViewById<View>(R.id.visible_off).setOnClickListener{ showPassword = false; updateView() }
            passwordActionsView!!.findViewById<View>(R.id.login_page_enter_button).setOnClickListener { onLoginPress() }
        }
        scene.findViewById<LinearLayout>(R.id.driver_login_actions_container).addView(passwordActionsView)
        vis(R.id.error_field, false)
        errorField = scene.findViewById(R.id.error_field)
        enableInput(true)
    }

    fun showPasswordRequestActions() {
        enableInput(true)
        if(passwordActionsView != null && passwordActionsView!!.parent != null) {
            scene.findViewById<LinearLayout>(R.id.driver_login_actions_container).removeView(passwordActionsView)
        }
        if(requestPasswordActionsView == null) {
            requestPasswordActionsView = scene.layoutInflater.inflate(R.layout.driver_login_request_password_actions, null ) as LinearLayout
            requestPasswordActionsView!!.findViewById<View>(R.id.go_back_to_login).setOnClickListener{
                showPasswordActions()
            }

            requestButton = PreloadingButton(requestPasswordActionsView!!.findViewById(R.id.login_page_request_button),
                requestPasswordActionsView!!.findViewById(R.id.login_page_request_button_loader))

            requestPasswordActionsView!!.findViewById<View>(R.id.login_page_request_button).setOnClickListener { onRequestPress() }
        }
        scene.findViewById<LinearLayout>(R.id.driver_login_actions_container).addView(requestPasswordActionsView)
        vis(R.id.error_field, false)
        errorField = scene.findViewById(R.id.error_field)
        enableInput(true)
    }

    fun onRequestPress() {
        if(!validate()) return
        Main.setParam("phone", phoneField!!.unMaskedText.toString())
        vis(R.id.error_field, false)

        enableInput(false)

        HTTPRequest("password/create", hashMapOf(
            "phone" to "7${phoneField!!.unMaskedText.toString()}",
            "notificationToken" to "",
        ), _callback = fun (_response:HashMap<String, Any>) {
            Main.log("Result ======== ", TAG)
            Main.log(_response, TAG)
            if(_response["result"] == "error") {
                showError(_response)
            } else {
                showPasswordActions()
                passwordText!!.setText("")
                passwordText!!.requestFocus()
                Main.setParam("password", "")
                (Main.main.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager).showSoftInput(passwordText, InputMethodManager.SHOW_IMPLICIT)
            }
        }, _requestMethod = "GET", _isTms = true).execute()
    }

    fun showError(_response:HashMap<String, Any>) {
        enableInput(true)
        errorField!!.text = Dict.getErrorByCode(_response["responseCode"].toString())
        vis(R.id.error_field, true)
    }

    fun onLoginPress() {
        if(!validate()) return
        Main.setParam("phone", phoneField!!.unMaskedText.toString())
        Main.setParam("password", passwordText!!.text.toString())
        vis(R.id.error_field, false)

        enableInput(false)

        HTTPRequest("core/login-driver", hashMapOf(
            "phone" to "7${phoneField!!.unMaskedText.toString()}",
            "password" to passwordText!!.text.toString(),
            "token" to "0000",
            "notificationToken" to "",
        ), _callback = fun (_response:HashMap<String, Any>) {
            Main.log("Result ======== ", TAG)
            Main.log(_response, TAG)
            if(_response["result"] == "error") {
                showError(_response)
            } else {
                Main.setParam("server", _response["server"].toString())
                Main.setParam("token", _response["token"].toString())
                Main.setParam("token_type", _response["token_type"].toString())

                HTTPRequest.token = _response["token"].toString()
                HTTPRequest.token_type = _response["token_type"].toString()
                HTTPRequest.nodeServerUrl =  _response["server"].toString() + "/mobile/"
                HTTPRequest("users/current", _requestMethod = "GET", _callback = fun (_response:HashMap<String, Any>) {
                    runApp(_response)
                }).execute()
            }
        }, _isTms = true).execute()
    }

    private fun runApp(_userData:HashMap<String, Any>) {
        UserData.collectData(_userData)
        Main.setParam("driverLoggedOuted", "true")

        Beacons.init()
        switchTopage(Dict.TRIP_PAGE)
        var uuid = Dict.DRIVER_ON_FIELD_ON_AIR
        uuid += DriverAppController.currentCarNumber.length.let { Integer.toHexString(it).uppercase()}
        uuid += DriverAppController.numberCode
        uuid = Beacons.completeRawUUID(uuid)
        Beacons.startScan(uuid)
    }

    private fun enableInput(_isEnable:Boolean = true) {
        if(passwordActionsView != null && passwordActionsView!!.parent != null) {
            if(_isEnable) loginButton!!.stopPreloading() else loginButton!!.startPreloading()

            passwordText!!.isEnabled = _isEnable

            passwordActionsView!!.findViewById<View>(R.id.visible_on).isEnabled = _isEnable
            passwordActionsView!!.findViewById<View>(R.id.visible_off).isEnabled = _isEnable
            passwordActionsView!!.findViewById<View>(R.id.get_password_by_sms).isEnabled = _isEnable
        }
        if(requestPasswordActionsView != null && requestPasswordActionsView!!.parent != null) {
            if(_isEnable) requestButton!!.stopPreloading() else requestButton!!.startPreloading()
            requestPasswordActionsView!!.findViewById<View>(R.id.go_back_to_login).isEnabled = _isEnable
        }
        phoneField!!.isEnabled = _isEnable
    }

    private fun validate():Boolean {
        vis(R.id.error_field, false)
        errorField!!.setText("")
        if(phoneField!!.unMaskedText.toString().length != 10) {
            errorField!!.setText("?????????? ???????????????? ????????????\n?????????????????? 10 ????????.")
            vis(R.id.error_field, true)
            return false
        }
        if(passwordActionsView!!.parent != null && passwordText!!.text.length != 6) {
            errorField!!.setText("?????????????? 6???? ??????????????\n???????????? ???? ??????")
            vis(R.id.error_field, true)
            return false
        }
        return true
    }

    override fun updateView() {
        if(pageKilled)
            return

        val selStart = passwordText!!.selectionStart
        val selEnd = passwordText!!.selectionEnd
        if (showPassword) {
            vis(R.id.visible_on, false)
            vis(R.id.visible_off, true)
            passwordText!!.inputType = InputType.TYPE_CLASS_NUMBER
        } else {
            vis(R.id.visible_on, true)
            vis(R.id.visible_off, false)
            passwordText!!.inputType = 18
        }
        passwordText!!.setSelection(selStart, selEnd)
    }
}