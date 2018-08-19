package de.randombyte.xmlgenerator.xml

import javax.xml.stream.XMLStreamWriter

abstract class XmlSerializable {

    companion object {
        val ILLEGAL_STATE_EXCEPTION = IllegalStateException("This function must only be called from the 'write()' function!")
    }

    // temporary set and used when executing the write() function
    private var writer: XMLStreamWriter? = null

    fun write(writer: XMLStreamWriter) {
        this.writer = writer
        write()
        this.writer = null
    }

    /**
     * Writes the element to XML using the xml writer functions in this class.
     */
    abstract fun write()

    /**
     * Overwrite this to perform validation checks.
     */
    open fun validate(): List<ValidationResult.Check> = emptyList()

    class ValidationResult(val checks: List<Check>) {
        sealed class Check {
            object Successful : Check()
            class Error(val message: String): Check()

            companion object {
                fun whether(errorMessage: String, check: () -> Boolean) = whether(errorMessage, check())
                fun whether(errorMessage: String, check: Boolean) = if (check) Successful else Error(errorMessage)
            }
        }

        val allChecksPassed = checks.all { it === Check.Successful }
        val errorMessages = checks.mapNotNull { it as? Check.Error }.map { it.message }
    }

    // XML utils

    fun document(function: XMLStreamWriter.() -> Unit) {
        writer?.document(function) ?: throw ILLEGAL_STATE_EXCEPTION
    }

    fun element(name: String, function: XMLStreamWriter.() -> Unit) {
        writer?.element(name, function) ?: throw ILLEGAL_STATE_EXCEPTION
    }

    fun element(name: String, content: String? = null) {
        writer?.element(name, content) ?: throw ILLEGAL_STATE_EXCEPTION
    }

    fun attribute(name: String, value: String) {
        writer?.attribute(name, value) ?: throw ILLEGAL_STATE_EXCEPTION
    }

    fun write(xmlSerializable: XmlSerializable?) {
        writer?.let { xmlSerializable?.write(it) } ?: throw ILLEGAL_STATE_EXCEPTION
    }
}