package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ui.batch.BatchPage
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

enum class UploadTarget {
    DEV,
    PROD
}

class BatchDataStore : Component(), ScopedInstance {

    val appTitleProperty = SimpleStringProperty()
    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    val serverProperty = SimpleStringProperty()
    val usernameProperty = SimpleStringProperty()
    val passwordProperty = SimpleStringProperty()

    val batches = observableListOf<Batch>()
    val uploadTargets = observableListOf(UploadTarget.DEV, UploadTarget.PROD)

    init {
        uploadTargetProperty.onChange { target ->
            if (target != null) {
                onTargetChanged()
            }
        }

        val version = getVersion()
        val appTitle = messages["appName"] + (if (version == null) "" else " - $version")
        appTitleProperty.set(appTitle)

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
                LocalDateTime.parse("2018-05-19T07:21:11"),
                lazy { listOf() }
            ),
            Batch(
                File("example2.wav"),
                "My custom batch name",
                LocalDateTime.parse("2024-12-18T14:10:43"),
                lazy { listOf() }
            )
        )
    }

    fun goHome() {
        uploadTargetProperty.set(null)
        workspace.dock<StartupPage>()
    }

    private fun getVersion(): String? {
        val prop = Properties()
        val inputStream = javaClass.classLoader.getResourceAsStream("version.properties")

        if (inputStream != null) {
            prop.load(inputStream)
            return prop.getProperty("version")
        }

        return null
    }

    private fun onTargetChanged() {
        workspace.dock<BatchPage>()
    }
}