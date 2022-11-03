package ru.prodimex.digitaldispatcher

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class HTTPRequest(_method:String, _args:HashMap<String, String> = hashMapOf(), _callback:((data:HashMap<String, Any>)->Unit)? = null, _requestMethod:String = "POST", _isTms:Boolean = false):AsyncTask<Void, Void, String>() {
    companion object {
        private val tmsServerUrl = "https://apitms.prodimex.ru/mobile/"
        var nodeServerUrl = ""
        var token = ""
        var token_type = ""
        var notification_token = ""
    }
    private val callback = _callback
    private val args = _args
    private val appContext = Main.main.applicationContext
    private val REQUEST_METHOD = _requestMethod
    private val IS_TMS = _isTms
    private val METHOD = _method
    override fun doInBackground(vararg p0: Void?):String {
        if(!isNetworkAvailable(appContext))
            return "{'result':'error', 'responseCode':'888'}"

        var reqParam = ""
        args.forEach {
            if(reqParam != "")
                reqParam += "&"
            reqParam += "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }

        val con:HttpURLConnection
        var urlParams = ""
        if(REQUEST_METHOD == "GET") {
            urlParams = "?$reqParam"
            /*args.forEach {
                if(urlParams == "") urlParams += "?" else urlParams += ""
            }*/
        }
        if(IS_TMS) {
            con= URL(tmsServerUrl + METHOD + urlParams).openConnection() as HttpURLConnection
        } else {
            con = URL(nodeServerUrl + METHOD + urlParams).openConnection() as HttpURLConnection
            con.setRequestProperty("Authorization", "$token_type $token")
            //con.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            con.setRequestProperty("appVersion", "SUPER")
            con.setRequestProperty("nds", "true")

            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //con.setRequestProperty("car_id", "10")
            con.setRequestMethod(REQUEST_METHOD);
            /*con.setDoInput(true);
            con.setInstanceFollowRedirects(false);
            con.connect();*/
        }
        println("====================${con.url} $REQUEST_METHOD")
        try {
            with(con) {
                requestMethod = REQUEST_METHOD
                if(REQUEST_METHOD == "POST") { val wr = OutputStreamWriter(outputStream) ; wr.write(reqParam); wr.close()
                }
                val response = StringBuffer()
            BufferedReader(InputStreamReader(if (responseCode == HttpURLConnection.HTTP_OK) inputStream else errorStream)).use {
                    var inputLine = it.readLine(); while (inputLine != null) { response.append(inputLine); inputLine = it.readLine() }
                }

                var data:HashMap<String, Any> = Gson().fromJson(response.toString(), object : TypeToken<HashMap<String?, Any?>?>() {}.type)
                data["result"] = if (responseCode == HttpURLConnection.HTTP_OK) "success" else "error"
                data["responseCode"] = responseCode
                //disconnect()
                return Gson().toJson(data)
            }
        } catch (IO:Exception) {
            return "{'result':'error', 'responseCode':'999'}"
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    override fun onPostExecute(result:String) {
        super.onPostExecute(result)
        if(callback != null) {
            val data:HashMap<String, Any> = Gson().fromJson(result, object : TypeToken<HashMap<String?, Any?>?>() {}.type)
            data["responseCode"] = data["responseCode"].toString().toFloat().toInt().toString()
            callback!!(data)
        }

    }
}