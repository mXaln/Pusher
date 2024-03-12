package org.bibletranslationtools.maui.jvm.controls

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.jvm.customizeScrollbarSkin
import org.bibletranslationtools.maui.jvm.ui.events.DeleteBatchEvent
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.text.MessageFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class BatchTableView(
    batches: ObservableList<Batch>
) : TableView<Batch>(batches) {

    val emptyPromptProperty = SimpleStringProperty()
    val nameColumnProperty = SimpleStringProperty()
    val dateColumnProperty = SimpleStringProperty()
    val deleteTextProperty = SimpleStringProperty()
    val dayAgoTextProperty = SimpleStringProperty()
    val daysAgoTextProperty = SimpleStringProperty()
    val todayTextProperty = SimpleStringProperty()

    init {
        addClass("batch-table-view")
        importStylesheet(AppResources.load("/css/batch-table-view.css"))

        runLater { customizeScrollbarSkin() }

        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
        isEditable = true

        placeholder = borderpane {
            center = vbox {
                addClass("batch-table-view__placeholder")

                label {
                    addClass("batch-table-view__placeholder-icon")
                    graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE)
                }
                label(emptyPromptProperty) {
                    addClass("batch-table-view__placeholder-text")
                }
            }
        }

        column("", Node::class) {
            textProperty().bind(nameColumnProperty)
            setCellValueFactory {
                label(it.value.name) {
                    addClass("batch-table-view__name")
                    graphic = FontIcon(MaterialDesign.MDI_FILE)
                }.toProperty()
            }
            isReorderable = false
        }
        column("", String::class) {
            textProperty().bind(dateColumnProperty)
            setCellValueFactory {
                formatDateTime(it.value.created)
            }
            isReorderable = false
        }
        column("", Node::class) {
            setCellValueFactory {
                hbox {
                    addClass("actions-column__buttons")
                    button {
                        addClass("btn", "btn--icon", "btn--delete")

                        graphic = FontIcon(MaterialDesign.MDI_DELETE)
                        tooltip {
                            textProperty().bind(deleteTextProperty)
                        }

                        action {
                            FX.eventbus.fire(DeleteBatchEvent(it.value))
                        }
                    }
                }.toProperty()
            }
            isReorderable = false
            isSortable = false
        }
    }

    private fun formatDateTime(dateTime: String): ObservableValue<String> {
        val parsed = LocalDateTime.parse(dateTime)
        val daysAgo = parsed.until(LocalDateTime.now(), ChronoUnit.DAYS)
        return when {
            daysAgo == 0L -> todayTextProperty
            daysAgo == 1L -> {
                dayAgoTextProperty.stringBinding { days ->
                    days?.let { MessageFormat.format(days, daysAgo) } ?: ""
                }
            }
            daysAgo > 1 -> {
                daysAgoTextProperty.stringBinding { days ->
                    days?.let { MessageFormat.format(days, daysAgo) } ?: ""
                }
            }
            else -> SimpleStringProperty()
        }
    }
}

fun EventTarget.batchTableView(
    values: ObservableList<Batch>,
    op: BatchTableView.() -> Unit = {}
) = BatchTableView(values).attachTo(this, op)
