package org.bibletranslationtools.maui.jvm.ui.upload

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.BatchFileAccessor
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.io.BooksReader
import org.bibletranslationtools.maui.jvm.io.LanguagesReader
import org.bibletranslationtools.maui.jvm.io.ResourceTypesReader
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.onChange
import tornadofx.toObservable
import javax.inject.Inject

class UploadMediaViewModel : ViewModel() {

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var mediaMapper: MediaMapper

    private val batchDataStore: BatchDataStore by inject()

    private val mediaItems = observableListOf<MediaItem>()
    private val filteredMediaItems = FilteredList(mediaItems)
    val sortedMediaItems = SortedList(filteredMediaItems)

    val languages = observableListOf<String>()
    val resourceTypes = observableListOf<String>()
    val books = observableListOf<String>()
    val mediaExtensions = MediaExtension.values().toList().toObservable()
    val mediaQualities = MediaQuality.values().toList().toObservable()
    val groupings = Grouping.values().toList().toObservable()
    val statuses = FileStatus.values().toList().toObservable()

    private var batchFileAccessor: BatchFileAccessor? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        batchDataStore.activeBatchProperty.onChange {
            it?.let { batch ->
                batchFileAccessor = BatchFileAccessor(directoryProvider, batch)
            }
        }

        loadLanguages()
        loadResourceTypes()
        loadBooks()
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

    private fun loadLanguages() {
        LanguagesReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                languages.addAll(list)
            }
    }

    private fun loadResourceTypes() {
        ResourceTypesReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                resourceTypes.addAll(list)
            }
    }

    private fun loadBooks() {
        BooksReader().read()
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                books.addAll(list)
            }
    }
}