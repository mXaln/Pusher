package org.bibletranslationtools.maui.jvm.io

import org.junit.Assert
import org.junit.Test

class LanguagesReaderTest {

    @Test
    fun testReadLanguagesFile() {

        val result = LanguagesReader().read()

        Assert.assertTrue(result.isNotEmpty())
    }
}
