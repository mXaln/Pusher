package org.bibletranslationtools.maui.jvm.ui

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.subscribeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.Media
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import org.bibletranslationtools.maui.common.usecases.FileProcessingRouter
import org.bibletranslationtools.maui.common.usecases.batch.CreateBatch
import org.bibletranslationtools.maui.common.usecases.batch.UpdateBatch
import org.bibletranslationtools.maui.jvm.controls.dialog.AlertDialogEvent
import org.bibletranslationtools.maui.jvm.controls.dialog.AlertType
import org.bibletranslationtools.maui.jvm.controls.dialog.ProgressDialogEvent
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.events.BatchMediaUpdatedEvent
import org.bibletranslationtools.maui.jvm.ui.upload.UploadPage
import org.slf4j.LoggerFactory
import tornadofx.ViewModel
import tornadofx.get
import tornadofx.runLater
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject

enum class ImportType {
    CREATE,
    UPDATE
}

class ImportFilesViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject lateinit var fileProcessRouter: FileProcessingRouter
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var createBatch: CreateBatch
    @Inject lateinit var updateBatch: UpdateBatch
    @Inject lateinit var mediaMapper: MediaMapper

    private val navigator: NavigationMediator by inject()
    private val batchDataStore: BatchDataStore by inject()

    private val activeBatchProperty = SimpleObjectProperty<Batch>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        activeBatchProperty.bind(batchDataStore.activeBatchProperty)
    }

    fun onDropFiles(files: List<File>, importType: ImportType) {
        if (files.isEmpty()) return

        val event = ProgressDialogEvent(
            true,
            messages["importingFiles"],
            messages["importingFilesMessage"]
        )
        fire(event)

        val filesToImport = prepareFilesToImport(files)
        importFiles(filesToImport, importType)
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

    private fun importFiles(files: List<File>, importType: ImportType) {
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
                    val event = AlertDialogEvent(
                        type = AlertType.INFO,
                        title = messages["errorOccurred"],
                        message = messages["importFailed"],
                        details = createErrorReport(resultList),
                        isWarning = true
                    )
                    fire(event)

                    // Cleanup cached files if import was not successful
                    cleanupCache(resultList)
                } else {
                    val media = resultList.mapNotNull { it.data }.map {
                        if (it.status == FileStatus.PROCESSED) {
                            // Remove status for successfully processed files
                            // In order to re-verify them later
                            it.copy(status = null)
                        } else it
                    }
                    when (importType) {
                        ImportType.CREATE -> createBatch(media)
                        ImportType.UPDATE -> updateBatch(media)
                    }
                }
            }
    }

    private fun createErrorReport(resultList: List<FileResult>): String {
        return resultList
            .filter { it.status == FileStatus.REJECTED }
            .joinToString("") {
                val builder = StringBuilder()
                val separator = "\n\n"
                it.data?.let { media ->
                    builder.append(MessageFormat.format(messages["fileInfo"], media.file))
                    builder.append("\n")
                    builder.append(MessageFormat.format(messages["errorInfo"], media.statusMessage))
                    builder.append("\n")
                    builder.append(
                        media.parentFile?.let { file ->
                            MessageFormat.format(messages["parentFileInfo"], file) + "\n"
                        } ?: "")
                    builder.append(separator)
                } ?: run {
                    builder.append(MessageFormat.format(messages["fileInfo"], it.file))
                    builder.append("\n")
                    builder.append(MessageFormat.format(messages["errorInfo"], it.statusMessage))
                    builder.append("\n")
                    builder.append(separator)
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

    private fun createBatch(media: List<Media>) {
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
                val event = AlertDialogEvent(
                    type = AlertType.INFO,
                    title = messages["errorOccurred"],
                    message = it.message!!,
                    isWarning = true
                )
                fire(event)
            })
    }

    private fun updateBatch(media: List<Media>) {
        fire(BatchMediaUpdatedEvent(media))
    }
}