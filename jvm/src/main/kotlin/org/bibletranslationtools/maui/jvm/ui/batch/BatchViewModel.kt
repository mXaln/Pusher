package org.bibletranslationtools.maui.jvm.ui.batch

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.usecases.FileProcessingRouter
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.mappers.MediaMapper
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.NavigationMediator
import org.bibletranslationtools.maui.jvm.ui.work.UploadPage
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.onChange
import java.io.File
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

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        loadBatches()
        setupBatchSearchListener()
    }

    fun onDock() {
    }

    fun onUndock() {
    }

    fun onDropFiles(files: List<File>) {
        // isProcessing.set(true)
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
            .doFinally { /*isProcessing.set(false)*/ }
            .subscribe { resultList ->
                resultList.forEach {
                    if (it.status == FileStatus.ERROR) {
                        /*emitErrorMessage(
                            message = messages["fileNotRecognized"],
                            fileName = it.requestedFile?.name ?: ""
                        )*/
                        println("File ${it.requestedFile} rejected")
                        println(it.statusMessage)
                    } else {
                        val item = mediaMapper.fromEntity(it.data!!)

                        println(item.file)
                        println("Language: ${item.language}")
                        println("Resource Type: ${item.resourceType}")
                        println("Book: ${item.book}")
                        println("Chapter: ${item.chapter}")
                        println("Status: ${item.status}")
                        println("Message: ${item.statusMessage}")
                    }
                    println("------------------------------------------")
                    // if (!mediaItems.contains(item)) mediaItems.add(item)
                }
                // mediaItems.sort()
            }
    }

    fun openBatch(batch: Batch) {
        batchDataStore.activeBatchProperty.set(batch)
        navigator.dock<UploadPage>()
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
}