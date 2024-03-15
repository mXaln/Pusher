package org.bibletranslationtools.maui.jvm.ui.mediacell

import javafx.scene.control.ListCell
import org.bibletranslationtools.maui.jvm.data.MediaItem

class MediaCell : ListCell<MediaItem>() {

    private val cellView = MediaView()

    override fun updateItem(item: MediaItem?, empty: Boolean) {
        super.updateItem(item, empty)

        graphic = if (empty) {
            null
        } else {
            cellView.mediaItemProperty.set(null)
            cellView.mediaItemProperty.set(item)
            cellView
        }
    }
}
