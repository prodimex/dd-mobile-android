package ru.prodimex.digitaldispatcher.driver

import org.altbeacon.beacon.Beacon
import ru.prodimex.digitaldispatcher.Beacons
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.uitools.PopupManager
import ru.prodimex.digitaldispatcher.R
import java.text.SimpleDateFormat
import java.util.*

class DriverSettingsPage: DriverAppController() {
    companion object {

    }
    init {
        init(R.layout.driver_settings_page)

        highlightIcon(R.id.settings_ico, R.color.text_yellow)
        highlightText(R.id.settings_text, R.color.text_yellow)

        setOnClick(R.id.set_current_trip_to_loaded) {

        }

        setOnClick(R.id.set_current_trip_to_discarded) {

        }

        setOnClick(R.id.show_sample_alert) {
            PopupManager.showAlert(
                "Не протестировано плохое соединение, нет возможности выбрать машину, выбирается всегда первая машина, не обрабатывается вход с другого устройства, нет возможности запуска без интернета",
                "Осталось доделать"
            )
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