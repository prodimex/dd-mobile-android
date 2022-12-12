package ru.prodimex.digitaldispatcher

import org.altbeacon.beacon.Beacon

class LoaderSettingsPage:LoaderAppController() {
    companion object {

    }
    init {
        init(R.layout.loader_settings_page)

        setOnClick(R.id.change_farm_index) {
            Main.setParam("appEntered", "")
            switchTopage(Main.LOADER_ENTER_PAGE)
        }
        setOnClick(R.id.change_application_mode) {
            scene.showPage(Main.ROLE_SELECTOR)
        }
        setOnClick(R.id.clear_cache) {
            Main.setParam("driversInfoCache", "{}")
            PopupManager.showAlert("Кеш очищен.", "")
        }

        setOnClick(R.id.drop_drivers) {
            Beacons.killAllBeacons()
            dropDrivers()
            PopupManager.showAlert("Водители очищены.", "")
        }

        setOnClick(R.id.upload_logs) {

        }

        highlightButton(R.id.settings_button)
        highlightIcon(R.id.settings_ico)
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