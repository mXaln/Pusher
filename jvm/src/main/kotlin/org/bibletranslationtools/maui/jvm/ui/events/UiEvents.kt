package org.bibletranslationtools.maui.jvm.ui.events

import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.jvm.controls.dialog.DialogType
import tornadofx.FXEvent

class DialogEvent(
    val type: DialogType,
    val title: String,
    val message: String,
    val details: String? = null
) : FXEvent()
class ProgressDialogEvent(
    val show: Boolean,
    val title: String? = null,
    val message: String? = null
) : FXEvent()
class OpenBatchEvent(val batch: Batch) : FXEvent()
class DeleteBatchEvent(val batch: Batch) : FXEvent()
class EditBatchNameEvent(val batch: Batch, val name: String) : FXEvent()