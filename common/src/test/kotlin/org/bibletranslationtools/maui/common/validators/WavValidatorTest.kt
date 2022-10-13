package org.bibletranslationtools.maui.common.validators

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.wycliffeassociates.otter.common.audio.wav.InvalidWavFileException
import java.io.File

class WavValidatorTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun testGoodWavFile() {
        val file = File(javaClass.getResource("/en_ulb_b41_mat_c01.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testBadWavFile() {
        expectedException.expect(InvalidWavFileException::class.java)

        val file = File(javaClass.getResource("/fake.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testBadChapterFileName() {
        expectedException.expect(InvalidWavFileException::class.java)
        expectedException.expectMessage("Chapter filename is incorrect")

        val file = File(javaClass.getResource("/en_mat_c01.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testBadChunkVerseFileName() {
        expectedException.expect(InvalidWavFileException::class.java)
        expectedException.expectMessage("Chunk/verse filename is incorrect")

        val file = File(javaClass.getResource("/mat_c01_v01.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testWavChunkHasGoodMetadata() {
        val file = File(javaClass.getResource("/en_ulb_b41_mat_c01_v01_t01.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testWavChunkHasBadMetadata() {
        expectedException.expect(InvalidWavFileException::class.java)
        expectedException.expectMessage("Chunk has corrupt metadata")

        val file = File(javaClass.getResource("/en_ulb_b41_mat_c01_v01_t01_bad.wav").file)
        val validator = WavValidator(file)
        validator.validate()
    }

    @Test
    fun testWavWithCustomExtension() {
        val file = File(javaClass.getResource("/wav_with_custom_extension.wav").file)

        expectedException.expect(InvalidWavFileException::class.java)
        expectedException.expectMessage("wav file with custom extension is not supported: $file")

        WavValidator(file).validate()
    }
}
