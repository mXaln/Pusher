package org.bibletranslationtools.maui.jvm.ui.startup

import org.bibletranslationtools.maui.jvm.ui.BatchDataStore
import org.bibletranslationtools.maui.jvm.ui.UploadTarget
import tornadofx.ViewModel

class StartupViewModel : ViewModel() {

    private val batchDataStore: BatchDataStore by inject()

    fun selectUploadTarget(target: UploadTarget) {
        batchDataStore.uploadTargetProperty.set(target)
    }
}