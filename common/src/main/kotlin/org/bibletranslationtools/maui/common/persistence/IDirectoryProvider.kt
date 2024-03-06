package org.bibletranslationtools.maui.common.persistence

import java.io.File

interface IDirectoryProvider {
    /** Directory to store the application's private data */
    fun getAppDataDirectory(appendedPath: String = ""): File

    /** Create a new IFileWriter */
    // fun newFileWriter(file: File): IFileWriter

    /** Create a new IFileReader */
    // fun newFileReader(file: File): IFileReader

    /** Create directory in cache directory */
    fun createCacheDirectory(dirName: String): File

    /** Create directory in temporary directory */
    fun createTempDirectory(dirName: String): File

    /** Create temp file */
    fun createTempFile(prefix: String, suffix: String? = null): File

    /** Clean temporary directory */
    fun cleanTempDirectory()

    val logsDirectory: File
    val tempDirectory: File
    val batchDirectory: File
    val cacheDirectory: File
}