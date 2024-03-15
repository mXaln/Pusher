package org.bibletranslationtools.maui.jvm.ui.upload

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import org.bibletranslationtools.maui.common.BatchFileAccessor
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.jvm.data.FileStatusFilter
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.io.BooksReader
import org.bibletranslationtools.maui.jvm.io.LanguagesReader
import org.bibletranslationtools.maui.jvm.io.ResourceTypesReader
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.data.MediaItem
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.*
import javax.inject.Inject

class UploadMediaViewModel : ViewModel() {

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var mediaMapper: MediaMapper

    private val batchDataStore: BatchDataStore by inject()

    private val mediaItems = observableListOf<MediaItem>()
    val tableMediaItems = SortedFilteredList(mediaItems)

    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    val languages = observableListOf<String>()
    val resourceTypes = observableListOf<String>()
    val books = observableListOf<String>()
    val mediaExtensions = MediaExtension.values().toList().toObservable()
    val mediaQualities = MediaQuality.values().toList().toObservable()
    val groupings = Grouping.values().toList().toObservable()
    val statusFilter = FileStatusFilter.values().toList().toObservable()

    private var batchFileAccessor: BatchFileAccessor? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        batchDataStore.activeBatchProperty.onChange {
            it?.let { batch ->
                batchFileAccessor = BatchFileAccessor(directoryProvider, batch)
            }
        }

        uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)

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
        mediaItems.forEach {
            println(it.file.name)
            println(it.language)
            println(it.resourceType)
            println(it.book)
            println(it.chapter)
            println(it.mediaExtension)
            println(it.mediaQuality)
            println(it.grouping)
            println(it.status)
            println(it.selected)
            println("-------------------------")
        }
        fire(AppSaveDoneEvent())
    }

    fun viewUploadedFiles() {
        println("*** view uploaded files triggered ***")
    }

    fun exportCsv() {
        println("*** export CSV triggered ***")
    }

    fun removeSelected() {
        val toRemove = mediaItems.filter { it.selected }
        mediaItems.removeAll(toRemove)
    }

    fun verify() {
        println("*** verify triggered ***")
    }

    fun upload() {
        println("*** upload triggered ***")
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