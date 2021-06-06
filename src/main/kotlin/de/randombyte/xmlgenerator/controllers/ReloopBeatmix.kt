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

        val midiToNameMappingString = getMidiToNameMapping(controls)
        println()
        println()
        println(midiToNameMappingString)
        println()
        println()

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
    private const val POTENTIOMETER_BASE = 0xB0

    private const val DECK_INDEPENDENT = 0xB4

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader",   status = DECK_INDEPENDENT, msb = 0x53)
            +control(name = "Headphone",    status = DECK_INDEPENDENT, msb = 0x51)
            +control(name = "HeadphoneMix", status = DECK_INDEPENDENT, msb = 0x51)
            +control(name = "TraxButton",   status = DECK_INDEPENDENT, msb = 0x20, shiftOffset = 0x20)
            +control(name = "TraxEncoder",  status = DECK_INDEPENDENT, msb = 0x18, shiftOffset = 0x01)

            ALL_CHANNELS_OFFSET.forEach { i ->
                val s = 0xB0 + i

                +control(name = "${i}Play",             status = s, msb = 0x2F, shiftOffset = -0x20)
                +control(name = "${i}Cue",              status = s, msb = 0x2E, shiftOffset = -0x20)
                +control(name = "${i}Sync",             status = s, msb = 0x2C, shiftOffset = -0x20)
                +control(name = "${i}LoopButton",       status = s, msb = 0x14, shiftOffset = 0x3C) // TODO ab hier
                +control(name = "${i}JogTouchButton",   status = s, msb = 0x36, shiftOffset = 0x31)

                +control(name = "${i}Tempo",            status = s, msb = 0x00, lsb = 0x20, shiftOffset = 0x05)
                +control(name = "${i}LoopEncoder",      status = s, msb = 0x13, shiftOffset = 0x3C)

                +control(name = "${i}JogEncoder",       status = s, msb = 0x21, shiftOffset = 0x05)
                +control(name = "${i}JogEncoderTouch",  status = s, msb = 0x22, shiftOffset = 0x05)

                +control(name = "${i}Volume",           status = s, msb = 0x13 + 2 * i, lsb = 0x33 + 2 * i, shiftOffset = 0x01)
                +control(name = "${i}Load",             status = s, msb = 0x46 + i, shiftOffset = 0x12)
                +control(name = "${i}Pfl",              status = s, msb = 0x54 + i)

                listOf("High", "Mid", "Low").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "${i}Eq$eqName",
                            status = 0xB6,
                            msb = 0x07 + i + 4 * eqIndex,
                            lsb = 0x27 + i + 4 * eqIndex
                    )
                }

                (0..3).forEach { effectIndex ->
                    val effectStatusOffset = i % 2
                    val effectOffset = if (i in LAYERED_CHANNELS_OFFSET) 0x05 else 0x00
                    +control(name = "${i}Effect$effectIndex", status = 0x94 + effectStatusOffset, msb = 0x43 + effectOffset + effectIndex, shiftOffset = 0x0A)
                }

                (0..3).forEach { hotCueIndex ->
                    +control(name = "${i}Hotcue$hotCueIndex", status = s, msb = 0x2E + hotCueIndex, shiftOffset = 0x31)
                }
            }
        }

        return builder.controls
    }
}
