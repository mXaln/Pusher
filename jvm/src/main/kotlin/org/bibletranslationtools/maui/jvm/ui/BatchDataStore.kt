package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ui.batch.BatchPage
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import org.bibletranslationtools.maui.jvm.ui.work.UploadPage
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

    val uploadTargets = observableListOf(UploadTarget.DEV, UploadTarget.PROD)
    val activeBatchProperty = SimpleObjectProperty<Batch>()

    init {
        val version = getVersion()
        val appTitle = messages["appName"] + (if (version == null) "" else " - $version")
        appTitleProperty.set(appTitle)

        uploadTargetProperty.onChange { target ->
            if (target != null) {
                onTargetChanged()
            }
        }

        activeBatchProperty.onChange { batch ->
            if (batch != null) {
                onBatchChanged()
            }
        }
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

    private fun onBatchChanged() {
        workspace.dock<UploadPage>()
    }
}