package org.bibletranslationtools.maui.jvm.ui.batch

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.subscribeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.persistence.IBatchRepository
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.common.usecases.FileProcessingRouter
import org.bibletranslationtools.maui.common.usecases.batch.CreateBatch
import org.bibletranslationtools.maui.common.usecases.batch.DeleteBatch
import org.bibletranslationtools.maui.common.usecases.batch.UpdateBatch
import org.bibletranslationtools.maui.jvm.controls.dialog.DialogType
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.NavigationMediator
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.DialogEvent
import org.bibletranslationtools.maui.jvm.ui.events.ProgressDialogEvent
import org.bibletranslationtools.maui.jvm.ui.upload.UploadPage
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import java.util.function.Predicate
import javax.inject.Inject

class BatchViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject lateinit var mediaMapper: MediaMapper
    @Inject lateinit var fileProcessRouter: FileProcessingRouter
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var batchRepository: IBatchRepository
    @Inject lateinit var deleteBatch: DeleteBatch
    @Inject lateinit var updateBatch: UpdateBatch
    @Inject lateinit var createBatch: CreateBatch

    private val navigator: NavigationMediator by inject()
    private val batchDataStore: BatchDataStore by inject()

    private val batches = observableListOf<Batch>()
    private val filteredBatches = FilteredList(batches)
    val sortedBatches = SortedList(filteredBatches)
    val searchQueryProperty = SimpleStringProperty()

    val appTitleProperty = SimpleStringProperty()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()
    val uploadTargets = observableListOf<UploadTarget>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        appTitleProperty.bind(batchDataStore.appTitleProperty)
        uploadTargetProperty.bind(batchDataStore.uploadTargetProperty)
        uploadTargets.bind(batchDataStore.uploadTargets) { it }

        setupBatchSearchListener()
    }

    fun onDock() {
        loadBatches()
    }

    fun onUndock() {
    }

    fun onDropFiles(files: List<File>) {
        if (files.isEmpty()) return

        val event = ProgressDialogEvent(
            true,
            messages["importingFiles"],
            messages["importingFilesMessage"]
        )
        fire(event)

        val filesToImport = prepareFilesToImport(files)
        importFiles(filesToImport)
    }

    private fun prepareFilesToImport(files: List<File>): List<File> {
        val filesToImport = mutableListOf<File>()
        files.forEach { file ->
            file.walk().filter { it.isFile }.forEach {
                filesToImport.add(it)
            }
        }
        return filesToImport
    }

    private fun importFiles(files: List<File>) {
        Observable.fromCallable {
            fileProcessRouter.handleFiles(files)
        }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                fire(ProgressDialogEvent(false))
            }
            .subscribe { resultList ->
                if (resultList.any { it.status == FileStatus.REJECTED }) {
                    val event = DialogEvent(
                        type = DialogType.ERROR,
                        title = messages["errorOccurred"],
                        message = messages["importFailed"],
                        details = createErrorReport(resultList)
                    )
                    fire(event)

                    // Cleanup cached files if import was not successful
                    cleanupCache(resultList)
                } else {
                    createBatch(resultList)
                }
            }
    }

    fun openBatch(batch: Batch) {
        batchDataStore.activeBatchProperty.set(batch)
        navigator.dock<UploadPage>()
    }

    fun deleteBatch(batch: Batch) {
        deleteBatch.delete(batch)
            .subscribeOn(Schedulers.io())
            .doOnError {
                logger.error("Error in deleteBatch", it)
            }
            .subscribe {
                batches.remove(batch)
                val event = DialogEvent(
                    type = DialogType.INFO,
                    title = messages["deleteSuccessful"],
                    message = messages["batchDeleted"],
                    details = batch.name
                )
                fire(event)
            }
    }

    fun editBatchName(batch: Batch, name: String) {
        if (batch.name != name) {
            val newBatch = batch.copy(name = name)
            updateBatch.edit(newBatch)
                .subscribeOn(Schedulers.io())
                .doOnError {
                    logger.error("Error in editBatchName", it)
                }
                .subscribe {
                    batches.remove(batch)
                    batches.add(newBatch)
                }
        }
    }

    private fun loadBatches() {
        batches.clear()

        sortedBatches.comparator = compareByDescending { it.created }

        Single.fromCallable {
            batchRepository.getAll()
        }
            .observeOn(Schedulers.io())
            .subscribeOnFx()
            .subscribe { list ->
                batches.addAll(list)
            }
    }

    private fun setupBatchSearchListener() {
        searchQueryProperty.onChange { q ->
            val query = q?.trim()

            if (query.isNullOrEmpty()) {
                filteredBatches.predicate = Predicate { true }
            } else {
                filteredBatches.predicate = Predicate { batch ->
                    batch.name.contains(query, true)
                }
            }
        }
    }

    private fun createErrorReport(resultList: List<FileResult>): String {
        return resultList
            .filter { it.status == FileStatus.REJECTED }
            .joinToString("") {
                val separator = "------------------------------------------------\n"
                it.data?.let { media ->
                    val fileText = MessageFormat.format(messages["fileInfo"], media.file)
                    val errorText = MessageFormat.format(messages["errorInfo"], media.statusMessage)
                    val parentFile = media.parentFile?.let { file ->
                        MessageFormat.format(messages["parentFileInfo"], file) + "\n"
                    } ?: ""
                    "$fileText\n$errorText\n$parentFile$separator"
                } ?: run {
                    val fileText = MessageFormat.format(messages["fileInfo"], it.parentFile)
                    val errorText = MessageFormat.format(messages["errorInfo"], it.statusMessage)
                    "$fileText\n$errorText\n$separator"
                }
            }
    }

    private fun cleanupCache(resultList: List<FileResult>) {
        Completable.fromCallable {
            directoryProvider.deleteCachedFiles(
                resultList.mapNotNull {
                    it.data?.file
                }
            )
        }
            .observeOn(Schedulers.io())
            .doOnError {
                logger.error("Error in cleanupCache", it)
            }
            .subscribe()
    }

    private fun createBatch(resultList: List<FileResult>) {
        val media = resultList.mapNotNull { it.data }
        createBatch.create(media)
            .observeOn(Schedulers.io())
            .doOnError {
                logger.error("Error in createBatch", it)
            }
            .subscribeOnFx()
            .subscribe({ batch ->
                runLater {
                    batchDataStore.activeBatchProperty.set(batch)
                    navigator.dock<UploadPage>()
                }
            }, {
                val event = DialogEvent(
                    type = DialogType.ERROR,
                    title = messages["errorOccurred"],
                    message = it.message!!
                )
                fire(event)
            })
    }
}