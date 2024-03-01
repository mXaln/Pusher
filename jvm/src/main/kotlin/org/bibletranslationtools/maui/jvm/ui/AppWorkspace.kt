package org.bibletranslationtools.maui.jvm.ui

import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.Workspace
import tornadofx.removeFromParent

class AppWorkspace : Workspace() {

    init {
        header.removeFromParent()
    }

    override fun onBeforeShow() {
        workspace.dock<StartupPage>()
    }
}