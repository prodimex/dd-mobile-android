package ru.prodimex.digitaldispatcher.loader

import android.widget.LinearLayout
import ru.prodimex.digitaldispatcher.Beacons
import ru.prodimex.digitaldispatcher.Dict
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.R

class LoaderArchiveListItem(_number:String, _shortCut:String, _time:String): ListItem(_number, _shortCut) {
    override var PAGE_ID = Dict.LOADER_CANCELLED_PAGE
    override var view: LinearLayout = Main.main.layoutInflater.inflate(R.layout.archive_driver_list_item, null) as LinearLayout
    val time = _time
    var driverState = Dict.YOU_LOADED_GO_TO_FACTORY

    override val TAG = "LOADER ARCHIVE LIST ITEM"
    init {

    }

    override fun receiveUIIDs(_uuid: String) {
        Main.log(_uuid, TAG)
        if(driverState == Dict.YOU_LOADED_GO_TO_FACTORY) {
            if(_uuid.indexOf(Dict.IM_LOADED_AND_GO_TO_FACTORY) == 0) {
                Beacons.killBeacon(Beacons.completeRawUUID("$driverState$shortCut"))
                driverState = Dict.SILENCE
            }
        }
    }
}