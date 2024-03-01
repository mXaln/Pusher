package org.bibletranslationtools.maui.jvm.ui

import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.ui.events.GoHomeEvent
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.Workspace
import tornadofx.importStylesheet
import tornadofx.removeFromParent

class AppWorkspace : Workspace() {

    private val batchDataStore: BatchDataStore by inject()

    init {
        header.removeFromParent()

        importStylesheet(AppResources.load("/css/root.css"))
        importStylesheet(AppResources.load("/css/buttons.css"))
        importStylesheet(AppResources.load("/css/main-header.css"))
        importStylesheet(AppResources.load("/css/upload-target-header.css"))

        subscribe<GoHomeEvent> {
            batchDataStore.goHome()
        }
    }

    override fun onBeforeShow() {
        workspace.dock<StartupPage>()
    }
}