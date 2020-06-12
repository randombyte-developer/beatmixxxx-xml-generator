package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Options.SCRIPT_BINDING

object PioneerDdjWego {

    private val FUNCTION_PREFIX = "DdjWego"

    val INFO = Info(
            name = "PioneerDDJWego",
            author = "RandomByte",
            description = "An experimental mapping for the Pioneer DDj Wego"
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
                id = "PioneerDDJWego",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "ddj-wego.js",
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

    private val LEFT_CHANNELS_OFFSET = listOf(0x00, 0x02)
    private val RIGHT_CHANNELS_OFFSET = listOf(0x01, 0x03)
    private val LAYERED_CHANNELS_OFFSET = listOf(0x02, 0x03)
    private val ALL_CHANNELS_OFFSET: Channels = LEFT_CHANNELS_OFFSET + RIGHT_CHANNELS_OFFSET
    private const val EFFECT_OFFSET = 0x04
    private const val MIXER_OFFSET = 0x06

    private const val BUTTON_BASE = 0x90
    private const val POTENTIOMETER_BASE = 0xB0

    private fun buildControls(): List<Control> {
        val builder = ControlListBuilder().apply {

            +control(name = "Crossfader", status = 0xB6, msb = 0x1F, lsb = 0x3F, shiftOffset = -0x1F)
            +control(name = "Master", status = 0xB6, msb = 0x03, lsb = 0x23)
            +control(name = "Headphone", status = 0xB6, msb = 0x02, lsb = 0x22)
            +control(name = "HeadphoneMix", status = 0xB6, msb = 0x01, lsb = 0x21)
            +control(name = "TraxButton", status = 0x96, msb = 0x41, shiftOffset = 0x01)
            +control(name = "TraxEncoder", status = 0xB6, msb = 0x40, shiftOffset = 0x24)

            ALL_CHANNELS_OFFSET.forEach { channelIndex ->
                val channelButton = BUTTON_BASE + channelIndex
                val channelPot = POTENTIOMETER_BASE + channelIndex

                +control(name = "${channelIndex}Play", status = channelButton, msb = 0x0B, shiftOffset = 0x3C)
                +control(name = "${channelIndex}Cue", status = channelButton, msb = 0x0C, shiftOffset = 0x3C)
                +control(name = "${channelIndex}Sync", status = channelButton, msb = 0x58, shiftOffset = 0x04)
                +control(name = "${channelIndex}LoopButton", status = channelButton, msb = 0x14, shiftOffset = 0x3C)
                +control(name = "${channelIndex}JogTouchButton", status = channelButton, msb = 0x36, shiftOffset = 0x31) // this is a weird shift state

                +control(name = "${channelIndex}Tempo", status = channelPot, msb = 0x00, lsb = 0x20, shiftOffset = 0x05)
                +control(name = "${channelIndex}LoopEncoder", status = channelPot, msb = 0x13, shiftOffset = 0x3C)

                +control(name = "${channelIndex}JogEncoder", status = channelPot, msb = 0x21, shiftOffset = 0x05)
                +control(name = "${channelIndex}JogEncoderTouch", status = channelPot, msb = 0x22, shiftOffset = 0x05)

                +control(name = "${channelIndex}Volume", status = 0xB6, msb = 0x13 + 2 * channelIndex, lsb = 0x33 + 2 * channelIndex, shiftOffset = 0x01)
                +control(name = "${channelIndex}Load", status = 0x96, msb = 0x46 + channelIndex, shiftOffset = 0x12)
                +control(name = "${channelIndex}Pfl", status = 0x96, msb = 0x54 + channelIndex)

                listOf("High", "Mid", "Low").forEachIndexed { eqIndex, eqName ->
                    +control(
                            name = "${channelIndex}Eq$eqName",
                            status = 0xB6,
                            msb = 0x07 + channelIndex + 4 * eqIndex,
                            lsb = 0x27 + channelIndex + 4 * eqIndex
                    )
                }

                (0..3).forEach { effectIndex ->
                    val effectStatusOffset = channelIndex % 2
                    val effectOffset = if (channelIndex in LAYERED_CHANNELS_OFFSET) 0x05 else 0x00
                    +control(name = "${channelIndex}Effect$effectIndex", status = 0x94 + effectStatusOffset, msb = 0x43 + effectOffset + effectIndex, shiftOffset = 0x0A)
                }

                (0..3).forEach { hotCueIndex ->
                    +control(name = "${channelIndex}Hotcue$hotCueIndex", status = channelButton, msb = 0x2E + hotCueIndex, shiftOffset = 0x31)
                }
            }
        }

        return builder.controls
    }

    private fun control(name: String, status: Int, msb: Int, lsb: Int? = null, shiftOffset: Int? = null): List<Control> {
        val controls = mutableListOf<Control>()

        if (lsb != null) {
            val control = Control(key = name + "Msb", status = status, midiNumber = msb, options = setOf(SCRIPT_BINDING))
            controls += control
            controls += control.copy(key = name + "Lsb", midiNumber = lsb)
        } else {
            controls += Control(key = name, status = status, midiNumber = msb, options = setOf(SCRIPT_BINDING))
        }

        if (shiftOffset != null) {
            controls += controls.map { it.copy(key = it.key + "Shifted", midiNumber = it.midiNumber!! + shiftOffset) }
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
    private fun getMidiToNameMapping(controls: List<Control>): String {
        fun Int.hex() = "0x" + String.format("%02X", this)

        return controls
                .groupBy { it.status }
                .toList()
                .joinToString(prefix = "{\n", separator = ",\n", postfix = "}") { (status, statusControls) ->
                    status.hex() + ": {\n" + statusControls.joinToString(separator = ",\n") { control ->
                        "${control.midiNumber!!.hex()}: \"${control.key}\""
                    } + "\n}"
                }
    }
}
