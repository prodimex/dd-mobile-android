package ru.prodimex.digitaldispatcher

class LoaderCancelledPage:LoaderAppController() {
    companion object {

    }
    init {
        init(R.layout.loader_queue_page)

        highlightButton(R.id.cancelled_button)
        highlightText(R.id.cancelled_text)
    }
}