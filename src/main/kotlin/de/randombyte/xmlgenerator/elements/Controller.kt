package de.randombyte.xmlgenerator.elements

import de.randombyte.xmlgenerator.xml.XmlSerializable
import de.randombyte.xmlgenerator.elements.control.Control

data class Controller(
        val id: String,
        val scriptFiles: List<ScriptFile> = emptyList(),
        val controls: List<Control> = emptyList()
) : XmlSerializable() {

    override fun write() {
        element("controller") {
            attribute("id", id)
            element("scriptfiles") {
                scriptFiles.forEach { scriptFile ->
                    write(scriptFile)
                }
            }
            element("controls") {
                controls.forEach { control ->
                    write(control)
                }
            }
        }
    }

    override fun validate() = scriptFiles.flatMap { it.validate() } + controls.flatMap { it.validate() }
}