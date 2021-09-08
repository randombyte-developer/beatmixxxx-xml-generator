package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.ControlListBuilder
import de.randombyte.xmlgenerator.control
import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control

object HerculesDjci500 {
    private val FUNCTION_PREFIX = "HerculesDjci500"

    val INFO = Info(
            name = "HerculesDjci500",
            author = "RandomByte",
            description = "An experimental mapping for the Hercules DjControl Inpuls 500"
    )

    fun buildPreset(): ControllerPreset {
        val controls = buildControls()

        val controller = Controller(
                id = "HerculesDjci500",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "hercules-djci500.js",
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

    private val ALL_CHANNELS_OFFSET = listOf(0x01, 0x02)

    private const val STATUS_SHIFT_OFFSET = 0x03

    private const val BUTTON_BASE = 0x90
    private const val POTI_BASE = 0xB0

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader", status = POTI_BASE, msb = 0x00, lsb = 0x20)
            +control(name = "Headphone", status = POTI_BASE, msb = 0x04, lsb = 0x24)
            +control(name = "HeadphoneMix", status = POTI_BASE, msb = 0x05, lsb = 0x25)

            +control(name = "TraxEncoder", status = POTI_BASE, msb = 0x01, statusShiftOffset = STATUS_SHIFT_OFFSET)
            +control(name = "TraxButton", status = BUTTON_BASE, msb = 0x00, statusShiftOffset = STATUS_SHIFT_OFFSET)

            ALL_CHANNELS_OFFSET.forEachIndexed { i, channelOffset ->
                val knob = POTI_BASE + channelOffset
                val button = BUTTON_BASE + channelOffset

                +control(name = "${i}Load", status = button, msb = 0x0D, statusShiftOffset = STATUS_SHIFT_OFFSET)

                +control(name = "${i}Play", status = button, msb = 0x07)
                +control(name = "${i}Cue", status = button, msb = 0x06)
                +control(name = "${i}Sync", status = button, msb = 0x05)
                +control(name = "${i}Shift", status = button, msb = 0x04)
                +control(name = "${i}Pfl", status = button, msb = 0x0C, statusShiftOffset = STATUS_SHIFT_OFFSET)

                +control(name = "${i}LoopButton", status = button, msb = 0x2C, statusShiftOffset = STATUS_SHIFT_OFFSET)
                +control(name = "${i}LoopEncoder", status = knob, msb = 0x0E, statusShiftOffset = STATUS_SHIFT_OFFSET)

                +control(name = "${i}JogTouchButton", status = button, msb = 0x08, statusShiftOffset = STATUS_SHIFT_OFFSET)
                +control(name = "${i}JogEncoderUntouched", status = knob, msb = 0x09, statusShiftOffset = STATUS_SHIFT_OFFSET)
                +control(name = "${i}JogEncoderTouched", status = knob, msb = 0x0A, statusShiftOffset = STATUS_SHIFT_OFFSET)

                +control(name = "${i}TempoLedUp", status = button, msb = 0x1E)
                +control(name = "${i}TempoLedDown", status = button, msb = 0x1F)

                +control(name = "${i}LoopIn", status = button, msb = 0x09, statusShiftOffset = STATUS_SHIFT_OFFSET)
                +control(name = "${i}LoopOut", status = button, msb = 0x0A, statusShiftOffset = STATUS_SHIFT_OFFSET)

                (0..7).forEach { hotCueIndex ->
                    +control(name = "${i}Hotcue$hotCueIndex", status = button + 0x05, msb = hotCueIndex, shiftOffset = 0x08)
                }

                +control(name = "${i}Tempo", status = knob, msb = 0x08, lsb = 0x28)

                listOf("Volume", "Filter", "EqLow", "EqMid", "EqHigh", "Gain").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "$i$eqName",
                            status = knob,
                            msb = eqIndex + 0x00,
                            lsb = eqIndex + 0x20
                    )
                }
            }
        }

        return builder.controls
    }
}
