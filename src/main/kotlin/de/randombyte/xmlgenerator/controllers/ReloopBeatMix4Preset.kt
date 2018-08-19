package de.randombyte.xmlgenerator.controllers

import de.randombyte.xmlgenerator.elements.Controller
import de.randombyte.xmlgenerator.elements.ControllerPreset
import de.randombyte.xmlgenerator.elements.Info
import de.randombyte.xmlgenerator.elements.ScriptFile
import de.randombyte.xmlgenerator.elements.control.Control
import de.randombyte.xmlgenerator.elements.control.Groups
import de.randombyte.xmlgenerator.elements.control.Options.NORMAL

val RELOOP_BEATMIX_4 = ControllerPreset(
        info = Info(
                name = "Beatmixxxx",
                author = "RandomByte",
                description = "An experimental mapping for the Beatmix 4"
        ),
        controllers = setOf(
                Controller(
                        id = "BeatMix4",
                        scriptFiles = listOf(
                                ScriptFile(
                                        filename = "ReloopBeatmixxxx.js",
                                        functionPrefix = "MyController"
                                )
                        ),
                        controls = (0x91..0x94).mapIndexed { channel, status ->
                            Control(
                                    group = Groups.channel(channel + 1),
                                    key = "play",
                                    status = status,
                                    midiNumber = 0x24,
                                    options = setOf(NORMAL)
                            )
                        }
                )
        )
)