package de.randombyte.xmlgenerator

import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Options

fun <T, R> Iterable<T>.flatMapIndexed(transform: (Int, T) -> Iterable<R>): List<R> = this
        .toList()
        .mapIndexed { index, t -> Pair(index, t) }
        .flatMap { (index, t) -> transform(index, t) }

fun control(name: String, status: Int, msb: Int, lsb: Int? = null, shiftOffset: Int? = null, statusShiftOffset: Int? = null): List<Control> {
    val controls = mutableListOf<Control>()

    if (lsb != null) {
        val control = Control(internalKey = name + "Msb", status = status, midiNumber = msb, options = setOf(Options.SCRIPT_BINDING))
        controls += control
        controls += control.copy(internalKey = name + "Lsb", midiNumber = lsb)
    } else {
        controls += Control(internalKey = name, status = status, midiNumber = msb, options = setOf(Options.SCRIPT_BINDING))
    }

    if (shiftOffset != null) {
        controls += controls.map { it.copy(internalKey = it.internalKey + "Shifted", midiNumber = it.midiNumber!! + shiftOffset) }
    }

    if (statusShiftOffset != null) {
        controls += controls.map { it.copy(internalKey = it.internalKey + "Shifted", status = it.status + statusShiftOffset) }
    }

    return controls
}

class ControlListBuilder {
    private val allControls = mutableListOf<Control>()
    val controls: List<Control> get() = allControls

    operator fun List<Control>.unaryPlus() {
        allControls.addAll(this)
    }
}

// For the typescript mapping
fun getMidiToNameMapping(prefix: String, controls: List<Control>): String {
    fun Int.hex() = "0x" + String.format("%02X", this)

    return controls
            .groupBy { it.status }
            .toList()
            .joinToString(prefix = "{\n", separator = ",\n", postfix = "}") { (status, statusControls) ->
                status.hex() + ": {\n" + statusControls.joinToString(separator = ",\n") { control ->
                    "${control.midiNumber!!.hex()}: \"${control.internalKey}\""
                } + "\n}"
            }
}
