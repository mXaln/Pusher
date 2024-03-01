package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ui.batch.BatchPage
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.*
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