/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.bibletranslationtools.maui.jvm.audio

import org.bibletranslationtools.maui.common.audio.ISoxBinaryProvider
import org.bibletranslationtools.maui.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class SoxBinaryProvider @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : ISoxBinaryProvider {
    override fun getFile(): File? {
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> {
                directoryProvider.soxBinaryDirectory.resolve("win/sox.exe")
            }
            os.contains("mac") -> {
                directoryProvider.soxBinaryDirectory.resolve("mac/sox")
            }
            os.contains("linux") -> {
                directoryProvider.soxBinaryDirectory.resolve("linux/sox")
            }
            else -> null
        }
    }
}