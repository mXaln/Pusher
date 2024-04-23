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

class SoxBinaryProvider : ISoxBinaryProvider {
    override fun getPath(): String? {
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> {
                SoxBinaryProvider::class.java.getResource("/sox/win/sox.exe")?.path
            }
            os.contains("mac") -> {
                SoxBinaryProvider::class.java.getResource("/sox/mac/sox")?.path
            }
            os.contains("linux") -> {
                SoxBinaryProvider::class.java.getResource("/sox/linux/sox")?.path
            }
            else -> null
        }
    }
}