package ru.prodimex.digitaldispatcher

import kotlin.math.abs

class Dictionary {
    companion object {
        const val NEW_ITEM = "00"
        const val CONNECT_TO_LOADER_SIGNAL = "01"
        const val GIVE_SHORTCUT_TO_DRIVER_AND_RETURN_DRIVER_INFO = "02"
        const val SEND_DRIVER_INFO_TO_LOADER = "03"
        const val STOP_SENDING_DATA_AND_WAIT = "04"
        const val IM_WAITING_FOR_LOADER_SIGNAL = "05"
        const val SILENCE = "06"
        const val YOU_NEED_TO_RECONNECT = "07"
        const val RECONNECT_TO_LOADER = "08"
        const val GO_TO_LOADING = "09"
        const val IM_ON_LOADING = "0a"

        const val GIVE_SHORTCUT_AND_WAIT_FOR_LOADER_SIGNAL = "0b"
        const val DISMISS_FROM_QUEUE = "0c"
        const val IM_DISMISSED_BUT_ON_FIELD = "0d"

        val units = arrayOf("Кбайт", "Мбайт", "Гбайт", "Тбайт", "Пбайт", "Эбайт", "Збайт", "Ибайт")

        fun fileSizeAsString(_bytes:Float, _dp:Int = 2):String {
            var bytes = _bytes
            val thresh = 1024
            if (abs(bytes) < thresh)
                return "$bytes байт"

            var i = -1
            val r = 10 * _dp * _dp

            do {
                bytes /= thresh
                i++
            } while (Math.round(abs(bytes) * r) / r >= thresh && i < units.size - 1)

            return "${"%.2f".format(bytes)} ${units[i]}"
        }

        private val farmIndexChars = "0123456789АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ -|"
        val farmIndexCharsByNum:HashMap<String, String> = hashMapOf()
        val farmIndexNumByChar:HashMap<String, String> = hashMapOf()
        val farmIndexHexByChar:HashMap<String, String> = hashMapOf()
        val farmIndexCharByHex:HashMap<String, String> = hashMapOf()

        /*val carNumberChars = mapOf(
            "0" to "00", "1" to "01", "2" to "02", "3" to "03", "4" to "04", "5" to "05", "6" to "06",
            "7" to "07", "8" to "08", "9" to "09",
            "А" to "0A", "В" to "0B", "Е" to "0C", "К" to "0D", "М" to "0E", "Н" to "0F", "О" to "10",
            "Р" to "11", "С" to "12", "Т" to "13", "У" to "14", "Х" to "15", " " to "16"
        )*/
        private val carNumbersChars = "0123456789АВЕКМНОРСТУХ "
        val carNumberCharsByHex:HashMap<String, String> = hashMapOf()
        val carNumberHexsByChar:HashMap<String, String> = hashMapOf()

        fun init() {
            var i = 10
            farmIndexChars.forEach {
                farmIndexCharsByNum[i.toString()] = "$it"
                farmIndexNumByChar["$it"] = i.toString()
                var hex = Integer.toHexString(i).lowercase()
                if(hex.length == 1) hex = "0$hex"
                farmIndexCharByHex["${hex}"] = "$it"
                farmIndexHexByChar["$it"] = "${hex}"
                i ++
            }
            i = 0
            Integer.toHexString(10)
            Integer.toHexString(11)
            Integer.toHexString(12)
            carNumbersChars.forEach {
                var hex = Integer.toHexString(i).lowercase()
                if(hex.length == 1) hex = "0$hex"
                carNumberHexsByChar[it.toString()] = hex
                carNumberCharsByHex[hex] = it.toString()
                i++
            }
            Main.log(farmIndexCharsByNum)
            Main.log(farmIndexNumByChar)
            Main.log(farmIndexHexByChar)
            Main.log("-----------------")

            Main.log(carNumberHexsByChar)
            Main.log(carNumberCharsByHex)
        }

        val daysOfWeek = arrayOf("", "пн", "вт", "ср", "чт", "пт", "сб", "вс", "", "", "", "", "", "")
        val monts = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "февраля", "", "")
        val statusColors = hashMapOf<Int, Int>(
            1 to R.color.status_text_color_1,
            2 to R.color.status_text_color_2,
            3 to R.color.status_text_color_3,
            4 to R.color.status_text_color_4,
            5 to R.color.status_text_color_5,
            6 to R.color.status_text_color_6,
            7 to R.color.status_text_color_7,
        )
    }
}