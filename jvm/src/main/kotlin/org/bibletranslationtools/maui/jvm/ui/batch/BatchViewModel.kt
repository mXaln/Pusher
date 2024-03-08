package org.bibletranslationtools.maui.jvm.ui.batch

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.usecases.FileProcessingRouter
import org.bibletranslationtools.maui.jvm.controls.dialog.DialogType
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.NavigationMediator
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import org.bibletranslationtools.maui.jvm.ui.events.DialogEvent
import org.bibletranslationtools.maui.jvm.ui.events.ProgressDialogEvent
import org.bibletranslationtools.maui.jvm.ui.upload.UploadPage
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import java.time.LocalDateTime
import java.util.function.Predicate
import javax.inject.Inject

class BatchViewModel : ViewModel() {

    @Inject lateinit var mediaMapper: MediaMapper
    @Inject lateinit var fileProcessRouter: FileProcessingRouter

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

        loadBatches()
        setupBatchSearchListener()
    }

    fun onDock() {
    }

    fun onUndock() {
    }

    fun onDropFiles(files: List<File>) {
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
            .doFinally { fire(ProgressDialogEvent(false))}
            .subscribe { resultList ->
                if (resultList.any { it.status == FileStatus.REJECTED }) {
                    val event = DialogEvent(
                        type = DialogType.ERROR,
                        title = messages["errorOccurred"],
                        message = messages["importFailed"],
                        details = createErrorReport(resultList)
                    )
                    fire(event)
                } else {
                    println("success")
                }
            }
    }

    fun openBatch(batch: Batch) {
        batchDataStore.activeBatchProperty.set(batch)
        navigator.dock<UploadPage>()
    }

    fun deleteBatch(batch: Batch) {
        batches.remove(batch)
        val event = DialogEvent(
            type = DialogType.SUCCESS,
            title = messages["deleteSuccessful"],
            message = messages["batchDeleted"],
            details = batch.name
        )
        fire(event)
    }

    private fun loadBatches() {
        batches.addAll(
            Batch(
                File("example.wav"),
                "en_ulb_gen",
                LocalDateTime.parse("2018-05-12T18:33:52"),
                lazy { listOf() }
            ),
            Batch(
                File("example1.wav"),
                "ah087a0wf70a70aw70f8aw70f87a9f",
                LocalDateTime.parse("2022-05-19T07:21:11"),
                lazy { listOf() }
            ),
            Batch(
                File("example2.wav"),
                "My custom batch name",
                LocalDateTime.parse("2024-12-18T14:10:43"),
                lazy { listOf() }
            ),
            Batch(
                File("example3.wav"),
                "New-batch",
                LocalDateTime.parse("2023-07-11T11:19:48"),
                lazy { listOf() }
            )
        )
        // batches.clear()
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
}