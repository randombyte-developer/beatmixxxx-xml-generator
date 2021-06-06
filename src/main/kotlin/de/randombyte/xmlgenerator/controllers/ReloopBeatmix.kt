package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.ControlListBuilder
import de.randombyte.xmlgenerator.control
import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.getMidiToNameMapping

object ReloopBeatmix {

    private val FUNCTION_PREFIX = "ReloopBeatmix"

    val INFO = Info(
            name = "ReloopBeatmix",
            author = "RandomByte",
            description = "An experimental mapping for the Reloop Beatmix"
    )

    fun buildPreset(): ControllerPreset {
        val controls = buildControls()

        val controller = Controller(
                id = "ReloopBeatmix",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "reloop-beatmix.js",
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
    private const val DECK_INDEPENDENT_BUTTON_BASE = BUTTON_BASE + 0x04
    private const val DECK_INDEPENDENT_POTI_BASE = POTI_BASE + 0x04

    private const val BUTTON_SHIFT_OFFSET = -0x20
    private const val ENCODER_SHIFT_OFFSET = 0x01
    private const val POTI_SHIFT_OFFSET = 0x08

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader",   status = DECK_INDEPENDENT_POTI_BASE, msb = 0x53)
            +control(name = "Headphone",    status = DECK_INDEPENDENT_POTI_BASE, msb = 0x51)
            +control(name = "HeadphoneMix", status = DECK_INDEPENDENT_POTI_BASE, msb = 0x52)
            +control(name = "TraxButton",   status = DECK_INDEPENDENT_BUTTON_BASE, msb = 0x20, shiftOffset = 0x20)
            +control(name = "TraxEncoder",  status = DECK_INDEPENDENT_POTI_BASE, msb = 0x18, shiftOffset = ENCODER_SHIFT_OFFSET)

            ALL_CHANNELS_OFFSET.forEach { i ->
                val knob = POTI_BASE + i
                val button = BUTTON_BASE + i

                +control(name = "${i}Play",             status = button,    msb = 0x2F, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}Cue",              status = button,    msb = 0x2E, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}Sync",             status = button,    msb = 0x2C, shiftOffset = BUTTON_SHIFT_OFFSET)

                +control(name = "${i}Shift",            status = button,    msb = 0x28)

                +control(name = "${i}Volume",           status = knob,      msb = 0x37 + 0x10 * i)
                +control(name = "${i}Load",             status = button,    msb = 0x32, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}Pfl",              status = button,    msb = 0x23, shiftOffset = BUTTON_SHIFT_OFFSET)

                +control(name = "${i}FxSelectButton",   status = button,    msb = 0x30, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}FxSelectEncoder",  status = knob,      msb = 0x10 + 0x04 * i, shiftOffset = ENCODER_SHIFT_OFFSET)

                +control(name = "${i}Param2",           status = knob,      msb = 0x30 + 0x11 * i, shiftOffset = POTI_SHIFT_OFFSET)
                +control(name = "${i}Filter",           status = knob,      msb = 0x31 + 0x0F * i, shiftOffset = POTI_SHIFT_OFFSET)

                +control(name = "${i}LoopButton",       status = button,    msb = 0x31, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}LoopEncoder",      status = knob,      msb = 0x12 + 0x04 * i, shiftOffset = ENCODER_SHIFT_OFFSET)

                +control(name = "${i}FxOn",             status = button,    msb = 0x20, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}BeatMash",         status = button,    msb = 0x21 + 0x01 * i, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}AutoLoop",         status = button,    msb = 0x22 - 0x01 * i, shiftOffset = BUTTON_SHIFT_OFFSET)

                +control(name = "${i}Tempo",            status = knob,      msb = 0x36 + 0x10 * i, lsb = 0x76, shiftOffset = POTI_SHIFT_OFFSET)

                +control(name = "${i}JogTouchButton",   status = button,    msb = 0x3F, shiftOffset = 0x20)
                +control(name = "${i}JogEncoder",       status = knob,      msb = 0x20 + 0x02 * i, shiftOffset = ENCODER_SHIFT_OFFSET)

                +control(name = "${i}PitchBendMinus",   status = button,    msb = 0x26, shiftOffset = BUTTON_SHIFT_OFFSET)
                +control(name = "${i}PitchBendPlus",    status = button,    msb = 0x27, shiftOffset = BUTTON_SHIFT_OFFSET)

                listOf("High", "Mid", "Low").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "${i}Eq$eqName",
                            status = knob,
                            msb = 0x33 + eqIndex + 0x10 * i,
                            shiftOffset = 0x08
                    )
                }

                +control(name = "${i}Gain",             status = knob,      msb = 0x32 + 0x10 * i, shiftOffset = POTI_SHIFT_OFFSET)

                (0..2).forEach { hotCueIndex ->
                    +control(name = "${i}Hotcue$hotCueIndex", status = button, msb = 0x29 + hotCueIndex, shiftOffset = BUTTON_SHIFT_OFFSET)
                }
            }
        }

        return builder.controls
    }
}
