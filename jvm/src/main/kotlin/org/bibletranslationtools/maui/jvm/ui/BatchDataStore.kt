package org.bibletranslationtools.maui.jvm.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.ui.batch.BatchPage
import tornadofx.Component
import tornadofx.ScopedInstance
import tornadofx.observableListOf
import tornadofx.onChange

enum class UploadTarget {
    DEV,
    PROD
}

class BatchDataStore : Component(), ScopedInstance {

    val uploadTargetProperty = SimpleObjectProperty<UploadTarget>()

    val serverProperty = SimpleStringProperty()
    val usernameProperty = SimpleStringProperty()
    val passwordProperty = SimpleStringProperty()

    val batches = observableListOf<Batch>()

    init {
        uploadTargetProperty.onChange { target ->
            if (target != null) {
                onTargetChanged()
            }
        }
    }

    private fun onTargetChanged() {
        workspace.dock<BatchPage>()
    }
}