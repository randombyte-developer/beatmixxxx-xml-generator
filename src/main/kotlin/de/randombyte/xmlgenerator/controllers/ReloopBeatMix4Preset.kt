package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Options.SCRIPT_BINDING
import de.randombyte.xmlgenerator.flatMapIndexed

typealias Channels = List<Int>


object ReloopBeatMix4Preset {

    private val FUNCTION_PREFIX = "Beatmixxxx"
    private val MIDI_INPUT_PREFIX = "$FUNCTION_PREFIX.midiInput"

    private val INFO = Info(
            name = "Beatmixxxx",
            author = "RandomByte",
            description = "An experimental mapping for the Beatmix 4"
    )

    fun buildPreset(): ControllerPreset {
        val controls = buildControls()

        val controller = Controller(
                id = "BeatMix4",
                scriptFiles = listOf(
                        ScriptFile(
                                filename = "lodash-4.17.10.js",
                                functionPrefix = ""
                        ),
                        ScriptFile(
                                filename = "ReloopBeatmixxxx.js",
                                functionPrefix = FUNCTION_PREFIX
                        )
                ),
                controls = controls
        )

        return ControllerPreset(
                info = INFO,
                controllers = listOf(controller)
        )
    }

    private val LEFT_SIDE_CHANNELS = listOf(0x91, 0x93)
    private val RIGHT_SIDE_CHANNELS = listOf(0x92, 0x94)
    private val EFFECTS_CHANNELS = listOf(0x91, 0x92)
    private val ALL_CHANNELS: Channels = LEFT_SIDE_CHANNELS + RIGHT_SIDE_CHANNELS

    private fun buildControls(): List<Control> = ALL_CHANNELS.use {
        mapOf(
                0x21 to "sync",
                0x22 to "play",
                0x23 to "cue",
                0x26 to "pitchMinus",
                0x27 to "pitchPlus",
                0x3F to "wheelTouch",
				0x25 to "effectsButton"
        ).flatMap { (midiNumber, key) ->
            controlWithShift(key, midiNumber, 0x40)
        } +
                control("shift", 0x20) +
                controlWithShift("load", 0x50, -0x10) +
                controlWithShift("pfl", 0x52, -0x10) +
                control("padModeA", 0x29) + control("padModeB", 0x2A)
    } + listOf(0xB5).use {
        controlWithShift("traxRotate", 0x60, 0x10) +
				controlWithShift("crossfader", 0x2F, 0x30) +
				controlWithShift("samplerVolume", 0x03, 0x30)
    } + listOf(0x95).use {
        controlWithShift("traxPress", 0x09, 0x40) + controlWithShift("back", 0x08, 0x40)
    } + LEFT_SIDE_CHANNELS.use {
        control("leftDeckSwitch", 0x28)
    } + RIGHT_SIDE_CHANNELS.use {
        control("rightDeckSwitch", 0x28)
    } + (0xB1..0xB4).toList().use {
        controlWithShift("volume", 0x14, 0x20) +
                controlWithShift("wheelRotate", 0x60, 0x10) +
                controlWithShift("gainKnob", 0x10, 0x20) +
                controlWithShift("highKnob", 0x11, 0x20) +
                controlWithShift("midKnob", 0x12, 0x20) +
                controlWithShift("lowKnob", 0x13, 0x20) +
                controlWithShift("effectsEncoder", 0x61, 0x10)
    } + listOf(0xB1, 0xB2).use {
        (0..2).flatMap { index ->
            controlWithShift("effectKnob$index", index + 1, 0x40)
        }
    } + (0xE1..0xE4).toList().use {
        control("rate", midiNumber = null)
    } + EFFECTS_CHANNELS.use {
        (0..2).flatMap { index ->
            controlWithShift("effectButton$index", 0x70 + index + 1, 0x03)
        }
    } + ALL_CHANNELS.use {
        mapOf<List<Int>, Function1<Int, String>>(
                (0x00..0x07).toList() to { i -> "bluePad$i" },
                (0x10..0x17).toList() to { i -> "redPad$i" }
                //(0x00..0x03).toList() + (0x10..0x13).toList() to { i -> "violetPad$i" }
        ).toList().flatMap { (midiNumbers, generateKey) ->
            midiNumbers.flatMapIndexed { index, midiNumber ->
                controlWithShift(generateKey(index), midiNumber, 0x08)
            }
        }
    }

    // Utils

    private fun <R> Channels.use(func: ChannelUtil.() -> R) = ChannelUtil(this).func()
    private class ChannelUtil(val channels: Channels) {
        fun control(key: String, midiNumber: Int?) = control(key, midiNumber, channels)
        fun controlWithShift(key: String, midiNumber: Int, shiftOffset: Int) = controlWithShift(key, midiNumber, shiftOffset, channels)

    }

    private fun control(key: String, midiNumber: Int?, channels: Channels): List<Control> =
            channels.mapIndexed { channel0based, status ->
                Control(
                        key = scriptFunction(key),
                        internalKey = "",
                        status = status,
                        midiNumber = midiNumber,
                        options = setOf(SCRIPT_BINDING)
                )
            }

    private fun controlWithShift(key: String, midiNumber: Int, shiftOffset: Int, channels: List<Int>): List<Control> {
        val controls = control(key, midiNumber, channels)

        val shiftedControls = controls.map { control ->
            control.copy(midiNumber = control.midiNumber!! + shiftOffset)
        }

        return controls + shiftedControls
    }

    private fun scriptFunction(name: String): String {
        val functionName = "control" + name.first().toUpperCase() + name.substring(startIndex = 1)
        return "$MIDI_INPUT_PREFIX.$functionName"
    }
}
