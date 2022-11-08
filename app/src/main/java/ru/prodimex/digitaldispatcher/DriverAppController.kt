package ru.prodimex.digitaldispatcher

import org.altbeacon.beacon.Beacon

open class DriverAppController:AppController() {

    override fun init(_layoutId: Int) {
        super.init(_layoutId)
        Beacons.controller = this

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

    override fun scanObserver(beacons: Collection<Beacon>) {
        super.scanObserver(beacons)
        Main.log("LOLOLOLOLOLOLO")
    }
}