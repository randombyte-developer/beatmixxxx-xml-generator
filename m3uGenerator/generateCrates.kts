#!kotlinc -script

import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.File;

fun BufferedWriter.writeLine(line: String) {
  write(line)
  newLine()
}

if (args.size != 2) {
  println("Usage: './generateCrates.kts <foldersPath> <crateFilesDestination>'")
  System.exit(0)
}

val path = Paths.get(args[0])
val crateTrackPaths = path.toFile().walk()
  .filter { file -> file.isDirectory() }.map { dir ->
    dir.name to dir.walk()
      .filter { trackFile -> trackFile.isFile && trackFile.extension == "mp3" }
      .map { trackFile -> trackFile.absolutePath }
  }

crateTrackPaths.forEach { (crateName, trackPaths) ->
  Paths.get(args[1], crateName + ".m3u").toFile().apply { parentFile.mkdirs(); createNewFile() }.bufferedWriter().use { writer ->
    writer.writeLine("#EXTM3U")

    trackPaths.forEach { trackPath ->
      writer.writeLine("#EXTINF")
      writer.writeLine(trackPath)
    }
  }
}
