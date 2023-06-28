package org.bibletranslationtools.maui.common.io

import io.reactivex.Single

interface IResourceReader {
    fun read(): Single<List<String>>
}
