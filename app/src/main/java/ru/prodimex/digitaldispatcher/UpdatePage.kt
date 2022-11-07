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
    companion object {

    }

    init {
        showLayout(R.layout.update_page)


        scene.findViewById<LinearLayout>(R.id.update_layout_content).visibility = View.GONE
        scene.findViewById<LinearLayout>(R.id.update_download_progress).visibility = View.GONE
        startPreloading()

        HTTPRequest.nodeServerUrl = AppConfig.UPDATE_URL
        HTTPRequest("", hashMapOf("ver" to BuildConfig.BUILD_TYPE), _requestMethod = "GET", _callback = fun(_response:HashMap<String, Any>) {
            Main.log(_response)
            /*if(_response.contains("error")) {


                return
            }*/

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
        bytesTotalString = "${Dictionary.fileSizeAsString(bytesTotal)}"
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
            var updater = UpdateApp(_updateData["apkUrl"].toString(), this)
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
            "${Dictionary.fileSizeAsString(bytesTotal * (_percent.toFloat()/100f))} из $bytesTotalString"

        Main.log(_percent)
        Main.log(  "$fullProgressLineWidth ${(fullProgressLineWidth * (_percent/100))}")
        //scene.findViewById<LinearLayout>(R.id.update_download_progress_bg).width =

        //R.id.update_download_progress
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

    //@Throws(Exception::class)
    //private fun download(param: DownloadParams, res: DownloadResult) {
    private fun download(_url:String, _apkName:String = "pxupdate") {
        val con = URL(_url).openConnection() as HttpURLConnection
        con.requestMethod = "GET"
        con.doOutput = true
        //c.connect()
        var inputStream:InputStream? = null

        if (con.responseCode != HttpURLConnection.HTTP_OK)  {
            inputStream = con.errorStream
        } else  {
            inputStream = con.inputStream
        }

        val PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "$_apkName.apk"
        Main.log(PATH)
        val fos = FileOutputStream(PATH)

        val buffer = ByteArray(1024)
        var len1 = 0
        while (inputStream.read(buffer).also { len1 = it } != -1) {
            Main.log(buffer.size)
            fos.write(buffer, 0, len1)
        }
        fos.close()
        inputStream.close()
        /*var connection:HttpURLConnection? = null
        try {
            connection = URL(_url).openConnection() as HttpURLConnection
            connection!!.connect()

            var statusCode: Int = connection.getResponseCode()
            var lengthOfFile: Long = getContentLength(connection)
            if (statusCode == HttpURLConnection.HTTP_OK) {

            }
            /*val iterator: ReadableMapKeySetIterator = param.headers.keySetIterator()
            while (iterator.hasNextKey()) {
                val key: String = iterator.nextKey()
                val value: String = param.headers.getString(key)
                connection.setRequestProperty(key, value)
            }
            connection.setConnectTimeout(param.connectionTimeout)
            connection.setReadTimeout(param.readTimeout)*/


            val isRedirect = statusCode != HttpURLConnection.HTTP_OK &&
                    (statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == 307 || statusCode == 308)
            if (isRedirect) {
                val redirectURL: String = connection.getHeaderField("Location")
                connection.disconnect()
                connection = URL(redirectURL).openConnection() as HttpURLConnection
                connection.setConnectTimeout(5000)
                connection.connect()
                statusCode = connection.getResponseCode()
                lengthOfFile = getContentLength(connection)
            }
            if (statusCode >= 200 && statusCode < 300) {
                val headers: Map<String, List<String>> = connection.getHeaderFields()
                val headersFlat: MutableMap<String, String> = HashMap()
                for ((headerKey, value): Map.Entry<String, List<String>> in headers) {
                    val valueKey: String = value[0]
                    if (headerKey != null && valueKey != null) {
                        headersFlat[headerKey] = valueKey
                    }
                }
                if (mParam.onDownloadBegin != null) {
                    mParam.onDownloadBegin.onDownloadBegin(statusCode, lengthOfFile, headersFlat)
                }
                input = BufferedInputStream(connection.getInputStream(), 8 * 1024)
                output = FileOutputStream(param.dest)
                val data = ByteArray(8 * 1024)
                var total: Long = 0
                var count: Int
                var lastProgressValue = 0.0
                var lastProgressEmitTimestamp: Long = 0
                val hasProgressCallback = mParam.onDownloadProgress != null
                while (input.read(data).also { count = it } != -1) {
                    if (mAbort.get()) throw Exception("Download has been aborted")
                    total += count.toLong()
                    if (hasProgressCallback) {
                        if (param.progressInterval > 0) {
                            val timestamp = System.currentTimeMillis()
                            if (timestamp - lastProgressEmitTimestamp > param.progressInterval) {
                                lastProgressEmitTimestamp = timestamp
                                publishProgress(longArrayOf(lengthOfFile, total))
                            }
                        } else if (param.progressDivider <= 0) {
                            publishProgress(longArrayOf(lengthOfFile, total))
                        } else {
                            val progress =
                                Math.round(total.toDouble() * 100 / lengthOfFile).toDouble()
                            if (progress % param.progressDivider === 0) {
                                if (progress != lastProgressValue || total == lengthOfFile) {
                                    Log.d("Downloader", "EMIT: $progress, TOTAL:$total")
                                    lastProgressValue = progress
                                    publishProgress(longArrayOf(lengthOfFile, total))
                                }
                            }
                        }
                    }
                    output.write(data, 0, count)
                }
                output.flush()
                res.bytesWritten = total
            }
            res.statusCode = statusCode*/
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
