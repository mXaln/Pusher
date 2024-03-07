package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.Batch
import tornadofx.*
import java.util.*

enum class UploadTarget(val target: String, val styleClass: String) {
    DEV("dev", "dev-target"),
    PROD("prod", "prod-target")
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
}