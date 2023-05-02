package org.bibletranslationtools.maui.common.io

import io.reactivex.Single

interface IVersificationReader {
    fun read(): Single<Map<String, List<Int>>>
}