package org.bibletranslationtools.maui.jvm

import javafx.scene.image.Image
import javafx.stage.Stage
import org.bibletranslationtools.maui.jvm.di.AppDependencyGraph
import org.bibletranslationtools.maui.jvm.di.DaggerAppDependencyGraph
import org.bibletranslationtools.maui.jvm.di.IDependencyGraphProvider
import org.bibletranslationtools.maui.jvm.ui.events.AppCloseRequestEvent
import org.bibletranslationtools.maui.jvm.ui.AppWorkspace
import tornadofx.*

class MainApp: App(AppWorkspace::class), IDependencyGraphProvider {
    override val dependencyGraph: AppDependencyGraph =
        DaggerAppDependencyGraph.builder().build()

    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
        javaClass.getResource("/launcher.png")?.let { url ->
            stage.icons.add(Image(url.openStream()))
        }
        stage.scene.window.setOnCloseRequest {
            fire(AppCloseRequestEvent())
        }
    }
}

fun main(args: Array<String>) {
    launch<MainApp>(args)
}
