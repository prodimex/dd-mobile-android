package ru.prodimex.digitaldispatcher

import android.telephony.PhoneNumberUtils
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserData {
    companion object {
        var data = HashMap<String, Any>()
        var driver = LinkedTreeMap<String, Any>()
        var profile = LinkedTreeMap<String, Any>()
        var base_farm = LinkedTreeMap<String, Any>()
        var cars = ArrayList<LinkedTreeMap<String, String>>()

        var surname = ""
        var name = ""
        var patronymic = ""
        var fullName = ""
        var shortName = ""
        var phone = ""
        var phoneFormatted = ""
        var user_id = ""
        var carsNumbers = ""

        var base_farm_name = ""
        var base_farm_index = ""

        var tripStatuses = LinkedTreeMap<Int, LinkedTreeMap<String, String>>()

        var loadingCargoStation = LinkedTreeMap<String, Any>()
        var tripFieldindex = ""

        fun collectData(_data:HashMap<String, Any>) {
            data = _data

            var str = Gson().toJson(_data).toString()
            Main.setParam("userData", str)

            driver = toNode(data["driver"])
            profile = toNode(driver["profile"])
            base_farm = toNode(driver["base_farm"])
            cars = driver["cars"] as ArrayList<LinkedTreeMap<String, String>>

            surname = profile["surname"].toString()
            name = profile["name"].toString()
            patronymic = profile["patronymic"].toString()
            fullName = profile["fullName"].toString()
            shortName = profile["shortName"].toString()
            phone = driver["phone"].toString()
            phoneFormatted = "+${PhoneNumberUtils.formatNumber(phone, Locale.getDefault().country)}"
            user_id = profile["user_id"].toString()

            base_farm_name = base_farm["name"].toString()
            base_farm_index = base_farm["alternative_name"].toString()
            tripFieldindex = base_farm["alternative_name"].toString()

            carsNumbers = ""
            if(cars.size > 0) {
                cars.forEach{
                    if(carsNumbers != "")
                        carsNumbers += ", "
                    carsNumbers += it["number"]!!
                }
            } else {
                carsNumbers = "АМ не указан"
            }

            val settings = toNode(data["settings"])
            val tripStatusesRow = settings["tripStatuses"] as ArrayList<LinkedTreeMap<String, String>>
            tripStatusesRow.forEach {
               tripStatuses[it["id"].toString().toFloat().toInt()] = it
            }
        }

        var tripData:HashMap<String, Any>? = null
        var currentTrip:LinkedTreeMap<String, Any>? = null
        var currentTripStatus = 0
        var tripId = ""
        var dq_id = ""

        fun collectTripData(_data:HashMap<String, Any>) {
            if (_data.contains("error"))
                return

            var str = Gson().toJson(_data).toString()
            Main.setParam("tripData", str)

            tripData = _data
            if (_data["timeslot"]!!::class.simpleName == "ArrayList" && (_data["timeslot"]!! as ArrayList<Any>).size == 0) {
                //Main.log(_data)
                //Main.log((_data["timeslot"]!! as ArrayList<Any>).size)
                currentTrip = null
                return
            }
            currentTrip = toNode(_data["timeslot"])
            currentTripStatus = currentTrip!!["status_id"].toString().toFloat().toInt()

            tripId = currentTrip!!["id"].toString().toFloat().toInt().toString()
            dq_id = currentTrip!!["dq_id"].toString()


            loadingCargoStation = toNode(currentTrip!!["loading_cargo_station"])
            tripFieldindex = toNode(loadingCargoStation["farm"])["alternative_name"] as String
            //Main.log(data)
        }

        fun toNode(_node:Any?):LinkedTreeMap<String, Any> {
            return _node as LinkedTreeMap<String, Any>
        }
    }
}