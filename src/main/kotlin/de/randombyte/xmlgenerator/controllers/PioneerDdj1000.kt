package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.ControlListBuilder
import de.randombyte.xmlgenerator.control
import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control

object PioneerDdj1000 {
    private val FUNCTION_PREFIX = "PioneerDdj1000"

    val INFO = Info(
            name = "PioneerDdj1000",
            author = "RandomByte",
            description = "An experimental mapping for the Pioneer DDJ 1000"
    )

    fun buildPreset(): ControllerPreset {
        val controls = buildControls()

        val controller = Controller(
                id = "PioneerDdj1000",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "pioneer-ddj1000.js",
                                functionPrefix = FUNCTION_PREFIX
                        )
                ),
                controls = controls.map { it.copy(key = "$FUNCTION_PREFIX.midiInput") }
        )

        return ControllerPreset(
                info = INFO,
                controllers = listOf(controller)
        )
    }

    private val ALL_CHANNELS_OFFSET = listOf(0x00, 0x01)

    private const val BUTTON_BASE = 0x90
    private const val POTI_BASE = 0xB0

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader", status = 0xB6, msb = 0x1F, lsb = 0x3F)
            +control(name = "Headphone", status = 0xB6, msb = 0x0D, lsb = 0x2D)
            +control(name = "HeadphoneMix", status = 0xB6, msb = 0x0C, lsb = 0x2C)
            +control(name = "Master", status = 0xB6, msb = 0x08, lsb = 0x28)

            +control(name = "TraxEncoder", status = 0xB6, msb = 0x40, statusShiftOffset = 0x24)
            +control(name = "LibraryView", status = 0x96, msb = 0x7A)
            +control(name = "LibraryBack", status = 0x96, msb = 0x65)

            ALL_CHANNELS_OFFSET.forEach { offset ->
                val knob = POTI_BASE + offset
                val button = BUTTON_BASE + offset

                +control(name = "${offset}TraxButton", status = 0x96, msb = 0x46 + offset, statusShiftOffset = 0x17)

                +control(name = "${offset}Play", status = button, msb = 0x0B)
                +control(name = "${offset}Cue", status = button, msb = 0x0C)
                +control(name = "${offset}Sync", status = button, msb = 0x58)
                +control(name = "${offset}Shift", status = button, msb = 0x3F)
                +control(name = "${offset}Pfl", status = button, msb = 0x54)

                +control(name = "${offset}LoopButton", status = button, msb = 0x14)
                +control(name = "${offset}LoopIn", status = button, msb = 0x10)
                +control(name = "${offset}LoopOut", status = button, msb = 0x11)

                +control(name = "${offset}JogTouchButton", status = button, msb = 0x36)
                +control(name = "${offset}JogEncoderUntouched", status = knob, msb = 0x21)
                +control(name = "${offset}JogEncoderTouched", status = knob, msb = 0x22)

                +control(name = "${offset}Tempo", status = knob, msb = 0x00, lsb = 0x20)

                +control(name = "${offset}SearchBackward", status = button, msb = 0x5E)
                +control(name = "${offset}SearchForward", status = button, msb = 0x5F)

                val pad = 0x90 + offset * 0x02 + 0x07
                val padShifted = 0x90 + offset * 0x02 + 0x08

                (0..3).forEach { hotCueIndex ->
                    +control(name = "${offset}Hotcue$hotCueIndex", status = pad, msb = hotCueIndex)
                    +control(name = "${offset}Hotcue${hotCueIndex}Shifted", status = padShifted, msb = hotCueIndex)
                }

                +control(name = "${offset}BeatjumpBackward", status = pad, msb = 0x21)
                +control(name = "${offset}BeatjumpForward", status = pad, msb = 0x22)

                +control(name = "${offset}KillLow", status = pad, msb = 0x10)
                +control(name = "${offset}KillMid", status = pad, msb = 0x11)
                +control(name = "${offset}KillHigh", status = pad, msb = 0x12)

                +control(name = "${offset}Gain", status = knob, msb = 0x04, lsb = 0x24)
                +control(name = "${offset}Filter", status = 0xB6, msb = 0x17 + offset, lsb = 0x37 + offset)

                listOf("EqHigh", "EqMid", "EqLow", "Volume").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "$offset$eqName",
                            status = knob,
                            msb = 0x07 + eqIndex * 0x04,
                            lsb = 0x27 + eqIndex * 0x04
                    )
                }
            }
        }

        return builder.controls
    }
}
