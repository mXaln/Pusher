package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.bibletranslationtools.maui.common.data.MediaExtension
import org.bibletranslationtools.maui.common.usecases.ParseFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class OratureFileProcessorTest {
    private val oratureFileName = "orature_file.zip"
    private val expectedWavFiles = 2

    @Test
    fun testExtractAudioFiles() {
        val oratureFile = getOratureFile(oratureFileName)
        val extension = MediaExtension.WAV.toString()
        val files = OratureFileProcessor().extractAudio(oratureFile, extension)

        assertEquals(expectedWavFiles, files.size)

        files.forEach { file ->
            val result = ParseFileName(file).parse()

            // imported file names should be valid for parser
            assertNotNull(result.language)
            assertNotNull(result.resourceType)
            assertNotNull(result.book)
            assertNotNull(result.chapter)
        }
    }

    @Test
    fun testProcessGoodOratureFile() {
        val oratureFile = getOratureFile(oratureFileName)
        val exportList = mutableListOf<FileResult>()
        val queue: Queue<File> = LinkedList<File>()
        val status = OratureFileProcessor().process(oratureFile, queue, exportList)

        assertEquals(FileStatus.PROCESSED, status)
        assertEquals(2, queue.size)
        assertEquals(0, exportList.size)
    }

    @Test
    fun testProcessBadOratureFile() {
        val anyZipFile = createTempFile(suffix = ".zip")
        val exportList = mutableListOf<FileResult>()
        val queue: Queue<File> = LinkedList<File>()
        val status = OratureFileProcessor().process(anyZipFile, queue, exportList)

        assertEquals(FileStatus.REJECTED, status)
        assertEquals(0, queue.size)
        assertEquals(0, exportList.size)
    }

    private fun getOratureFile(fileName: String): File {
        val resource = javaClass.classLoader.getResource(fileName)
                ?: throw FileNotFoundException("Test resource not found: $fileName")
        return File(resource.path)
    }
}