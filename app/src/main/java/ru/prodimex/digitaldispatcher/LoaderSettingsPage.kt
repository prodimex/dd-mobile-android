package ru.prodimex.digitaldispatcher

class LoaderSettingsPage:LoaderAppController() {
    companion object {

    }
    init {
        init(R.layout.loader_settings_page)

        setOnClick(R.id.change_farm_index) {
            Main.setParam("tripFieldindex", "")
            switchTopage(Main.LOADER_ENTER_PAGE)
        }
        setOnClick(R.id.change_application_mode) {
            scene.showPage(Main.ROLE_SELECTOR)
        }
        setOnClick(R.id.clear_cache) {
            Main.setParam("driversInfoCache", "{}")
            PopupManager.showAlert("Кеш очищен.", "")
        }

        highlightButton(R.id.settings_button)
        highlightIcon(R.id.settings_ico)

    }

    override fun updateView() {
        super.updateView()
        var text = "Всего маяков: ${Beacons.beaconTransmitters.size}<br>"
        text += if(Beacons.scanStarted) "Сканирование запущено<br>" else "Сканирование не запущено<br>"
        text += "Код индекса хохяйства: ${Beacons.beaconFarmCode}<br>"
        setText(R.id.settings_debug_text, text)
    }
}