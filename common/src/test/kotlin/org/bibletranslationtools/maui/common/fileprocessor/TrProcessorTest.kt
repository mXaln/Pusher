package org.bibletranslationtools.maui.common.fileprocessor

import org.bibletranslationtools.maui.common.data.FileResult
import org.bibletranslationtools.maui.common.data.FileStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class TrProcessorTest {
    lateinit var queue: Queue<File>
    lateinit var resultList: MutableList<FileResult>

    @Before
    fun setUp() {
        queue = LinkedList<File>()
        resultList = mutableListOf<FileResult>()
    }

    @After
    fun cleanUp() {
        queue.clear()
        resultList.clear()
    }

    @Test
    fun testProcessGoodFile() {
        val file = getTestFile("en_ulb_mat_verse.tr")
        val status = TrProcessor().process(file, queue, resultList)

        assertEquals(FileStatus.PROCESSED, status)
        assertEquals(1, resultList.size)
        assertEquals(0, queue.size)
    }

    @Test
    fun testProcessBadFile() {
        val file = getTestFile("fake.tr")
        val status = TrProcessor().process(file, queue, resultList)

        assertEquals(FileStatus.REJECTED, status)
        assertEquals(0, resultList.size)
        assertEquals(0, queue.size)
    }

    private fun getTestFile(fileName: String): File {
        val resource = javaClass.classLoader.getResource(fileName)
                ?: throw FileNotFoundException("Test resource not found: $fileName")
        return File(resource.path)
    }
}