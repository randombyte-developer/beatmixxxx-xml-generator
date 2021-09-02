package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.ControlListBuilder
import de.randombyte.xmlgenerator.control
import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control

object NumarkNv2 {

    private val FUNCTION_PREFIX = "NumarkNv2"

    val INFO = Info(
            name = "NumarkNv2",
            author = "RandomByte",
            description = "An experimental mapping for the Numark NV II"
    )

    fun buildPreset(): ControllerPreset {
        val controls = buildControls()

        val controller = Controller(
                id = "NumarkNv2",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "numark-nv2.js",
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

    // the controller supports 4 channels but only 2 are used
    private val ALL_CHANNELS_OFFSET = listOf(0x01, 0x02)

    private const val BUTTON_BASE = 0x90
    // "on" status is 0x91, "off" is 0x81
    // we ignore the status and only react to the data2 byte, which is >0x00 for "on" and =0x00 for "off" (velocity pads included)
    private val BUTTON_OFFSETS = listOf(0x00, 0x10)

    private const val POTI_BASE = 0xB0

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader",   status = POTI_BASE, msb = 0x07, lsb = 0x27)
            +control(name = "Headphone",    status = POTI_BASE, msb = 0x44)
            +control(name = "HeadphoneMix", status = POTI_BASE, msb = 0x12)
            
            listOf(0x03, 0x4D).forEach {
                +control(name = "TraxEncoder", status = POTI_BASE, msb = it)
            }

            ALL_CHANNELS_OFFSET.forEachIndexed { i, offset ->
                val knob = POTI_BASE + offset
                val button = BUTTON_BASE + offset

                +control(name = "${i}TraxButton",       status = button,        msb = 0x07)

                +control(name = "${i}Play",             status = button,        msb = 0x21)
                +control(name = "${i}Cue",              status = button,        msb = 0x20)
                +control(name = "${i}Sync",             status = button,        msb = 0x1F)

                +control(name = "${i}Shift",            status = button,        msb = 0x0D)

                +control(name = "${i}Volume",           status = knob,          msb = 0x08 + 0x05 * i,  lsb = 0x28 + 0x05 * i)
                +control(name = "${i}Back",             status = button,        msb = 0x08)
                +control(name = "${i}Pfl",              status = BUTTON_BASE,   msb = 0x35 + i)

                +control(name = "${i}Loop",             status = button,        msb = 0x1B)

                +control(name = "${i}Tempo",            status = knob,          msb = 0x01,             lsb = 0x21)

                +control(name = "${i}JogTouchButton",   status = button,        msb = 0x5D)
                +control(name = "${i}JogEncoder",       status = knob,          msb = 0x00,             lsb = 0x20)

                +control(name = "${i}PitchBendMinus",   status = button,        msb = 0x10)
                +control(name = "${i}PitchBendPlus",    status = button,        msb = 0x11)

                +control(name = "${i}ParamAdjustLeft",  status = button,        msb = 0x0E)
                +control(name = "${i}ParamAdjustRight", status = button,        msb = 0x0F)

                +control(name = "${i}Filter",           status = POTI_BASE,     msb = 0x5B + i,         lsb = 0x7B + i)

                listOf("EqLow", "EqMid", "EqHigh", "Gain").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "$i$eqName",
                            status = POTI_BASE,
                            msb = 0x09 + eqIndex + i * 0x05,
                            lsb = 0x29 + eqIndex + i * 0x05
                    )
                }

                (0..7).forEach { hotCueIndex ->
                    +control(name = "${i}Hotcue$hotCueIndex", status = button, msb = 0x47 + hotCueIndex)
                }
            }
        }

        return builder.controls
    }
}
