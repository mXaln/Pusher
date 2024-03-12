package org.bibletranslationtools.maui.common

import org.bibletranslationtools.maui.common.data.Batch
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider

class BatchFileAccessor(
    private val directoryProvider: IDirectoryProvider,
    private val batch: Batch
) {

}