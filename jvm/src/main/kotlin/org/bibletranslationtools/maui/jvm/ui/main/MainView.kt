package org.bibletranslationtools.maui.jvm.ui.main

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.Property
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ListView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.bibletranslationtools.maui.jvm.assets.AppResources
import org.bibletranslationtools.maui.common.data.Grouping
import org.bibletranslationtools.maui.jvm.controls.mediafilter.mediafilter
import org.bibletranslationtools.maui.jvm.ui.MediaItem
import org.bibletranslationtools.maui.jvm.ui.mediacell.MediaCell
import tornadofx.*
import java.util.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class MainView : View() {
    private val viewModel: MainViewModel by inject()

    private val filter = mediafilter()
    private lateinit var listView: ListView<MediaItem>

    init {
        title = messages["appName"] + getVersion().let {
            if (it == null) "" else " - v$it"
        }
        importStylesheet(AppResources.load("/css/main.css"))
    }

    override val root = stackpane {
        addClass("main")

        createSnackBar(this)
        createSuccessDialog()

        vbox {
            alignment = Pos.CENTER

            addClass("main__progress")
            progressindicator()
            label(messages["processing"])

            visibleWhen(viewModel.isProcessing)
        }

        vbox {
            addClass("main__drop-files")

            visibleWhen {
                viewModel.mediaItemsProperty.emptyProperty()
                    .and(viewModel.isProcessing.not())
            }

            label(messages["dropFiles"])

            onDragOver = onDragOverHandler()
            onDragDropped = onDragDroppedHandler()
        }

        vbox {
            alignment = Pos.CENTER

            hiddenWhen {
                viewModel.mediaItemsProperty.emptyProperty()
                    .or(viewModel.isProcessing)
            }

            filter.apply {
                maxWidthProperty().bind(primaryStage.widthProperty())

                languageLabelProperty.set(messages["language"])
                languagesProperty.set(viewModel.languages)

                resourceTypeLabelProperty.set(messages["resourceType"])
                resourceTypesProperty.set(viewModel.resourceTypes)

                bookLabelProperty.set(messages["book"])
                booksProperty.set(viewModel.books)

                chapterLabelProperty.set(messages["chapter"])

                mediaExtensionLabelProperty.set(messages["mediaExtension"])
                mediaExtensionsProperty.set(viewModel.mediaExtensions)

                mediaQualityLabelProperty.set(messages["mediaQuality"])
                mediaQualitiesProperty.set(viewModel.mediaQualities)

                groupingLabelProperty.set(messages["grouping"])
                groupingsProperty.set(viewModel.groupings)

                onConfirmAction {
                    showConfirmDialog { answer ->
                        onConfirmCallback(answer)
                    }
                }

                setFilterChangeListeners()

                viewModel.updatedObservable.subscribe {
                    if (it) {
                        reset()
                    }
                }
            }

            add(filter)

            listView = listview(viewModel.mediaItems) {
                vgrow = Priority.ALWAYS
                addClass("main__file-list")

                setCellFactory { MediaCell() }

                onDragOver = onDragOverHandler()
                onDragDropped = onDragDroppedHandler()
            }

            hbox {
                addClass("main__footer")

                spacing = 20.0

                add(
                    JFXButton("Verify").apply {
                        addClass("btn", "btn--primary", "main__upload_btn")

                        setOnAction {
                            viewModel.verify()
                        }
                    }
                )
                add(
                    JFXButton(messages["upload"]).apply {
                        addClass("btn", "btn--primary", "main__upload_btn")

                        setOnAction {
                            viewModel.upload()
                        }
                    }
                )
                add(
                    JFXButton(messages["clearList"]).apply {
                        addClass("btn", "btn--secondary", "main__clear_btn")

                        setOnAction {
                            viewModel.clearList()
                        }
                    }
                )
            }
        }
    }

    private fun getVersion(): String? {
        val prop = Properties()
        val inputStream = javaClass.classLoader.getResourceAsStream("version.properties")

        if (inputStream != null) {
            prop.load(inputStream)
            return prop.getProperty("version")
        }

        return null
    }

    private fun onDragOverHandler(): EventHandler<DragEvent> {
        return EventHandler {
            if (it.gestureSource != this && it.dragboard.hasFiles()) {
                it.acceptTransferModes(TransferMode.COPY)
            }
            it.consume()
        }
    }

    private fun onDragDroppedHandler(): EventHandler<DragEvent> {
        return EventHandler {
            var success = false
            if (it.dragboard.hasFiles()) {
                viewModel.onDropFiles(it.dragboard.files)
                success = true
            }
            it.isDropCompleted = success
            it.consume()
        }
    }

    private fun createSnackBar(pane: Pane) {
        val snackBar = JFXSnackbar(pane)
        viewModel.snackBarObservable.subscribe { message ->
            snackBar.enqueue(
                JFXSnackbar.SnackbarEvent(
                    JFXSnackbarLayout(message, messages["ok"]) { snackBar.close() },
                    Duration.INDEFINITE,
                    null
                )
            )
        }
    }

    private fun showConfirmDialog(op: (answer: Boolean) -> Unit) {
        Alert(Alert.AlertType.CONFIRMATION).apply {
            title = messages["confirmSelection"]
            headerText = null
            contentText = messages["confirmSelectionQuestion"]

            val yesButton = ButtonType(messages["yes"])
            val noButton = ButtonType(messages["no"])

            buttonTypes.setAll(
                yesButton,
                noButton
            )

            val result = showAndWait()

            when (result.get()) {
                yesButton -> op.invoke(true)
                noButton -> op.invoke(false)
            }
        }
    }

    private fun createSuccessDialog() {
        Alert(Alert.AlertType.INFORMATION).apply {
            title = messages["successTitle"]
            headerText = null
            contentText = messages["uploadSuccessfull"]

            viewModel.successfulUploadProperty.onChange {
                if (it) show() else close()
            }

            setOnCloseRequest {
                viewModel.successfulUploadProperty.set(false)
            }
        }
    }

    private fun setFilterChangeListeners() {
        setPropertyListener(
            filter.selectedLanguageProperty,
            MediaItem::language,
        )

        setPropertyListener(
            filter.selectedResourceTypeProperty,
            MediaItem::resourceType,
        )

        setPropertyListener(
            filter.selectedBookProperty,
            MediaItem::book,
        )

        setPropertyListener(
            filter.chapterProperty,
            MediaItem::chapter,
        )

        setPropertyListener(
            filter.selectedMediaExtensionProperty,
            MediaItem::mediaExtension,
            MediaItem::mediaExtensionAvailable
        )

        setPropertyListener(
            filter.selectedMediaQualityProperty,
            MediaItem::mediaQuality,
            MediaItem::mediaQualityAvailable
        )

        setPropertyListener(
            filter.selectedGroupingProperty,
            MediaItem::grouping,
        )
    }

    private fun <T> setPropertyListener(
        property: Property<T>,
        targetProp: KMutableProperty1<MediaItem, T?>,
        availableProp: KProperty1<MediaItem, BooleanExpression>? = null
    ) {
        property.onChange {
            it?.let { prop ->
                viewModel.mediaItems.forEach { mediaItem ->
                    val available = availableProp?.get(mediaItem)?.value ?: true
                    if (available) {
                        val isGrouping = prop is Grouping
                        val groupingAvailable = isGrouping &&
                                !viewModel.restrictedGroupings(mediaItem)
                                    .contains(prop as Grouping)

                        if (!isGrouping || groupingAvailable) {
                            targetProp.set(mediaItem, prop)
                            listView.refresh()
                        }
                    }
                }
            }
        }
    }
}
