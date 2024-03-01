package org.bibletranslationtools.maui.jvm

import javafx.scene.image.Image
import javafx.stage.Stage
import org.bibletranslationtools.maui.jvm.di.DaggerAppDependencyGraph
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.ui.AppWorkspace
import tornadofx.*

class MainApp: App(AppWorkspace::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()

    override fun start(stage: Stage) {
        stage.isMaximized = true
        stage.icons.add(
            Image(javaClass.getResource("/launcher.png").openStream())
        )
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    launch<MainApp>(args)
}
