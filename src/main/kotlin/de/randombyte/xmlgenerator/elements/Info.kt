package de.randombyte.xmlgenerator.elements

import de.randombyte.xmlgenerator.xml.XmlSerializable

class Info(
        val name: String? = null,
        val author: String? = null,
        val description: String? = null,
        val wiki: String? = null,
        val forums: String? = null
) : XmlSerializable() {

    companion object {
        val EMPTY = Info()
    }

    override fun write() {
        element("info") {
            element("name", name)
            element("author", author)
            element("description", description)
            element("wiki", wiki)
            element("forums", forums)
        }
    }
}
