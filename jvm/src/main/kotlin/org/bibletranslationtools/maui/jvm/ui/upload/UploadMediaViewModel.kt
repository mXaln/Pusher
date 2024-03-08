package org.bibletranslationtools.maui.jvm.ui.upload

import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.ViewModel
import tornadofx.observableListOf
import java.io.File
import javax.inject.Inject

class UploadMediaViewModel : ViewModel() {

    @Inject lateinit var mediaMapper: MediaMapper

    private val mediaItems = observableListOf<MediaItem>()
    private val filteredMediaItems = FilteredList(mediaItems)
    val sortedMediaItems = SortedList(filteredMediaItems)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDock() {
        loadMediaItems()
    }

    fun onUndock() {
        saveBatch()
    }

    fun saveBatch() {
        println("Saved...")
        fire(AppSaveDoneEvent())
    }

    private fun loadMediaItems() {
        mediaItems.clear()
        listOf(
            Media(File("example1.wav"), "en"),
            Media(File("example2.wav")),
            Media(File("example3.wav")),
            Media(File("example4.wav"), "fr", "udb", "psa", 5, grouping = Grouping.CHAPTER),
            Media(File("example5.wav")),
            Media(File("example6.wav"), status = FileStatus.REJECTED, statusMessage = "An error occurred."),
        )
            .map(mediaMapper::fromEntity)
            .forEach(mediaItems::add)

        //mediaItems.clear()
    }
}