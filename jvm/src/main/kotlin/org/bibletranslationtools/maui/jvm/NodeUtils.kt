package org.bibletranslationtools.maui.jvm

import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.control.ScrollBar
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import tornadofx.add
import tornadofx.addClass

fun Parent.customizeScrollbarSkin() {
    val scrollBars = lookupAll(".scroll-bar")
    scrollBars
        .mapNotNull { it as? ScrollBar }
        .forEach { bar ->
            val thumb = bar.lookup(".thumb")
            (thumb as? StackPane)?.let { t ->
                if (t.children.size == 0) {
                    t.add(
                        FontIcon(Material.DRAG_INDICATOR).apply {
                            addClass("thumb-icon")
                            if (bar.orientation == Orientation.HORIZONTAL) rotate = 90.0
                        }
                    )
                }
            }
        }
}