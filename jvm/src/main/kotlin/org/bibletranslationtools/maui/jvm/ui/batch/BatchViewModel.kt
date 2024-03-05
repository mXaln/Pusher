package org.bibletranslationtools.maui.jvm.ui.batch

import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.NavigationMediator
import org.bibletranslationtools.maui.jvm.ui.work.UploadPage
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.onChange
import java.io.File
import java.time.LocalDateTime
import java.util.function.Predicate

class BatchViewModel : ViewModel() {

    private val navigator: NavigationMediator by inject()
    private val batchDataStore: BatchDataStore by inject()
    private val batches = observableListOf<Batch>()

    private val filteredBatches = FilteredList(batches)
    val sortedBatches = SortedList(filteredBatches)
    val searchQueryProperty = SimpleStringProperty()

    init {
        loadBatches()
        setupBatchSearchListener()
    }

    fun onDock() {

    }

    fun onUndock() {

    }

    fun importFiles(media: List<File>) {
        media.forEach(::println)
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
        //batches.clear()
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