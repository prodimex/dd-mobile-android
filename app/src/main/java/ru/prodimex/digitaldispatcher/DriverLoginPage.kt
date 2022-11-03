package ru.prodimex.digitaldispatcher

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.vicmikhailau.maskededittext.MaskedEditText


class DriverLoginPage:AppController() {
    companion object {

    }
    private var showPassword = false
    val phoneField:MaskedEditText
    var passwordText:EditText? = null
    var errorField:TextView? = null
    var loginButton:PreloadingButton? = null
    var requestButton:PreloadingButton? = null
    var passwordActionsView:LinearLayout? = null
    var requestPasswordActionsView:LinearLayout? = null

    init {
        showLayout(R.layout.driver_enter_page)
        phoneField = scene.findViewById(R.id.user_phone)
        phoneField.setText(Main.getParam("phone"))
        showPasswordActions()
        updateView()

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
        /*
            "phone" to "79857739431",
            "password" to "373369",
            "token" to "0000",
            "notificationToken" to "",
            Пряхин: 79036396165
            Пароль: 802697
        */
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
            Main.log("Result ======== ")
            Main.log(_response)
            if(_response["result"] == "error") {
                showError(_response)
            } else {

                showPasswordActions()
                passwordText!!.setText("")
                passwordText!!.requestFocus()
                Main.setParam("password", "")
                (Main.main.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager).showSoftInput(passwordText, InputMethodManager.SHOW_IMPLICIT)
                /*Main.setParam("server", _response["server"].toString())
                Main.setParam("token", _response["token"].toString())
                Main.setParam("token_type", _response["token_type"].toString())

                HTTPRequest.token = _response["token"].toString()
                HTTPRequest.token_type = _response["token_type"].toString()
                HTTPRequest.nodeServerUrl =  _response["server"].toString() + "/mobile/"
                HTTPRequest("users/current", _requestMethod = "GET", _callback = fun (_response:HashMap<String, Any>) {
                    UserData.collectData(_response)
                    scene.showPage(Main.TRIP_PAGE)
                }).execute()*/
            }
        }, _requestMethod = "GET", _isTms = true).execute()
    }

    fun showError(_response:HashMap<String, Any>) {
        enableInput(true)
        when(_response["responseCode"]) {
            "403" -> errorField!!.text = "Неверный пароль."
            "400" -> errorField!!.text = "Пользователь с таким номером\nтелефона не найден."
            "888" -> errorField!!.text = "Интернет выключен.\nПроверьте Wi-Fi и Мобильные данные"
            "999" -> errorField!!.text = "Ошибка связи."
            else -> errorField!!.text = "Ошибка связи. (${_response["responseCode"]})"
        }
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

            Main.log("Result ======== ")
            Main.log(_response)
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
                    UserData.collectData(_response)
                    Beacons.init()
                    Beacons.startScan()
                    switchTopage(Main.TRIP_PAGE)
                }).execute()
            }
        }, _isTms = true).execute()
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
            errorField!!.setText("Номер телефона должен\nсодержать 10 цифр.")
            vis(R.id.error_field, true)
            return false
        }
        if(passwordActionsView!!.parent != null && passwordText!!.text.length != 6) {
            errorField!!.setText("Введите 6ти значный\nпароль из СМС")
            vis(R.id.error_field, true)
            return false
        }
        return true
    }

    override fun updateView() {
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