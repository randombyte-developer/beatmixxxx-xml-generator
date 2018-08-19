package de.randombyte.xmlgenerator.elements

import de.randombyte.xmlgenerator.xml.XmlSerializable
import de.randombyte.xmlgenerator.xml.XmlSerializable.ValidationResult.Check

class ScriptFile(
        val filename: String,
        val functionPrefix: String
) : XmlSerializable() {

    override fun write() {
        element("file") {
            attribute("filename", filename)
            attribute("functionprefix", functionPrefix)
        }
    }

    override fun validate() = listOf(
            Check.whether("The script must be a .js file!") {
                filename.endsWith(".js")
            })
}
