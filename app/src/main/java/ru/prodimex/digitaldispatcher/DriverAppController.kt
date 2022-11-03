package ru.prodimex.digitaldispatcher

open class DriverAppController:AppController() {

    override fun init(_layoutId: Int) {
        super.init(_layoutId)

        setOnClick(R.id.profile_button) {
            switchTopage(Main.PROFILE_PAGE)
        }

        setOnClick(R.id.trip_button) {
            switchTopage(Main.TRIP_PAGE)
        }

        setOnClick(R.id.settings_button) {
            switchTopage(Main.DRIVER_SETTINGS_PAGE)
        }
    }
}