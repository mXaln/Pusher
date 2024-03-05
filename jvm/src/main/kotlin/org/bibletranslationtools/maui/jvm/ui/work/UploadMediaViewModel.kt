package org.bibletranslationtools.maui.jvm.ui.work

import org.bibletranslationtools.maui.jvm.ui.events.AppSaveDoneEvent
import tornadofx.ViewModel

class UploadMediaViewModel : ViewModel() {

    fun saveBatch() {
        println("Saved...")
        fire(AppSaveDoneEvent())
    }

    fun onDock() {

    }

    fun onUndock() {
        saveBatch()
    }
}