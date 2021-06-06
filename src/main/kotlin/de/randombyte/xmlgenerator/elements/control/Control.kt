package de.randombyte.xmlgenerator.elements.control

import de.randombyte.xmlgenerator.xml.XmlSerializable

data class Control(
        val group: String? = null,
        val key: String = "NOT_SET",
        val internalKey: String, // only for the JS mapping, not serialized to XML
        val description: String? = null,
        val status: Int,
        val midiNumber: Int?,
        val options: Set<String> = emptySet()
) : XmlSerializable() {

    override fun write() {
        element("control") {
            element("group", group)
            element("key", key)
            element("description", description)
            element("status", status.hex)
            element("midino", midiNumber?.hex)
            element("options") {
                options.forEach { option ->
                    element(option)
                }
            }
        }
    }

    override fun validate(): List<ValidationResult.Check> {
        return listOf(
                ValidationResult.Check.whether("The group must be enclosed in square braces []!") {
                    if (group == null) true else {
                        group.startsWith("[") && group.endsWith("]")
                    }
                }
        )
    }

    private val Int.hex get() = "0x" + toString(radix = 16)
}
