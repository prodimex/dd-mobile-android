package ru.prodimex.digitaldispatcher

class LoaderLoadedPage:LoaderAppController() {
    companion object {

    }
    init {
        init(R.layout.loader_queue_page)

        highlightButton(R.id.loaded_button)
        highlightText(R.id.loaded_text)
        highlightText(R.id.loaded_counter_text)
    }
}