package org.bibletranslationtools.maui.jvm.ui.upload

import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.BatchFileAccessor
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.onChange
import javax.inject.Inject

class UploadMediaViewModel : ViewModel() {

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var mediaMapper: MediaMapper

    private val batchDataStore: BatchDataStore by inject()

    private val mediaItems = observableListOf<MediaItem>()
    private val filteredMediaItems = FilteredList(mediaItems)
    val sortedMediaItems = SortedList(filteredMediaItems)

    private var batchFileAccessor: BatchFileAccessor? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        batchDataStore.activeBatchProperty.onChange {
            it?.let { batch ->
                batchFileAccessor = BatchFileAccessor(directoryProvider, batch)
            }
        }
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

        batchDataStore.activeBatchProperty.value
            ?.media
            ?.value
            ?.map(mediaMapper::fromEntity)
            ?.forEach(mediaItems::add)
    }
}