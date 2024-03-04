package org.bibletranslationtools.maui.jvm.ui.batch

import tornadofx.ViewModel
import java.io.File

class BatchViewModel : ViewModel() {

    fun importFiles(media: List<File>) {
        media.forEach(::println)
    }
}