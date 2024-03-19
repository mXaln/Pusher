package org.bibletranslationtools.maui.jvm.ui.upload

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.BatchFileAccessor
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.data.MediaQuality
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.common.usecases.batch.UpdateBatch
import org.bibletranslationtools.maui.jvm.ListenerDisposer
import org.bibletranslationtools.maui.jvm.data.FileStatusFilter
import org.bibletranslationtools.maui.jvm.data.MediaItem
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.io.BooksReader
import org.bibletranslationtools.maui.jvm.io.LanguagesReader
import org.bibletranslationtools.maui.jvm.io.ResourceTypesReader
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.onChangeWithDisposer
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import org.slf4j.LoggerFactory
import tornadofx.*
import javax.inject.Inject


class UploadMediaViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var mediaMapper: MediaMapper
    @Inject lateinit var updateBatch: UpdateBatch

    private val batchDataStore: BatchDataStore by inject()

    private val mediaItems = observableListOf<MediaItem> {
        arrayOf(
            it.selectedProperty,
            it.languageProperty,
            it.resourceTypeProperty,
            it.bookProperty,
            it.chapterProperty,
            it.mediaExtensionProperty,
            it.mediaQualityProperty,
            it.groupingProperty,
            it.statusProperty,
            it.statusMessageProperty
        )
    }
    val tableMediaItems = SortedFilteredList(mediaItems)

    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()
    val activeBatchProperty = SimpleObjectProperty<Batch>()
    val appTitleProperty = SimpleStringProperty()
    val uploadTargets = observableListOf<UploadTarget>()
    val batchNameProperty = SimpleStringProperty()

    val languages = observableListOf<String>()
    val resourceTypes = observableListOf<String>()
    val books = observableListOf<String>()
    val mediaExtensions = MediaExtension.values().toList().toObservable()
    val mediaQualities = MediaQuality.values().toList().toObservable()
    val groupings = Grouping.values().toList().toObservable()
    val statusFilter = FileStatusFilter.values().toList().toObservable()

    val shouldSaveProperty = SimpleBooleanProperty()

    private val listeners = mutableListOf<ListenerDisposer>()
    private var batchFileAccessor: BatchFileAccessor? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        batchDataStore.activeBatchProperty.onChangeAndDoNow {
            it?.let { batch ->
                //batchFileAccessor = BatchFileAccessor(directoryProvider, batch)
                batchNameProperty.set(batch.name)
            }
        }

        uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
        activeBatchProperty.bind(batchDataStore.activeBatchProperty)
        appTitleProperty.bind(batchDataStore.appTitleProperty)

        uploadTargets.bind(batchDataStore.uploadTargets) { it }

        batchNameProperty.onChange {
            it?.let { shouldSaveProperty.set(true) }
        }

        loadLanguages()
        loadResourceTypes()
        loadBooks()
    }

    fun onDock() {
        shouldSaveProperty.set(false)
        loadMediaItems()
    }

    fun onUndock() {
        if (shouldSaveProperty.value) {
            saveBatch(true)
        } else {
            mediaItems.clear()
        }
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    fun saveBatch(clearMedia: Boolean = false) {
        val newBatch = activeBatchProperty.value.copy(
            name = batchNameProperty.value,
            media = lazy {
                mediaItems.map(mediaMapper::toEntity)
            }
        )
        updateBatch.edit(newBatch)
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error in saveBatch", it)
            }
            .subscribe {
                shouldSaveProperty.set(false)
                batchDataStore.activeBatchProperty.set(newBatch)
                fire(AppSaveDoneEvent())

                if (clearMedia) mediaItems.clear()
            }
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
        batchDataStore.activeBatchProperty.value
            ?.media
            ?.value
            ?.map(mediaMapper::fromEntity)
            ?.forEach(mediaItems::add)

        tableMediaItems.sortedItems.setComparator { o1, o2 ->
            o1.file.name.compareTo(o2.file.name, ignoreCase = true)
        }

        mediaItems.onChangeWithDisposer {
            shouldSaveProperty.set(true)
        }.also(listeners::add)
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