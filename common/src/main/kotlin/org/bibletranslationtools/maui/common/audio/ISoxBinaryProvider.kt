package org.bibletranslationtools.maui.common.audio

import java.io.File

interface ISoxBinaryProvider {
    fun getFile(): File?
}