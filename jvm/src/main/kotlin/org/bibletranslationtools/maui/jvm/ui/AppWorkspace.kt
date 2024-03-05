package org.bibletranslationtools.maui.jvm.ui

import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.Workspace
import tornadofx.importStylesheet
import tornadofx.removeFromParent

class AppWorkspace : Workspace() {

    private val navigator: NavigationMediator by inject()

    init {
        header.removeFromParent()

        importStylesheet(AppResources.load("/css/root.css"))
        importStylesheet(AppResources.load("/css/control.css"))
        importStylesheet(AppResources.load("/css/buttons.css"))
    }

    override fun onBeforeShow() {
        navigator.dock<StartupPage>()
    }
}