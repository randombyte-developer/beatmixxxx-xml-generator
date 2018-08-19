package de.randombyte.xmlgenerator.elements

import de.randombyte.xmlgenerator.xml.XmlSerializable

class ControllerPreset(
        val mixxxVersion: String = "",
        val schemaVersion: String = "1",
        val info: Info = Info.EMPTY,
        val controllers: Set<Controller> = emptySet()
) : XmlSerializable() {

    override fun write() {
        element("MixxxControllerPreset") {
            attribute("mixxxVersion", mixxxVersion)
            attribute("schemaVersion", schemaVersion)

            write(info)

            controllers.forEach { controller ->
                write(controller)
            }
        }
    }

    fun validateAll(): ValidationResult {
        return ValidationResult(validate())
    }

    override fun validate() = controllers.flatMap { it.validate() }
}