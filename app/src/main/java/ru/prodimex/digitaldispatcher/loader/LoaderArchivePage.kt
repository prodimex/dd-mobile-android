package ru.prodimex.digitaldispatcher.loader

import ru.prodimex.digitaldispatcher.R

class LoaderArchivePage: LoaderAppController() {
    companion object {

    }
    init {
        init(R.layout.loader_queue_page)

        highlightButton(R.id.cancelled_button)
        highlightText(R.id.cancelled_text)
    }
}