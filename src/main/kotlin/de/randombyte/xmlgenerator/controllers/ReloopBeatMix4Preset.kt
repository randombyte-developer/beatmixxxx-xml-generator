package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Options.SCRIPT_BINDING

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
    private val ALL_CHANNELS: Channels = LEFT_SIDE_CHANNELS + RIGHT_SIDE_CHANNELS

    private fun buildControls(): List<Control> {
        val buttons = ALL_CHANNELS.use {
            val shift = control("shift", 0x20)

            val transportControls = mapOf(
                    0x21 to "sync",
                    0x23 to "cue",
                    0x24 to "play"
            ).flatMap { (midiNumber, key) ->
                controlWithShift(key, midiNumber, 0x40)
            }

            val loadTrack = controlWithShift("load", 0x50, -0x10)

            return@use shift + transportControls + loadTrack
        }

        val fifthChannelControls = listOf(0xB5).use {
            controlWithShift("traxRotate", 0x60, 0x10) + controlWithShift("crossfader", 0x2F, 0x30)
        }

        val fifthChannelButtons = listOf(0x95).use {
            controlWithShift("traxPress", 0x09, 0x40)
        }

        val deckSwitchButtons = LEFT_SIDE_CHANNELS.use {
            control("leftDeckSwitch", 0x28)
        } + RIGHT_SIDE_CHANNELS.use {
            control("rightDeckSwitch", 0x28)
        }


        return buttons + fifthChannelControls + fifthChannelButtons + deckSwitchButtons
    }

    // Utils

    private fun <R> Channels.use(func: ChannelUtil.() -> R) = ChannelUtil(this).func()
    private class ChannelUtil(val channels: Channels) {
        fun control(key: String, midiNumber: Int) = control(key, midiNumber, channels)
        fun controlWithShift(key: String, midiNumber: Int, shiftOffset: Int) = controlWithShift(key, midiNumber, shiftOffset, channels)

    }

    private fun control(key: String, midiNumber: Int, channels: Channels): List<Control> =
            channels.mapIndexed { channel0based, status ->
                Control(
                        key = scriptFunction(key),
                        status = status,
                        midiNumber = midiNumber,
                        options = setOf(SCRIPT_BINDING)
                )
            }

    private fun controlWithShift(key: String, midiNumber: Int, shiftOffset: Int, channels: List<Int>): List<Control> {
        val controls = control(key, midiNumber, channels)

        val shiftedControls = controls.map { control ->
            control.copy(midiNumber = control.midiNumber + shiftOffset)
        }

        return controls + shiftedControls
    }

    private fun scriptFunction(name: String): String {
        val functionName = "control" + name.first().toUpperCase() + name.substring(startIndex = 1)
        return "$MIDI_INPUT_PREFIX.$functionName"
    }
}