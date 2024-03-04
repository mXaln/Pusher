package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.time.format.DateTimeFormatter

class BatchTableView(
    batches: ObservableList<Batch>
) : TableView<Batch>(batches) {

    val noBatchesPromptProperty = SimpleStringProperty()
    val batchNameLabelProperty = SimpleStringProperty()
    val batchDateLabelProperty = SimpleStringProperty()

    init {
        addClass("batch-table-view")
        importStylesheet(AppResources.load("/css/batch-table-view.css"))

        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        placeholder = borderpane {
            center = vbox {
                addClass("batch-list__placeholder")

                label {
                    addClass("batch-list__placeholder-icon")
                    graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE)
                }
                label(noBatchesPromptProperty) {
                    addClass("batch-list__placeholder-text")
                }
            }
        }

        column("", String::class) {
            textProperty().bind(batchNameLabelProperty)
            setCellValueFactory { it.value.name.toProperty() }
            isResizable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.45))
        }
        column("", String::class) {
            textProperty().bind(batchDateLabelProperty)
            setCellValueFactory {
                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss a")
                it.value.created.format(formatter).toProperty()
            }
            isResizable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.45))
        }
        column("", Node::class) {
            addClass("actions-column")
            setCellValueFactory {
                hbox {
                    addClass("actions-column__buttons")
                    button {
                        graphic = FontIcon(MaterialDesign.MDI_DELETE)
                        action {
                            println("delete")
                        }
                    }
                }.toProperty()
            }
            isResizable = false
            prefWidthProperty().bind(this@BatchTableView.widthProperty().multiply(0.1))
        }
    }
}

/**
 * Constructs a language table and attach it to the parent.
 */
fun EventTarget.batchTableView(
    values: ObservableList<Batch>,
    op: BatchTableView.() -> Unit = {}
) = BatchTableView(values).attachTo(this, op)
