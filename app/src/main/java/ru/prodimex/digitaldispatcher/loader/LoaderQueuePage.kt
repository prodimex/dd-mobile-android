package ru.prodimex.digitaldispatcher.loader

import ru.prodimex.digitaldispatcher.R

class LoaderQueuePage: LoaderAppController() {
    companion object {

    }

    init {
        init(R.layout.loader_queue_page)

        highlightButton(R.id.queue_button)
        highlightText(R.id.queue_text)
        highlightText(R.id.queue_counter_text)
    }
}