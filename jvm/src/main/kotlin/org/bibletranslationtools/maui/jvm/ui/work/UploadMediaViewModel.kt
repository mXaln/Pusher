package org.bibletranslationtools.maui.jvm.ui.work

import javafx.collections.transformation.FilteredList
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.ViewModel
import tornadofx.observableListOf

class UploadMediaViewModel : ViewModel() {

    val mediaItems = observableListOf<MediaItem>()
    val filteredMediaItems = FilteredList(mediaItems)

    fun saveBatch() {
        println("Saved...")
        fire(AppSaveDoneEvent())
    }

    fun onDock() {
    }

    fun onUndock() {
        saveBatch()
    }
}