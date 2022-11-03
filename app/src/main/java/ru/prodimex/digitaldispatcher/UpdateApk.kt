package ru.prodimex.digitaldispatcher

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateApp(_apkUrl:String, _scene:UpdatePage): AsyncTask<String, Int, String>() {
    private var wakeLock:WakeLock? = null
    val apkUrl = _apkUrl
    val scene = _scene

    @SuppressLint("WakelockTimeout")
    override fun onPreExecute() {
        super.onPreExecute()
        val pm = Main.main.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        wakeLock!!.acquire()
    }

    override fun onProgressUpdate(vararg values:Int?) {
        super.onProgressUpdate(*values)
        values[0]?.let { scene.showDownloadProgress(it) }
        Main.log(values)
    }
    override fun doInBackground(vararg p0:String?):String {
        try {
            var url = URL(apkUrl)
            var con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()

            val fileLength:Int = con.contentLength
            var PATH = "${Environment.getExternalStorageDirectory().absolutePath}/Download"

            val file = File(PATH)
            file.mkdirs()
            val outputFile = File(PATH, "pxupdate.apk")
            if (outputFile.exists())
                outputFile.delete()

            var fos = FileOutputStream(outputFile)
            val input = con!!.inputStream
            val buffer = ByteArray(8192)
            var len1 = 0
            var total = 0
            while (input.read(buffer).also { len1 = it } != -1) {
                total += len1;
                if (fileLength > 0) {
                    var pr = (total * 100 / fileLength)
                    publishProgress(pr)
                }
                fos.write(buffer, 0, len1)
            }
            fos.close()
            con.disconnect()
            input.close()
            return "complete"
        } catch (e:Exception) {
            println(e.toString())
            return e.toString()
        }
    }
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        wakeLock!!.release()

        if (result != "complete") {
            Main.main.toastMe("Ошибка загрузки: $result")
        } else {
            Main.main.toastMe("Загрузка завершена")
            scene.scene.installUpdate()
        }
    }
}
