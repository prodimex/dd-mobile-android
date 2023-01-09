package ru.prodimex.digitaldispatcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.gson.internal.LinkedTreeMap
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class UpdatePage:AppController() {
    override val TAG = "UPDATE PAGE"
    init {
        showLayout(R.layout.update_page)


        scene.findViewById<LinearLayout>(R.id.update_layout_content).visibility = View.GONE
        scene.findViewById<LinearLayout>(R.id.update_download_progress).visibility = View.GONE
        startPreloading()

        HTTPRequest.nodeServerUrl = AppConfig.UPDATE_URL
        HTTPRequest("", hashMapOf(
            "build_mode" to BuildConfig.BUILD_TYPE,
            "app_mode" to AppConfig.APP_MODE
            ), _requestMethod = "GET", _callback = fun(_response:HashMap<String, Any>) {
            if(_response["result"] == "error") {
                scene.runApplication()
                return
            }
            Main.log(_response, TAG)

            if(_response.contains("versionCode") && _response["versionCode"].toString().toFloat().toInt() > BuildConfig.VERSION_CODE) {
                showUpdateInfo(_response)
            } else {
                scene.runApplication()
            }
        }).execute()
    }

    fun showUpdateInfo(_updateData:HashMap<String, Any>) {
        scene.findViewById<LinearLayout>(R.id.update_layout_content).visibility = View.VISIBLE

        setText(R.id.update_page_version, "${_updateData["title"]}")
        var releaseChangesString = ""
        var release_changes = _updateData["release_changes"] as ArrayList<LinkedTreeMap<String, String>>
        release_changes.forEach {
            if(releaseChangesString != "")
                releaseChangesString += "<br>"
            releaseChangesString += it["title"]
        }
        setText(R.id.update_change_list, releaseChangesString)

        bytesTotal = _updateData["apkSize"].toString().toFloat()
        bytesTotalString = "${Dict.fileSizeAsString(bytesTotal)}"
        setText(R.id.update_weight_text, "Размер файла: $bytesTotalString")

        stopPreloading()

        setOnClick(R.id.do_not_update_button) {
            scene.runApplication()
        }
        setOnClick(R.id.update_now_button) {
            if(!checkStoragePermission())
                return@setOnClick

            progressLineParams = scene.findViewById<LinearLayout>(R.id.update_download_progress_fg).layoutParams as LayoutParams
            fullProgressLineWidth = progressLineParams!!.width.toFloat()
            showDownloadProgress(0)
            var updater = UpdateDownloader(_updateData["apkUrl"].toString(), this)
            updater.execute()
        }
    }

    var progressLineParams:LayoutParams? = null
    var fullProgressLineWidth = 0f
    var bytesTotal = 0f
    var bytesTotalString = ""
    fun showDownloadProgress(_percent:Int) {
        scene.findViewById<LinearLayout>(R.id.update_layout_content).visibility = View.GONE
        scene.findViewById<LinearLayout>(R.id.update_download_progress).visibility = View.VISIBLE

        setText(R.id.update_download_progress_text, "Загрузка - $_percent%")

        progressLineParams!!.width = (fullProgressLineWidth * (_percent.toFloat()/100f)).toInt()
        scene.findViewById<LinearLayout>(R.id.update_download_progress_fg).layoutParams = progressLineParams

        scene.findViewById<TextView>(R.id.update_download_loaded_bytes_text).text =
            "${Dict.fileSizeAsString(bytesTotal * (_percent.toFloat()/100f))} из $bytesTotalString"
    }

    fun startPreloading() {
        val r = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        r.duration = 750
        r.repeatCount = Animation.INFINITE
        r.interpolator = LinearInterpolator()

        scene.findViewById<ImageView>(R.id.update_page_preloading).startAnimation(r)
    }

    fun stopPreloading() {
        scene.findViewById<ImageView>(R.id.update_page_preloading).clearAnimation()
        scene.findViewById<ImageView>(R.id.update_page_preloading).visibility = View.GONE
    }

    private fun download(_url:String, _apkName:String = "pxupdate") {
        val con = URL(_url).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.doOutput = true

        var inputStream:InputStream? = null

        if (con.responseCode != HttpURLConnection.HTTP_OK)  {
            inputStream = con.errorStream
        } else  {
            inputStream = con.inputStream
        }

        val PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "$_apkName.apk"
        val fos = FileOutputStream(PATH)

        val buffer = ByteArray(1024)
        var len1 = 0
        while (inputStream.read(buffer).also { len1 = it } != -1) {
            fos.write(buffer, 0, len1)
        }
        fos.close()
        inputStream.close()
    }

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun checkStoragePermission():Boolean {
        var permission = ActivityCompat.checkSelfPermission(Main.main, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main.main, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            return false
        }
        permission = ActivityCompat.checkSelfPermission(Main.main, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main.main, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            return false
        }

        return true
    }
}
