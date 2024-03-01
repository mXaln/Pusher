package org.bibletranslationtools.maui.jvm.ui.batch

import org.bibletranslationtools.maui.jvm.assets.AppResources
import tornadofx.*

class BatchPage : View() {
    private val viewModel: BatchViewModel by inject()

    init {
        importStylesheet(AppResources.load("/css/batch.css"))
    }

    override val root = borderpane {
        top {
            label("Batch Page")
            button("Click me") {
                addClass("btn", "btn--primary")
            }
        }
    }
}