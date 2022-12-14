package ru.prodimex.digitaldispatcher

import kotlin.math.abs

class Dict {
    companion object {
        const val TRIP_PAGE = "TRIP_PAGE"
        const val PROFILE_PAGE = "PROFILE_PAGE"
        const val DRIVER_LOGIN_PAGE = "DRIVER_LOGIN_PAGE"
        const val DRIVER_SETTINGS_PAGE = "DRIVER_SETTINGS_PAGE"

        const val BEACON_SCANNER_PAGE = "BEACON_SCANNER_PAGE"

        const val LOADER_ENTER_PAGE = "LOADER_ENTER_PAGE"
        const val LOADER_QUEUE_PAGE = "LOADER_QUEUE_PAGE"
        const val LOADER_LOADED_PAGE = "LOADER_LOADED_PAGE"
        const val LOADER_CANCELLED_PAGE = "LOADER_CANCELLED_PAGE"
        const val LOADER_SETTINGS_PAGE = "LOADER_SETTINGS_PAGE"

        const val UPDATE_PAGE = "UPDATE_PAGE"

        const val ROLE_SELECTOR = "ROLE_SELECTOR"

        var signalsLangs = HashMap<String, String>()

        const val NEW_ITEM = "00"
        const val CONNECT_TO_LOADER_SIGNAL = "01"
        const val GIVE_ME_DRIVER_INFO = "02"
        const val SEND_DRIVER_INFO_TO_LOADER = "03"
        const val STOP_SENDING_DATA_AND_WAIT = "04"
        const val IM_WAITING_FOR_LOADER_SIGNAL = "05"
        const val SILENCE = "06"
        const val YOU_NEED_TO_RECONNECT = "07"
        const val RECONNECT_TO_LOADER = "08"
        const val GO_TO_LOADING = "09"
        const val IM_ON_LOADING = "0a"

        const val I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL = "0b"//todo refactor
        const val DISMISS_FROM_QUEUE = "0c"
        const val IM_DISMISSED_BUT_ON_FIELD = "0d"

        const val DRIVER_ON_FIELD_ON_AIR = "0e"
        const val LOADER_ON_FIELD_ON_AIR = "0f"
        const val SCANNER_ON_FIELD_ON_AIR = "10"
        const val RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE = "11"

        const val GO_RETURN_TO_QUEUE = "12"
        const val YOU_LOADED_GO_TO_FACTORY = "13"
        const val IM_LOADED_AND_GO_TO_FACTORY = "14"

        const val RECONNECT_TO_LOADER_AS_DISMISSED = "15"

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

        private const val farmIndexChars = "0123456789АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯABCDEFGHIJKLMNOPQRSTUVWXYZ -|"
        val farmIndexCharsByNum:HashMap<String, String> = hashMapOf()
        val farmIndexNumByChar:HashMap<String, String> = hashMapOf()
        val farmIndexHexByChar:HashMap<String, String> = hashMapOf()
        val farmIndexCharByHex:HashMap<String, String> = hashMapOf()

        private const val carNumbersChars = "0123456789АВЕКМНОРСТУХ "
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

            signalsLangs[NEW_ITEM] = ""
            signalsLangs[CONNECT_TO_LOADER_SIGNAL] = "Водитель: подключи меня"
            signalsLangs[GIVE_ME_DRIVER_INFO] = "Погрузчик: получи шорткат и вышли мне инфу о себе"
            signalsLangs[SEND_DRIVER_INFO_TO_LOADER] = "Водитель: передаю информацию о себе"
            signalsLangs[STOP_SENDING_DATA_AND_WAIT] = "Погрузчик: я получил инфу, жди"
            signalsLangs[IM_WAITING_FOR_LOADER_SIGNAL] = "Водитель: жду сигнала"
            signalsLangs[SILENCE] = "Погрузчик: молчание"
            signalsLangs[YOU_NEED_TO_RECONNECT] = "Погрузчик: водитель переподключиьс"
            signalsLangs[RECONNECT_TO_LOADER] = "Водитель: переподключаюсь"
            signalsLangs[GO_TO_LOADING] = "Погрузчик: отправляйся на погрузку"
            signalsLangs[IM_ON_LOADING] = "Водитель: я на погрузке"
            signalsLangs[I_KNOW_YOU_WAIT_FOR_LOADER_SIGNAL] = "Погрузчик: я тебя узнал, жди сигнала"
            signalsLangs[DISMISS_FROM_QUEUE] = "Погрузчик: отклоняю погрузку"
            signalsLangs[IM_DISMISSED_BUT_ON_FIELD] = "Водитель: меня отклонили"

            signalsLangs[DRIVER_ON_FIELD_ON_AIR] = "Водитель: в эфире"
            signalsLangs[LOADER_ON_FIELD_ON_AIR] = "Погрузчик: в эфире"
            signalsLangs[SCANNER_ON_FIELD_ON_AIR] = "Сканер: в эфире"

            signalsLangs[RECONNECT_TO_LOADER_IN_TO_LOADING_QUEUE] = "Водитель: переподключись в очередь на погрузку"
            signalsLangs[GO_RETURN_TO_QUEUE] = "Водитель: возвращаюсь в очередь на погрузку"

            signalsLangs[YOU_LOADED_GO_TO_FACTORY] = "Погрузчик: погрузка завершена, следуй на фабрику"
            signalsLangs[IM_LOADED_AND_GO_TO_FACTORY] = "Водитель: я загружен, возвращаюсь на фабрику"
            signalsLangs[RECONNECT_TO_LOADER_AS_DISMISSED] = "Водитель: переподключаюсь как отклонённый"
        }

        val daysOfWeek = arrayOf("", "пн", "вт", "ср", "чт", "пт", "сб", "вс", "", "", "", "", "", "")
        val monts = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря", "", "")
        val statusColors = hashMapOf<Int, Int>(
            1 to R.color.status_text_color_1,
            2 to R.color.status_text_color_2,
            3 to R.color.status_text_color_3,
            4 to R.color.status_text_color_4,
            5 to R.color.status_text_color_5,
            6 to R.color.status_text_color_6,
            7 to R.color.status_text_color_7,
        )

        fun getErrorByCode(_code:String):String {
            when(_code) {
                "403" -> return "Неверный пароль."
                "400" -> return "Пользователь с таким номером\nтелефона не найден."
                "401" -> return "Обнаружен вход\nс другого устройства."
                "888" -> return "Интернет выключен.\nПроверьте Wi-Fi и Мобильные данные"
                "999" -> return "Ошибка связи."
                else -> return "Ошибка связи. ($_code)"
            }
        }
    }
}