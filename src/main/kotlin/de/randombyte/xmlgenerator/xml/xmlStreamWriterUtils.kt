package de.randombyte.xmlgenerator.xml

import javax.xml.stream.XMLStreamWriter

fun XMLStreamWriter.document(function: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartDocument()
    this.function()
    this.writeEndDocument()
    return this
}

fun XMLStreamWriter.element(name: String, function: XMLStreamWriter.() -> Unit): XMLStreamWriter {
    this.writeStartElement(name)
    this.function()
    this.writeEndElement()
    return this
}

fun XMLStreamWriter.element(name: String, content: String? = null) {
    element(name) {
        writeCharacters(content)
    }
}

fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)