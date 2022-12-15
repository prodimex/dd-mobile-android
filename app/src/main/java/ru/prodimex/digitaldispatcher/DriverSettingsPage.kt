package ru.prodimex.digitaldispatcher

import org.altbeacon.beacon.Beacon
import java.text.SimpleDateFormat
import java.util.*

class DriverSettingsPage:DriverAppController() {
    companion object {

    }
    init {
        init(R.layout.driver_settings_page)

        highlightIcon(R.id.settings_ico, R.color.text_yellow)
        highlightText(R.id.settings_text, R.color.text_yellow)

        setOnClick(R.id.set_current_trip_to_loaded) {

            Main.log("------------------------------------")
            var d = Date()
            var timeZone = SimpleDateFormat("z", Locale("en")).format(d).replace(":", "")
            var date = SimpleDateFormat("EEE MMM d yyy HH:mm:ss", Locale("en")).format(d) + " $timeZone"
            Main.log(date)
            Main.log("Mon Oct 31 2022 08:38:22 GMT+0300")
            Main.log("------------------------------------")
            /*Main.log(SimpleDateFormat("EEE MMM d yyy HH:mm:ss zz", Locale("en")).format(d))
            Main.log("Mon Oct 31 2022 08:38:22 GMT+0300")
            Main.log("${d.day} ${d.month} ${d.date} ${d.year} ${d.hours}:${d.minutes}:${d.seconds} ${d.timezoneOffset} ")*/

            /*HTTPRequest("trips/logs-new",
                _args = hashMapOf("id" to UserData.tripId, "status" to "loaded", "loggingTime" to "Mon Oct 31 2022 08:38:22 GMT+0300"),
                _callback = fun(_response:HashMap<String, Any>) {
                    if(_response["result"] == "error") {
                        showErrorByCode(_response)
                        return
                    }
                    hideError()
                    Main.log(_response)
                }).execute()

             */
        }

        setOnClick(R.id.set_current_trip_to_discarded) {

        }

        setOnClick(R.id.show_sample_alert) {
            PopupManager.showAlert("Не протестировано плохое соединение, нет возможности выбрать машину, выбирается всегда первая машина, не обрабатывается вход с другого устройства, нет возможности запуска без интернета", "Осталось доделать")
        }
    }
    override fun scanObserver(beacons: Collection<Beacon>) {
        super.scanObserver(beacons)
        updateView()
    }

    override fun updateView() {
        super.updateView()
        var text = "Всего маяков: ${Beacons.beaconTransmitters.size}<br>"
        Beacons.beaconTransmitters.forEach {
            text += "${it.key}<br>"
        }
        text += "маяков в эфире: $beaconsOnAir <br>"
        text += if(Beacons.scanStarted) "Сканирование запущено<br>" else "Сканирование не запущено<br>"
        text += "Код индекса хохяйства: ${Beacons.beaconFarmCode}<br>"
        setText(R.id.settings_debug_text, text)
    }
}