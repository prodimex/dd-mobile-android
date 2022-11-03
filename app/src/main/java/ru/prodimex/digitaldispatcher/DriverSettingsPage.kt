package ru.prodimex.digitaldispatcher

class DriverSettingsPage:DriverAppController() {
    companion object {

    }
    init {
        init(R.layout.driver_settings_page)

        highlightIcon(R.id.settings_ico, R.color.text_yellow)
        highlightText(R.id.settings_text, R.color.text_yellow)

        var text = "Всего маяков: ${Beacons.beaconTransmitters.size}<br>"
        text += if(Beacons.scanStarted) "Сканирование запущено<br>" else "Сканирование не запущено<br>"
        setText(R.id.settings_debug_text, text)

        setOnClick(R.id.set_current_trip_to_loaded) {
            HTTPRequest("trips/logs-new",
                _args = hashMapOf("id" to UserData.tripId, "status" to "loaded", "loggingTime" to "Mon Oct 31 2022 08:38:22 GMT+0300"),
                _callback = fun(_resp:HashMap<String, Any>) {
                    Main.log(_resp)
                }).execute()
        }

        setOnClick(R.id.set_current_trip_to_discarded) {

        }
        setOnClick(R.id.show_sample_alert) {
            PopupManager.showAlert("Не протестировано плохое соединение, нет возможности выбрать машину, выбирается всегда первая машина, не обрабатывается вход с другого устройства, нет возможности запуска без интернета", "Осталось доделать")
        }
    }
}