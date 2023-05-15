package org.bibletranslationtools.maui.jvm.io

class DocWriter {
    fun write(filename:String, content:String) {
        println("$filename: ${content.substring(0, 10)}")
    }
}