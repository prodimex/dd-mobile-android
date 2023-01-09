package ru.prodimex.digitaldispatcher.loader


import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.prodimex.digitaldispatcher.*


class LoaderEnterPage: AppController() {
    init {
        LoaderAppController.driversInfoCache = Gson().fromJson(
            Main.sharedPref.getString("driversInfoCache", "{}"),
            object: TypeToken<HashMap<String?, Any?>?>() {}.type)

        if(Main.getParam("tripFieldindex") != "" && Main.getParam("appEntered") != "") {
            runApp(Main.getParam("tripFieldindex"))
        } else {
            init(R.layout.loader_enter_page)
        }
    }

    override fun init(_layoutId: Int) {
        super.init(_layoutId)

        val farmIndexInput = scene.findViewById<EditText>(R.id.farm_index_input_field)
        farmIndexInput.filters = arrayOf(AllCaps(), LengthFilter(5))
        farmIndexInput.setText(Main.getParam("tripFieldindex"))
        vis(R.id.error_field, false)
        setOnClick(R.id.loader_enter_page_enter_button) {
            if(farmIndexInput.text.toString().length < 3) {
                vis(R.id.error_field, true)
                return@setOnClick
            }
            runApp(farmIndexInput.text.toString())
        }
    }

    fun runApp(_index:String) {
        UserData.tripFieldindex = _index
        Main.setParam("tripFieldindex", UserData.tripFieldindex)
        Main.setParam("appEntered", "true")

        Beacons.init()
        Beacons.startScan(Beacons.completeRawUUID(Dict.LOADER_ON_FIELD_ON_AIR))
        switchTopage(Dict.LOADER_QUEUE_PAGE)
    }
}