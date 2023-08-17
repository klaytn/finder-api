package io.klaytn.commons.model.mapper

interface Mapper<SOURCE, OUTPUT> {
    fun transform(source: SOURCE): OUTPUT
}

interface ListMapper<SOURCE, OUTPUT> {
    fun transform(source: List<SOURCE>): List<OUTPUT>
}
