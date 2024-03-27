package org.bibletranslationtools.maui.jvm.ui.upload

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.*
import org.bibletranslationtools.maui.common.io.IBooksReader
import org.bibletranslationtools.maui.common.io.ILanguagesReader
import org.bibletranslationtools.maui.common.io.IResourceTypesReader
import org.bibletranslationtools.maui.common.persistence.*
import org.bibletranslationtools.maui.common.usecases.FileVerifyingRouter
import org.bibletranslationtools.maui.common.usecases.MakePath
import org.bibletranslationtools.maui.common.usecases.TransferFile
import org.bibletranslationtools.maui.common.usecases.batch.UpdateBatch
import org.bibletranslationtools.maui.jvm.ListenerDisposer
import org.bibletranslationtools.maui.jvm.client.FtpTransferClient
import org.bibletranslationtools.maui.jvm.controls.dialog.ConfirmDialogEvent
import org.bibletranslationtools.maui.jvm.controls.dialog.DialogType
import org.bibletranslationtools.maui.jvm.controls.dialog.LoginDialogEvent
import org.bibletranslationtools.maui.jvm.controls.dialog.ProgressDialogEvent
import org.bibletranslationtools.maui.jvm.data.FileStatusFilter
import org.bibletranslationtools.maui.jvm.data.MediaItem
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.onChangeWithDisposer
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.ImportFilesViewModel
import org.bibletranslationtools.maui.jvm.ui.ImportType
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.util.function.Predicate
import javax.inject.Inject
import io.reactivex.rxkotlin.toObservable as toRxObservable


class UploadMediaViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var mediaMapper: MediaMapper
    @Inject lateinit var updateBatch: UpdateBatch
    @Inject lateinit var fileVerifyingRouter: FileVerifyingRouter
    @Inject lateinit var languagesReader: ILanguagesReader
    @Inject lateinit var booksReader: IBooksReader
    @Inject lateinit var resourceTypesReader: IResourceTypesReader
    @Inject lateinit var prefRepository: IPrefRepository

    private val batchDataStore: BatchDataStore by inject()
    private val importFilesViewModel: ImportFilesViewModel by inject()

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
    val filteredMediaItems = SortedFilteredList(mediaItems)

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
    val defaultPredicate = Predicate<MediaItem> { !it.removed }

    private val listeners = mutableListOf<ListenerDisposer>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        batchDataStore.activeBatchProperty.onChangeAndDoNow {
            it?.let { batch ->
                batchNameProperty.set(batch.name)
            }
        }

        uploadTargetProperty.bindBidirectional(batchDataStore.uploadTargetProperty)
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

    fun onDropFiles(files: List<File>) {
        importFilesViewModel.onDropFiles(files, ImportType.UPDATE)
    }

    fun onNewMedia(media: List<Media>) {
        mediaItems.addAll(
            media.map(mediaMapper::fromEntity).filter {
                !mediaItems.contains(it)
            }
        )
    }

    fun saveBatch(clearMedia: Boolean = false) {
        val newBatch = activeBatchProperty.value.copy(
            name = batchNameProperty.value,
            media = lazy {
                mediaItems.map(mediaMapper::toEntity)
            }
        )
        updateBatch.update(newBatch)
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
        val event = ConfirmDialogEvent(
            DialogType.DELETE,
            messages["removingFiles"],
            messages["removingFilesMessage"],
            messages["wishToContinue"],
            secondaryAction = {
                filteredMediaItems
                    .filter { it.selected }
                    .forEach { it.removed = true }

                filteredMediaItems.filteredItems.apply {
                    predicate = defaultPredicate.and(predicate)
                }
            }
        )
        fire(event)
    }

    fun verify() {
        val progress = ProgressDialogEvent(
            true,
            messages["verifyingFiles"],
            messages["verifyingFilesMessage"]
        )
        fire(progress)

        Single.fromCallable {
            filteredMediaItems
                .filter { it.selected }
                .map { item ->
                    Pair(item, fileVerifyingRouter.handleItem(mediaMapper.toEntity(item)))
                }
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                fire(ProgressDialogEvent(false))
            }
            .subscribe({ items ->
                items.forEach { (item, result) ->
                    item.status = result.status
                    item.statusMessage = result.message
                }

                val success = ConfirmDialogEvent(
                    DialogType.INFO,
                    messages["filesVerified"],
                    messages["filesVerifiedMessage"]
                )
                fire(success)
            }, {
                val error = ConfirmDialogEvent(
                    DialogType.ERROR,
                    messages["filesVerified"],
                    messages["filesVerifiedErrorMessage"],
                    it.message
                )
                fire(error)
            })
    }

    fun tryUpload() {
        val server = batchDataStore.serverProperty.value.trim()
        val user = batchDataStore.userProperty.value.trim()
        val password = batchDataStore.passwordProperty.value.trim()

        if (server.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty()) {
            val hasUnverified = filteredMediaItems
                .filter { it.selected }
                .any {
                    it.status == null || it.status == FileStatus.REJECTED
                }
            val hasSelected = mediaItems.any { it.selected }

            when {
                hasUnverified -> {
                    val error = ConfirmDialogEvent(
                        DialogType.ERROR,
                        messages["errorOccurred"],
                        messages["hasUnverifiedFilesError"]
                    )
                    fire(error)
                }
                !hasSelected -> {
                    val error = ConfirmDialogEvent(
                        DialogType.ERROR,
                        messages["errorOccurred"],
                        messages["noSelectedFilesError"]
                    )
                    fire(error)
                }
                else -> doUpload()
            }
        } else {
            val loginEvent = LoginDialogEvent {
                updateLoginCredentials()
                runLater { tryUpload() }
            }
            fire(loginEvent)
        }
    }

    private fun doUpload() {
        val progress = ProgressDialogEvent(
            true,
            messages["processingUpload"],
            messages["processingUploadMessage"],
            showProgress = true
        )
        fire(progress)

        var current = 1.0
        val total = filteredMediaItems.filter { it.selected }.size

        filteredMediaItems.filter { it.selected }
            .toRxObservable()
            .map(mediaMapper::toEntity)
            .flatMapCompletable { media ->
                MakePath(media).build()
                    .flatMapCompletable { targetPath ->
                        val transferClient = FtpTransferClient(
                            media.file,
                            targetPath,
                            batchDataStore.serverProperty.value,
                            batchDataStore.userProperty.value,
                            batchDataStore.passwordProperty.value
                        )
                        TransferFile(transferClient).transfer().doFinally {
                            progress.progressProperty.set(current / total)
                            current++
                        }
                    }
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                fire(ProgressDialogEvent(false))
            }
            .subscribe({
                val success = ConfirmDialogEvent(
                    DialogType.INFO,
                    messages["filesUploaded"],
                    messages["filesUploadedMessage"]
                )
                fire(success)
            },{
                val error = ConfirmDialogEvent(
                    DialogType.ERROR,
                    messages["errorOccurred"],
                    messages["uploadFailed"],
                    it.message
                )
                fire(error)

                // Clear password on transfer error to allow user to update credentials
                batchDataStore.passwordProperty.set("")
            })
    }

    private fun loadMediaItems() {
        batchDataStore.activeBatchProperty.value
            ?.media
            ?.value
            ?.map(mediaMapper::fromEntity)
            ?.forEach(mediaItems::add)

        filteredMediaItems.sortedItems.setComparator { o1, o2 ->
            o1.file.name.compareTo(o2.file.name, ignoreCase = true)
        }

        filteredMediaItems.filteredItems.predicate = defaultPredicate

        mediaItems.onChangeWithDisposer {
            shouldSaveProperty.set(true)
        }.also(listeners::add)
    }

    private fun loadLanguages() {
        Single.fromCallable {
            languagesReader.read()
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                languages.addAll(list)
            }
    }

    private fun loadResourceTypes() {
        Single.fromCallable {
            resourceTypesReader.read()
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                resourceTypes.addAll(list)
            }
    }

    private fun loadBooks() {
        Single.fromCallable {
            booksReader.read()
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { list ->
                books.addAll(list)
            }
    }

    private fun updateLoginCredentials() {
        when (uploadTargetProperty.value) {
            UploadTarget.DEV -> {
                prefRepository.put(DEV_SERVER_NAME_KEY, batchDataStore.serverProperty.value)
                prefRepository.put(DEV_USER_NAME_KEY, batchDataStore.userProperty.value)
            }
            UploadTarget.PROD -> {
                prefRepository.put(PROD_SERVER_NAME_KEY, batchDataStore.serverProperty.value)
                prefRepository.put(PROD_USER_NAME_KEY, batchDataStore.userProperty.value)
            }
            else -> {}
        }
    }
}