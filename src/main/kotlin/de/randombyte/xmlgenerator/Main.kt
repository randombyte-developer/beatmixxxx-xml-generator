package de.randombyte.xmlgenerator

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter
import de.randombyte.xmlgenerator.controllers.PioneerDdjWego
import de.randombyte.xmlgenerator.controllers.ReloopBeatMix4Preset
import de.randombyte.xmlgenerator.controllers.ReloopBeatmix
import de.randombyte.xmlgenerator.xml.document
import java.nio.file.Files
import java.nio.file.Paths
import javax.xml.stream.XMLOutputFactory

val controllerPresets = listOf(
        ReloopBeatMix4Preset.buildPreset(),
        PioneerDdjWego.buildPreset(),
        ReloopBeatmix.buildPreset()
).map { it.info.name to it }.toMap()

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Arguments: <controllerPresetID> <outputFile>")
        return
    }

    val controllerPresetId = args[0]
    val outputFilePathString = args[1]

    val controllerPreset = controllerPresets[controllerPresetId]
    if (controllerPreset == null) {
        println("Controller preset with ID '$controllerPresetId' is not present!")
        return
    }

    val validationResult = controllerPreset.validateAll()
    if (!validationResult.allChecksPassed) {
        val errors = validationResult.errorMessages.joinToString(separator = "\n") { "- '$it'" }
        println("There are ${validationResult.errorMessages.size} error(s):")
        println(errors)

        return
    }

    val midiToNameMappingString = getMidiToNameMapping(
            prefix = controllerPreset.controllers.first().scriptFiles.first().functionPrefix,
            controls = controllerPreset.controllers.first().controls
    )
    println("\n\n" + midiToNameMappingString + "\n\n")

    val outputFilePath = Paths.get(outputFilePathString)

    Files.createDirectories(outputFilePath.parent)
    Files.deleteIfExists(outputFilePath)
    Files.createFile(outputFilePath)

    outputFilePath.toFile().bufferedWriter().use { bufferedWriter ->
        val xmlWriter =
                IndentingXMLStreamWriter( // TODO replace this with something other than com.sun.*
                        XMLOutputFactory
                                .newInstance()
                                .createXMLStreamWriter(bufferedWriter))

        xmlWriter.document {
            controllerPreset.write(this)
        }
    }

    println("Written '$controllerPresetId' to '$outputFilePath'!")
}