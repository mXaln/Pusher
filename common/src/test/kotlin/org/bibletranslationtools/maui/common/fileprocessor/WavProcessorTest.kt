package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.util.Queue
import java.util.LinkedList

class WavProcessorTest {
    lateinit var queue: Queue<File>
    lateinit var resultList: MutableList<FileResult>
    lateinit var tempDir: File
    val wavFileName = "en_ulb_b41_mat_c01.wav"

    @Before
    fun setUp() {
        tempDir = kotlin.io.path.createTempDirectory().toFile()
        queue = LinkedList<File>()
        resultList = mutableListOf<FileResult>()
    }

    @After
    fun cleanUp() {
        tempDir.deleteRecursively()
        queue.clear()
        resultList.clear()
    }

    @Test
    fun testProcessGoodFile() {
        val file = getTestFile(wavFileName)
        val status = WavProcessor().process(file, queue, resultList)

        assertEquals(FileStatus.SUCCESS, status)
        assertEquals(1, resultList.size)
        assertEquals(0, queue.size)
    }

    @Test
    fun testCustomResource() {
        val file = copyAndRename(wavFileName, "en_reg_b41_mat_c01.wav")
        val status = WavProcessor().process(file, queue, resultList)

        assertEquals(FileStatus.SUCCESS, status)
        assertEquals(1, resultList.size)
        assertEquals(0, queue.size)
    }

    @Test
    fun testProcessBadFile() {
        val file = getTestFile("fake.wav")
        val status = WavProcessor().process(file, queue, resultList)

        assertEquals(FileStatus.ERROR, status)
        assertEquals(0, resultList.size)
        assertEquals(0, queue.size)
    }

    private fun getTestFile(fileName: String): File {
        val resource = javaClass.classLoader.getResource(fileName)
                ?: throw FileNotFoundException("Test resource not found: $fileName")
        return File(resource.path)
    }

    private fun copyAndRename(source: String, target: String): File {
        val wav = getTestFile(source)
        val newFile = tempDir.resolve(target)
        wav.copyTo(newFile, true)

        return newFile
    }
}