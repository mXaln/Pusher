package org.bibletranslationtools.maui.common.io

import io.reactivex.Single

interface IReader {
    fun read(): Single<List<String>>
}
