package org.bibletranslationtools.maui.jvm.mappers

import org.bibletranslationtools.maui.common.data.VerifiedResult
import org.thymeleaf.context.Context

class VerifiedResultMapper : IMapper<List<VerifiedResult>, Context> {
    override fun fromEntity(type: List<VerifiedResult>): Context {
        val context = Context()
        context.setVariable("results", type)
        return context
    }

    override fun toEntity(type: Context): List<VerifiedResult> {
        return (type.getVariable("results") ?: emptyList<VerifiedResult>()) as List<VerifiedResult>
    }
}