package org.bibletranslationtools.maui.jvm.ui

import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.onChangeAndDoNow
import org.bibletranslationtools.maui.jvm.ui.startup.StartupPage
import tornadofx.*

class AppWorkspace : Workspace() {

    private val navigator: NavigationMediator by inject()
    private val batchDataStore: BatchDataStore by inject()

    init {
        header.removeFromParent()

        importStylesheet(AppResources.load("/css/root.css"))
        importStylesheet(AppResources.load("/css/target/dev-target.css"))
        importStylesheet(AppResources.load("/css/target/prod-target.css"))

        importStylesheet(AppResources.load("/css/control.css"))
        importStylesheet(AppResources.load("/css/buttons.css"))

        bindUploadTargetClassToRoot()
    }

    override fun onBeforeShow() {
        navigator.dock<StartupPage>()
    }

    private fun bindUploadTargetClassToRoot() {
        batchDataStore.uploadTargetProperty.onChangeAndDoNow {
            when (it) {
                UploadTarget.DEV -> {
                    root.addClass(UploadTarget.DEV.styleClass)
                    root.removeClass(UploadTarget.PROD.styleClass)
                }
                else -> {
                    root.addClass(UploadTarget.PROD.styleClass)
                    root.removeClass(UploadTarget.DEV.styleClass)
                }
            }
        }
    }
}