package ru.prodimex.digitaldispatcher


import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class LoaderEnterPage:AppController() {
    companion object {

    }
    init {
        LoaderAppController.driversInfoCache = Gson().fromJson(Main.sharedPref.getString("driversInfoCache", "{}"),
            object: TypeToken<HashMap<String?, Any?>?>() {}.type)

        if(Main.getParam("tripFieldindex") != "") {
            startWithIndex(Main.getParam("tripFieldindex"))
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
            startWithIndex(farmIndexInput.text.toString())
        }
    }

    fun startWithIndex(_index:String) {
        UserData.tripFieldindex = _index
        Beacons.init()
        Beacons.startScan()

        Main.setParam("tripFieldindex", UserData.tripFieldindex)
        switchTopage(Main.LOADER_QUEUE_PAGE)
    }
}