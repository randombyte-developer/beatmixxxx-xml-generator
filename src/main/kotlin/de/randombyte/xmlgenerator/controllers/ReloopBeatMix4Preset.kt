package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Groups
import de.randombyte.xmlgenerator.elements.control.Options.SCRIPT_BINDING

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

    private val ALL_CHANNELS = (0x91..0x94).toList()

    object ShiftOffsets {
        val TRANSPORT = 0x40
    }

    private fun buildControls(): List<Control> {
        val shift = control(scriptFunction("shift"), 0x20, SCRIPT_BINDING)

        val transportControls = mapOf(
                0x21 to "sync",
                0x23 to "cue",
                0x24 to "play"
        ).flatMap { (midiNumber, key) ->
            controlWithShift(scriptFunction(key), midiNumber, ShiftOffsets.TRANSPORT, SCRIPT_BINDING)
        }


        return shift + transportControls
    }

    private fun control(key: String, midiNumber: Int, vararg options: String): List<Control> =
            ALL_CHANNELS.mapIndexed { channel, status ->
                Control(
                        group = Groups.channel(channel + 1),
                        key = key,
                        status = status,
                        midiNumber = midiNumber,
                        options = options.toSet()
                )
            }

    private fun controlWithShift(key: String, midiNumber: Int, shiftOffset: Int, vararg options: String): List<Control> {
        val controls = control(key, midiNumber, *options)

        val shiftedControls = controls.map { control ->
            control.copy(midiNumber = control.midiNumber + shiftOffset)
        }

        return controls + shiftedControls
    }

    private fun scriptFunction(functionName: String): String {
        val finalFunctionName = "control" + functionName.first().toUpperCase() + functionName.substring(startIndex = 1)
        return "$MIDI_INPUT_PREFIX.$finalFunctionName"
    }
}