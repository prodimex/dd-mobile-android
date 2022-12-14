package ru.prodimex.digitaldispatcher.driver

import ru.prodimex.digitaldispatcher.Dict
import ru.prodimex.digitaldispatcher.Main
import ru.prodimex.digitaldispatcher.R
import ru.prodimex.digitaldispatcher.UserData

class DriverProfilePage: DriverAppController() {
    companion object {

    }
    init {
        init(R.layout.driver_profile_page)

        highlightIcon(R.id.profile_ico, R.color.text_yellow)
        highlightText(R.id.profile_text, R.color.text_yellow)

        setText(R.id.profile_page_surname, "Фамилия: <b>${UserData.surname}</b>")
        setText(R.id.profile_page_name, "Имя: <b>${UserData.name}</b>")
        setText(R.id.profile_page_patronymic, "Отчество: <b>${UserData.patronymic}</b>")
        setText(R.id.profile_page_phone, "Телефон: <b>${UserData.phoneFormatted}</b>")

        setText(R.id.profile_page_cars_numbers, "Номер АМ: <b>${UserData.carsNumbers}</b>")
        setText(R.id.profile_page_base_farm_name, "<b>${UserData.base_farm_name}</b>")
        setText(R.id.profile_page_base_farm_index, "Индекс хозяйства: <b>${UserData.base_farm_index}</b>")

        setOnClick(R.id.profile_page_exit) {
            Main.setParam("driverLoggedOuted", "")
            switchTopage(Dict.DRIVER_LOGIN_PAGE)
        }
    }
}